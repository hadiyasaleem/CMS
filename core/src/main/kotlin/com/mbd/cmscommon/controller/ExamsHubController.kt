package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.ExamsHubSnapshot
import com.mbd.cmscommon.domain.model.examsHubSnapshot
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.supervisorScope

class ExamsHubController(
    private val teacherId: String,
    private val assignmentsProvider: TeacherAssignmentsProvider,
    private val examPaperRepository: ExamPaperSubmissionRepository,
    private val datesheetRepository: DatesheetRepository,
    scope: CoroutineScope,
    private val today: () -> LocalDate = { LocalDate.now() },
) : ScreenController(scope) {

    val assignments: StateFlow<List<ResolvedAssignment>> = assignmentsProvider.observeAssignmentsFor(teacherId)
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val submissions: StateFlow<List<ExamPaperSubmission>> = assignments
        .flatMapLatest { current ->
            val offerings = current.distinctBy { it.sessionId to it.courseCode }
            if (offerings.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(offerings.map { examPaperRepository.observeSubmissionsForOffering(it.sessionId, it.courseCode) }) { arr ->
                    arr.flatMap { it }.distinctBy { it.submissionId }
                }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _datesheets = MutableStateFlow<List<Datesheet>>(emptyList())
    private val _slots = MutableStateFlow<List<DatesheetSlot>>(emptyList())

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private var loadVersion = 0

    val snapshot: StateFlow<ExamsHubSnapshot> = combine(assignments, submissions, _datesheets, _slots) { classes, papers, datesheets, slots ->
        examsHubSnapshot(teacherId, classes, papers, datesheets, slots, today())
    }.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5000),
        examsHubSnapshot(teacherId, emptyList(), emptyList(), emptyList(), emptyList(), today()),
    )

    init {
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) {
        loadVersion++
        val version = loadVersion
        launch {
            _loading.value = true
            _loadError.value = null
            try {
                supervisorScope {
                    val currentAssignments = async { assignmentsProvider.observeAssignmentsFor(teacherId).first() }.await()
                    val datesheetsDeferred = async {
                        runCatching {
                            if (fetchRemote) datesheetRepository.sync()
                            datesheetRepository.getDatesheets()
                        }
                    }

                    val offerings = currentAssignments.distinctBy { it.sessionId to it.courseCode }
                    val paperResults = if (fetchRemote) {
                        offerings.map { assignment ->
                            async { runCatching { examPaperRepository.sync(assignment.sessionId, assignment.courseCode) } }
                        }.awaitAll()
                    } else {
                        emptyList()
                    }

                    val datesheetResult = datesheetsDeferred.await()
                    val sheets = datesheetResult.getOrDefault(emptyList())
                    if (version == loadVersion) _datesheets.value = sheets

                    val slotResults = sheets
                        .map { sheet ->
                            async {
                                runCatching {
                                    if (fetchRemote) datesheetRepository.syncSlots(sheet.id)
                                    datesheetRepository.getSlots(sheet.id)
                                }
                            }
                        }
                        .awaitAll()

                    if (version == loadVersion) {
                        _slots.value = slotResults.flatMap { it.getOrDefault(emptyList()) }
                        val firstFailure = (paperResults + datesheetResult + slotResults).firstNotNullOfOrNull { it.exceptionOrNull() }
                        _loadError.value = firstFailure?.userMessageLogged("Some exam data could not be loaded.")
                    }
                }
            } finally {
                _loading.value = false
            }
        }
    }
}
