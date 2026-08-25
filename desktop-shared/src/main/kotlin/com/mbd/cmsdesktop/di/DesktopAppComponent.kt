package com.mbd.cmsdesktop.di

import com.mbd.cmscommon.auth.AdminUserProvisioner
import com.mbd.cmscommon.auth.RoleResolver
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.sync.AdminDataBootstrapper
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.DocumentRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.domain.repository.InsightsRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import dagger.Component
import javax.inject.Singleton

/**
 * Plain-Dagger (not Hilt — Hilt requires Android) singleton graph for all 3 desktop apps. Each app's
 * `Main.kt` calls [DesktopAppComponent.create] once at startup and pulls whatever it needs off the
 * accessor methods below; screens/controllers are constructed manually from these (no
 * `hiltViewModel()` equivalent on desktop — see the "manual navigation" pattern in [[cmsdesktop-project]]).
 */
@Singleton
@Component(modules = [SupabaseModule::class, RepositoryModule::class])
interface DesktopAppComponent {

    fun sessionManager(): SessionManager
    fun roleResolver(): RoleResolver
    fun adminUserProvisioner(): AdminUserProvisioner
    fun teacherAssignmentsProvider(): TeacherAssignmentsProvider
    fun adminDataBootstrapper(): AdminDataBootstrapper
    fun bootstrapSnapshotStore(): BootstrapSnapshotStore

    fun userRepository(): UserRepository
    fun departmentRepository(): DepartmentRepository
    fun administratorRepository(): AdministratorRepository
    fun teacherRepository(): TeacherRepository
    fun academicSessionRepository(): AcademicSessionRepository
    fun curriculumRepository(): CurriculumRepository
    fun sessionAttendanceRepository(): SessionAttendanceRepository
    fun sessionMarksRepository(): SessionMarksRepository
    fun sessionTimetableRepository(): SessionTimetableRepository
    fun sessionFeeRepository(): SessionFeeRepository
    fun fineRepository(): FineRepository
    fun datesheetRepository(): DatesheetRepository
    fun calendarRepository(): CalendarRepository
    fun documentRepository(): DocumentRepository
    fun examPaperSubmissionRepository(): ExamPaperSubmissionRepository
    fun markEditRequestRepository(): MarkEditRequestRepository
    fun notificationRepository(): NotificationRepository
    fun studentLinkRequestRepository(): StudentLinkRequestRepository
    fun insightsRepository(): InsightsRepository

    companion object {
        fun create(): DesktopAppComponent = DaggerDesktopAppComponent.create()
    }
}
