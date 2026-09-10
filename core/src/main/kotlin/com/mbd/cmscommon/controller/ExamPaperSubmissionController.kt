package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.examPaperUploadError
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.util.Outcome
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** One published-datesheet exam slot the teacher owns, joined with its submission (if any). */
data class TeacherPaperSlot(
    val datesheet: Datesheet,
    val slot: DatesheetSlot,
    val submission: ExamPaperSubmission?,
) {
    val isSubmitted: Boolean get() = submission != null
}

/** A file picked off-device but not yet uploaded -- [sizeError] is populated immediately (before
 * any network call) so an oversized or wrong-type file is rejected right at pick time. */
data class StagedPaperFile(val bytes: ByteArray, val fileName: String, val sizeError: String?)

class ExamPaperSubmissionController(
    private val examPaperRepository: ExamPaperSubmissionRepository,
    private val datesheetRepository: DatesheetRepository,
    private val assignmentsProvider: TeacherAssignmentsProvider,
    sessionRepository: AcademicSessionRepository,
    private val teacherId: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Every subject this teacher teaches that also has a slot on a published datesheet -- there is
     * no datesheet-side notion of "who set this paper," so the join is on (sessionId, courseCode)
     * against the teacher's own timetable-driven assignments. */
    val slots: StateFlow<List<TeacherPaperSlot>> = combine(
        datesheetRepository.observeDatesheets(),
        datesheetRepository.observeAllSlots(),
        examPaperRepository.observeAllSubmissions(),
        assignmentsProvider.observeAssignmentsFor(teacherId),
    ) { datesheets, allSlots, submissions, assignments ->
        val published = datesheets.filter { it.published }.associateBy { it.id }
        val myKeys = assignments.map { it.sessionId to it.courseCode }.toSet()
        val submissionBySlot = submissions.associateBy { it.datesheetSlotId }
        allSlots
            .mapNotNull { slot ->
                val sheet = published[slot.datesheetId] ?: return@mapNotNull null
                if ((sheet.sessionId to slot.courseCode) !in myKeys) return@mapNotNull null
                TeacherPaperSlot(sheet, slot, submissionBySlot[slot.id])
            }
            .sortedWith(compareBy({ it.datesheet.sessionId }, { it.slot.courseCode }))
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSlotId = MutableStateFlow<String?>(null)

    /** Re-derived from [slots] on every change (not just captured once) so the dialog always
     * reflects the latest submission state for the selected slot -- e.g. right after an upload or
     * delete, without the caller having to re-select it. */
    val selected: StateFlow<TeacherPaperSlot?> = combine(slots, _selectedSlotId) { list, id ->
        id?.let { sid -> list.find { it.slot.id == sid } }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    private val _stagedFile = MutableStateFlow<StagedPaperFile?>(null)
    val stagedFile: StateFlow<StagedPaperFile?> = _stagedFile.asStateFlow()

    private val _uploadState = MutableStateFlow<Outcome<Unit>?>(null)
    val uploadState: StateFlow<Outcome<Unit>?> = _uploadState.asStateFlow()

    fun selectSlot(slot: TeacherPaperSlot?) {
        _selectedSlotId.value = slot?.slot?.id
        _stagedFile.value = null
        _uploadState.value = null
    }

    /** Validates size/type immediately, before any network call -- [StagedPaperFile.sizeError]
     * drives an inline error in the picker step, never letting an invalid file reach [confirmUpload]. */
    fun stageFile(fileBytes: ByteArray, fileName: String) {
        _stagedFile.value = StagedPaperFile(fileBytes, fileName, examPaperUploadError(fileName, fileBytes))
        _uploadState.value = null
    }

    fun clearStagedFile() {
        _stagedFile.value = null
    }

    /** For the platform file-picker's own read step failing, before [stageFile] ever gets called. */
    fun reportPickFailure(t: Throwable) {
        _uploadState.value = Outcome.Error(t.userMessageLogged("Could not read the selected file."), t)
    }

    fun confirmUpload(fileName: String, description: String?) {
        val target = selected.value ?: return
        val staged = _stagedFile.value ?: return
        if (staged.sizeError != null) return
        if (_uploadState.value is Outcome.Loading) return // single-flight: block a double-tap duplicate upload
        _uploadState.value = Outcome.Loading
        launch {
            val result = runCatching {
                examPaperRepository.uploadSubmission(
                    datesheetSlotId = target.slot.id,
                    sessionId = target.datesheet.sessionId,
                    semester = target.datesheet.semester,
                    courseCode = target.slot.courseCode,
                    teacherId = teacherId,
                    fileBytes = staged.bytes,
                    fileName = fileName,
                    description = description,
                )
            }.fold(
                onSuccess = { Outcome.Success(Unit) },
                onFailure = { Outcome.Error(it.userMessageLogged("Upload failed."), it) },
            )
            // Deliberately NOT clearing stagedFile here: it and uploadState are set in the same
            // recomposition pass, so clearing it alongside a Success result would drop the "staged
            // file + success notice" screen before it ever renders. It clears naturally on the next
            // selectSlot()/stageFile() call instead.
            _uploadState.value = result
        }
    }

    fun deleteSubmission(submissionId: String) = launch {
        runCatching { examPaperRepository.deleteSubmission(submissionId) }
            .onFailure { _uploadState.value = Outcome.Error(it.userMessageLogged("Could not delete the submission."), it) }
    }

    fun downloadAndOpen(submission: ExamPaperSubmission, targetDir: File, opener: (File) -> Unit) = launch {
        runCatching {
            val file = examPaperRepository.downloadTo(submission, targetDir)
            opener(file)
        }.onFailure { _uploadState.value = Outcome.Error(it.userMessageLogged("Could not open the file."), it) }
    }
}
