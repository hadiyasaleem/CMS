package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.requireValid
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.domain.model.linkRequestClaimQuality
import com.mbd.cmscommon.domain.model.linkRequestQueueSnapshot
import com.mbd.cmscommon.domain.model.linkRequestVerificationKey
import com.mbd.cmscommon.domain.model.verifyLinkIdentityClaims
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class LinkRequestsController(
    private val repository: StudentLinkRequestRepository,
    private val sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    private val reviewerId: String,
    private val permissionCheck: (suspend () -> Boolean)? = null,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val requests: StateFlow<List<StudentLinkRequest>> = repository.observePendingRequests()
        .map { linkRequestQueueSnapshot(it).requests }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val verifications: StateFlow<Map<String, LinkRequestVerification>> = requests
        .map { queue ->
            coroutineScope {
                queue.map { request -> async { verifyRequest(request) } }.awaitAll().toMap()
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _access = MutableStateFlow(if (permissionCheck == null) LinkRequestAccess.GRANTED else LinkRequestAccess.CHECKING)
    val access: StateFlow<LinkRequestAccess> = _access.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _busyRequestId = MutableStateFlow<String?>(null)
    val busyRequestId: StateFlow<String?> = _busyRequestId.asStateFlow()

    private val _rowErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val rowErrors: StateFlow<Map<String, String>> = _rowErrors.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    init {
        if (permissionCheck == null) {
            _loading.value = false
        } else {
            launch {
                try {
                    _access.value = if (permissionCheck()) LinkRequestAccess.GRANTED else LinkRequestAccess.DENIED
                    _loading.value = false
                } finally {
                    if (_access.value == LinkRequestAccess.CHECKING) _access.value = LinkRequestAccess.DENIED
                    if (_access.value == LinkRequestAccess.DENIED) _loading.value = false
                }
            }
        }
    }

    fun refresh() = launch {
        if (_access.value != LinkRequestAccess.GRANTED) return@launch
        _loading.value = true
        try {
            repository.sync()
        } finally {
            _loading.value = false
        }
    }

    fun approve(request: StudentLinkRequest) = launch {
        val requestKey = linkRequestVerificationKey(request)
        try {
            _busyRequestId.value = requestKey
            _notice.value = null
            requireValid(reviewerId.isNotBlank()) { "Your signed-in account could not be identified." }

            val quality = linkRequestClaimQuality(request)
            requireValid(quality.isReviewable) { quality.summary ?: "This request cannot be reviewed safely." }
            requireValid(requests.value.any { it.requestId == request.requestId }) {
                "This request is no longer pending. Refresh the queue."
            }

            val verification = verifications.value[requestKey]
            requireValid(verification?.state == RosterVerificationState.MATCHED || verification?.state == RosterVerificationState.RELINK) {
                "Verify that this student exists in the selected session before approval."
            }

            val sessionId = request.sessionIdClaimed?.trim().orEmpty()
            val currentProfile = sessionRepository.getStudentProfile(sessionId, request.rollNumberClaimed.trim())
                ?: throw CmsException.NotFound("The official student profile could not be loaded. Refresh before approval.")

            val currentIdentity = verifyLinkIdentityClaims(request, currentProfile)
            requireValid(!currentIdentity.hasMismatch) {
                val fields = currentIdentity.mismatches.joinToString { it.field.label }
                "Official student details conflict with the request: $fields."
            }

            repository.approveRequest(request.requestId, reviewerId)
            _rowErrors.value = _rowErrors.value - requestKey
            _notice.value = "${request.nameClaimed ?: request.rollNumberClaimed} was linked successfully."
        } catch (t: Throwable) {
            _rowErrors.value = _rowErrors.value + (requestKey to t.userMessageLogged("Could not approve the request."))
        } finally {
            _busyRequestId.value = null
        }
    }

    fun reject(request: StudentLinkRequest, reason: String) = launch {
        val requestKey = linkRequestVerificationKey(request)
        try {
            _busyRequestId.value = requestKey
            _notice.value = null
            requireValid(reviewerId.isNotBlank()) { "Your signed-in account could not be identified." }
            requireValid(request.requestId.isNotBlank()) { "This request has no database ID and cannot be rejected safely." }
            requireValid(requests.value.any { it.requestId == request.requestId }) {
                "This request is no longer pending. Refresh the queue."
            }
            val normalizedReason = reason.trim()
            requireValid(normalizedReason.length >= 4) { "Add a short reason so the student knows what to correct." }
            requireValid(normalizedReason.length <= 500) { "Keep the rejection reason within 500 characters." }

            repository.rejectRequest(request.requestId, reviewerId, normalizedReason)
            _rowErrors.value = _rowErrors.value - requestKey
            _notice.value = "${request.nameClaimed ?: request.rollNumberClaimed}'s request was rejected with guidance."
        } catch (t: Throwable) {
            _rowErrors.value = _rowErrors.value + (requestKey to t.userMessageLogged("Could not reject the request."))
        } finally {
            _busyRequestId.value = null
        }
    }

    fun consumeNotice() {
        _notice.value = null
    }

    private suspend fun verifyRequest(request: StudentLinkRequest): Pair<String, LinkRequestVerification> {
        val key = linkRequestVerificationKey(request)
        val claimIssue = linkRequestClaimQuality(request).summary
        if (claimIssue != null) {
            return key to LinkRequestVerification(RosterVerificationState.FAILED, message = claimIssue)
        }
        return try {
            val match = repository.rosterLinkMatch(request.sessionIdClaimed, request.rollNumberClaimed)
            val verification = if (!match.exists) {
                LinkRequestVerification(RosterVerificationState.MISSING)
            } else {
                val sessionId = request.sessionIdClaimed?.trim().orEmpty()
                val profile = sessionRepository.getStudentProfile(sessionId, request.rollNumberClaimed.trim())
                if (profile == null) {
                    LinkRequestVerification(
                        RosterVerificationState.FAILED,
                        match.linkedEmail,
                        "The roster record exists, but its official profile could not be loaded.",
                    )
                } else {
                    val identity = verifyLinkIdentityClaims(request, profile)
                    if (identity.hasMismatch) {
                        LinkRequestVerification(RosterVerificationState.IDENTITY_MISMATCH, match.linkedEmail, identityComparisons = identity.comparisons)
                    } else {
                        val alreadyLinked = !match.linkedEmail.isNullOrBlank()
                        val state = if (!alreadyLinked || match.linkedEmail.equals(request.requestedByUid, ignoreCase = true)) {
                            RosterVerificationState.MATCHED
                        } else {
                            RosterVerificationState.RELINK
                        }
                        LinkRequestVerification(state, match.linkedEmail, identityComparisons = identity.comparisons)
                    }
                }
            }
            key to verification
        } catch (t: Throwable) {
            key to LinkRequestVerification(RosterVerificationState.FAILED, message = t.userMessageLogged("Could not verify the roster record."))
        }
    }
}
