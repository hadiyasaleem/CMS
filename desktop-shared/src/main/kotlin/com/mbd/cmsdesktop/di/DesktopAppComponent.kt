package com.mbd.cmsdesktop.di

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.sync.AdminDataBootstrapper
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.domain.repository.InsightsRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmsdesktop.auth.DesktopRoleResolver
import dagger.Component
import io.github.jan.supabase.auth.Auth
import javax.inject.Singleton

/**
 * Plain-Dagger (not Hilt — Hilt requires Android) singleton graph for all 3 desktop apps. Each app's
 * `Main.kt` calls [DesktopAppComponent.create] once at startup and pulls whatever it needs off the
 * accessor methods below; screens/controllers are constructed manually from these (no
 * `hiltViewModel()` equivalent on desktop — see the "manual navigation" pattern in [[cmsdesktop-project]]).
 */
@Singleton
@Component(modules = [SupabaseModule::class, RepositoryModule::class, DesktopRoomModule::class])
interface DesktopAppComponent {

    fun adminDataBootstrapper(): AdminDataBootstrapper
    fun auth(): Auth
    fun sessionManager(): SessionManager
    fun roleResolver(): DesktopRoleResolver

    fun datesheetRepository(): DatesheetRepository
    fun userRepository(): UserRepository
    fun administratorRepository(): AdministratorRepository
    fun notificationRepository(): NotificationRepository
    fun insightsRepository(): InsightsRepository
    fun examPaperRepository(): ExamPaperSubmissionRepository
    fun teacherRepository(): TeacherRepository
    fun departmentRepository(): DepartmentRepository
    fun buildingRepository(): BuildingRepository
    fun roomRepository(): RoomRepository
    fun markEditRequestRepository(): MarkEditRequestRepository
    fun calendarRepository(): CalendarRepository
    fun academicSessionRepository(): AcademicSessionRepository
    fun curriculumRepository(): CurriculumRepository
    fun sessionFeeRepository(): SessionFeeRepository
    fun sessionTimetableRepository(): SessionTimetableRepository
    fun studentLinkRequestRepository(): StudentLinkRequestRepository
    fun sessionAttendanceRepository(): SessionAttendanceRepository
    fun sessionMarksRepository(): SessionMarksRepository
    fun fineRepository(): FineRepository

    companion object {
        fun create(): DesktopAppComponent = DaggerDesktopAppComponent.create()
    }
}
