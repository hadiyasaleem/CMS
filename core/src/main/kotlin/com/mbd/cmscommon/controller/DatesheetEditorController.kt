package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetCurriculumDrift
import com.mbd.cmscommon.domain.model.DatesheetDraft
import com.mbd.cmscommon.domain.model.DatesheetScheduleQuality
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.datesheetCurriculumDrift
import com.mbd.cmscommon.domain.model.datesheetExternalConflicts
import com.mbd.cmscommon.domain.model.datesheetScheduleQuality
import com.mbd.cmscommon.domain.model.normalized
import com.mbd.cmscommon.domain.model.validationMessage
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.orLogCritical
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Manages one existing datesheet: its papers, curriculum drift, and publish lifecycle. */
class DatesheetEditorController(
    val datesheetId: String,
    private val datesheetRepository: DatesheetRepository,
    sessionRepository: AcademicSessionRepository,
    private val curriculumRepository: CurriculumRepository,
    teacherRepository: TeacherRepository,
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val sheet: StateFlow<Datesheet?> = datesheetRepository.observeDatesheets()
        .map { sheets -> sheets.firstOrNull { it.id == datesheetId } }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val session: StateFlow<AcademicSession?> = sheet
        .flatMapLatest { s -> if (s == null) flowOf(null) else sessionRepository.observeSession(s.sessionId) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    /** This datesheet's own semester's curriculum -- the source for its papers, same pattern SessionTimetableController uses. */
    val subjects: StateFlow<List<SemesterSubject>> = sheet
        .flatMapLatest { s -> if (s == null) flowOf(emptyList()) else curriculumRepository.observeSemesterSubjects(s.sessionId, s.semester) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val slots: StateFlow<List<DatesheetSlot>> = datesheetRepository.observeSlots(datesheetId)
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val teachers: StateFlow<List<Teacher>> = teacherRepository.observeActiveTeachers().stateIn(scope, SharingStarted.Eagerly, emptyList())
    val buildings: StateFlow<List<Building>> = buildingRepository.observeActiveBuildings().stateIn(scope, SharingStarted.Eagerly, emptyList())
    val rooms: StateFlow<List<Room>> = roomRepository.observeActiveRooms().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val quality: StateFlow<DatesheetScheduleQuality?> = combine(sheet, slots) { s, sl -> s?.let { datesheetScheduleQuality(it, sl) } }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val curriculumDrift: StateFlow<DatesheetCurriculumDrift?> = combine(slots, subjects) { sl, subj ->
        if (subj.isEmpty()) null else datesheetCurriculumDrift(sl, subj)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        // A freshly created datesheet has no papers yet -- once its session's curriculum for this
        // semester is available, prefill one unscheduled paper per subject.
        launch {
            val current = sheet.filterNotNull().first()
            runCatching { curriculumRepository.syncSession(current.sessionId) }.orLogCritical("DatesheetEditorController.syncCurriculum")
            val currentSubjects = curriculumRepository.observeSemesterSubjects(current.sessionId, current.semester).first()
            val currentSlots = datesheetRepository.observeSlots(datesheetId).first()
            if (currentSlots.isEmpty() && currentSubjects.isNotEmpty()) {
                datesheetRepository.prefillPapers(datesheetId, currentSubjects)
            }
        }
    }

    fun consumeActionMessage() {
        _actionMessage.value = null
    }

    fun updateDefaults(defaultStartTime: String?, defaultEndTime: String?, defaultBuildingId: String?, instructions: String?) = mutate {
        val current = requireCurrentSheet()
        val draft = DatesheetDraft(
            sessionId = current.sessionId,
            semester = current.semester,
            defaultStartTime = defaultStartTime,
            defaultEndTime = defaultEndTime,
            defaultBuildingId = defaultBuildingId,
            instructions = instructions,
            published = current.published,
        )
        validationMessage(draft).orThrowValidation()
        datesheetRepository.updateDatesheet(datesheetId, draft)
        _actionMessage.value = "Datesheet updated."
    }

    fun setPublished(published: Boolean) = mutate {
        val current = requireCurrentSheet()
        if (published) {
            val currentQuality = datesheetScheduleQuality(current, slots.value)
            requireValid(currentQuality.canPublish) { currentQuality.issues.joinToString(" ") }
        }
        datesheetRepository.setPublished(datesheetId, published)
        _actionMessage.value = if (published) "Datesheet published." else "Datesheet moved to drafts."
    }

    fun deleteDatesheet() = mutate {
        requireCurrentSheet()
        datesheetRepository.deleteDatesheet(datesheetId)
        _actionMessage.value = "Datesheet deleted."
    }

    /** Adds a paper for every curriculum subject the drift banner reports as missing. */
    fun syncMissingSubjects() = mutate {
        val missing = curriculumDrift.value?.missingSubjects.orEmpty()
        requireValid(missing.isNotEmpty()) { "There are no missing subjects to add." }
        datesheetRepository.prefillPapers(datesheetId, missing)
        _actionMessage.value = "Added ${missing.size} missing subject(s)."
    }

    /** Removes a paper -- typically one the curriculum-drift banner flagged as stale. */
    fun removePaper(slotId: String) = mutate {
        requireValid(slotId.isNotBlank()) { "This paper has no database ID and cannot be removed safely." }
        val current = requireCurrentSheet()
        val existing = slots.value
        requireValid(existing.any { it.id == slotId }) { "This paper is no longer in the datesheet. Refresh and try again." }
        if (current.published && existing.size <= 1) {
            throw CmsException.Validation("Move the datesheet to drafts before removing its final paper.")
        }
        datesheetRepository.deleteSlot(slotId)
        _actionMessage.value = "Exam paper removed."
    }

    fun updatePaper(slot: DatesheetSlot) = mutate {
        val current = requireCurrentSheet()
        requireValid(slot.id.isNotBlank()) { "This paper has no database ID and cannot be updated safely." }
        val normalizedSlot = normalized(slot)
        validationMessage(normalizedSlot).orThrowValidation()
        val existing = slots.value
        requireValid(existing.any { it.id == normalizedSlot.id }) { "This paper is no longer in the datesheet. Refresh and try again." }

        // Only the structural constraint the database itself enforces for this one paper (one paper
        // per date within a datesheet) -- NOT full-datesheet publish-readiness. Every other paper
        // still needing a date/time/building must never block saving *this* paper's edit; that
        // full-sheet completeness check belongs to setPublished()/the "needs review" banner only.
        if (normalizedSlot.examDate != null) {
            val dateClash = existing.firstOrNull { it.id != normalizedSlot.id && it.examDate == normalizedSlot.examDate }
            if (dateClash != null) {
                val label = dateClash.subjectName.ifBlank { dateClash.courseCode }
                throw CmsException.Validation("$label is already scheduled on ${normalizedSlot.examDate} in this datesheet.")
            }
        }

        // Client-side warning for a room/invigilator clash on another datesheet -- the database
        // trigger is the authoritative guard; this just surfaces the same problem before the save
        // round-trip instead of after.
        val date = normalizedSlot.examDate
        if (date != null) {
            val otherPapers = datesheetRepository.getPapersOnDates(setOf(date)).filter { it.datesheetId != datesheetId }
            if (otherPapers.isNotEmpty()) {
                val sheetsById = datesheetRepository.observeDatesheets().first().associateBy { it.id }
                val pairs = otherPapers.mapNotNull { paper -> sheetsById[paper.datesheetId]?.let { paper to it } }
                val externalConflicts = datesheetExternalConflicts(normalizedSlot, current, pairs)
                requireValid(externalConflicts.isEmpty()) { externalConflicts.joinToString(" ") }
            }
        }

        datesheetRepository.updateSlot(normalizedSlot)
        _actionMessage.value = "Exam paper updated."
    }

    private fun mutate(block: suspend () -> Unit) {
        if (_busy.value) return
        launch {
            clearError()
            _actionMessage.value = null
            _busy.value = true
            try {
                block()
            } finally {
                _busy.value = false
            }
        }
    }

    private fun requireCurrentSheet(): Datesheet =
        sheet.value ?: throw CmsException.NotFound("This datesheet is no longer available. Refresh and try again.")
}
