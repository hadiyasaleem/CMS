package com.mbd.cmsdesktop.di

import com.mbd.cmscommon.data.repository.AdministratorRepositoryImpl
import com.mbd.cmscommon.data.repository.CalendarRepositoryImpl
import com.mbd.cmscommon.data.repository.DatesheetRepositoryImpl
import com.mbd.cmscommon.data.repository.FineRepositoryImpl
import com.mbd.cmscommon.data.repository.InsightsRepositoryImpl
import com.mbd.cmscommon.data.repository.MarkEditRequestRepositoryImpl
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
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
import com.mbd.cmsdesktop.data.repository.DesktopAcademicSessionRepositoryImpl
import com.mbd.cmsdesktop.data.repository.DesktopCurriculumRepositoryImpl
import com.mbd.cmsdesktop.data.repository.DesktopDepartmentRepositoryImpl
import com.mbd.cmsdesktop.data.repository.DesktopExamPaperSubmissionRepository
import com.mbd.cmsdesktop.data.repository.DesktopNotificationRepositoryImpl
import com.mbd.cmsdesktop.data.repository.DesktopSessionAttendanceRepositoryImpl
import com.mbd.cmsdesktop.data.repository.DesktopSessionFeeRepositoryImpl
import com.mbd.cmsdesktop.data.repository.DesktopSessionMarksRepositoryImpl
import com.mbd.cmsdesktop.data.repository.DesktopSessionTimetableRepositoryImpl
import com.mbd.cmsdesktop.data.repository.DesktopStudentLinkRequestRepositoryImpl
import com.mbd.cmsdesktop.data.repository.DesktopTeacherRepositoryImpl
import com.mbd.cmsdesktop.data.repository.DesktopUserRepositoryImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/**
 * 6 of the 18 domain repositories are pure-Postgrest and reused as-is from `:core`
 * ([AdministratorRepositoryImpl], [CalendarRepositoryImpl], [DatesheetRepositoryImpl],
 * [FineRepositoryImpl], [InsightsRepositoryImpl], [MarkEditRequestRepositoryImpl]) — no Room, so
 * nothing platform-specific about them. The other 12 need desktop-specific caching/bootstrap
 * behavior and are bound to `Desktop*` classes in `data/repository`.
 */
@Module
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindDatesheetRepository(impl: DatesheetRepositoryImpl): DatesheetRepository

    @Singleton
    @Binds
    abstract fun bindInsightsRepository(impl: InsightsRepositoryImpl): InsightsRepository

    @Singleton
    @Binds
    abstract fun bindUserRepository(impl: DesktopUserRepositoryImpl): UserRepository

    @Singleton
    @Binds
    abstract fun bindAdministratorRepository(impl: AdministratorRepositoryImpl): AdministratorRepository

    @Singleton
    @Binds
    abstract fun bindNotificationRepository(impl: DesktopNotificationRepositoryImpl): NotificationRepository

    @Singleton
    @Binds
    abstract fun bindTeacherRepository(impl: DesktopTeacherRepositoryImpl): TeacherRepository

    @Singleton
    @Binds
    abstract fun bindDepartmentRepository(impl: DesktopDepartmentRepositoryImpl): DepartmentRepository

    @Singleton
    @Binds
    abstract fun bindMarkEditRequestRepository(impl: MarkEditRequestRepositoryImpl): MarkEditRequestRepository

    @Singleton
    @Binds
    abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository

    @Singleton
    @Binds
    abstract fun bindAcademicSessionRepository(impl: DesktopAcademicSessionRepositoryImpl): AcademicSessionRepository

    @Singleton
    @Binds
    abstract fun bindCurriculumRepository(impl: DesktopCurriculumRepositoryImpl): CurriculumRepository

    @Singleton
    @Binds
    abstract fun bindSessionFeeRepository(impl: DesktopSessionFeeRepositoryImpl): SessionFeeRepository

    @Singleton
    @Binds
    abstract fun bindSessionTimetableRepository(impl: DesktopSessionTimetableRepositoryImpl): SessionTimetableRepository

    @Singleton
    @Binds
    abstract fun bindStudentLinkRequestRepository(impl: DesktopStudentLinkRequestRepositoryImpl): StudentLinkRequestRepository

    @Singleton
    @Binds
    abstract fun bindSessionAttendanceRepository(impl: DesktopSessionAttendanceRepositoryImpl): SessionAttendanceRepository

    @Singleton
    @Binds
    abstract fun bindSessionMarksRepository(impl: DesktopSessionMarksRepositoryImpl): SessionMarksRepository

    @Singleton
    @Binds
    abstract fun bindFineRepository(impl: FineRepositoryImpl): FineRepository

    @Singleton
    @Binds
    abstract fun bindExamPaperSubmissionRepository(impl: DesktopExamPaperSubmissionRepository): ExamPaperSubmissionRepository
}
