package com.mbd.cmsdesktop.di

import com.mbd.cmscommon.data.repository.DesktopAdministratorRepository
import com.mbd.cmscommon.data.repository.DesktopCalendarRepository
import com.mbd.cmscommon.data.repository.DesktopDatesheetRepository
import com.mbd.cmscommon.data.repository.DesktopFineRepository
import com.mbd.cmscommon.data.repository.DesktopInsightsRepository
import com.mbd.cmscommon.data.repository.DesktopMarkEditRequestRepository
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

/** Desktop repository bindings use durable/cache-first adapters for all read-heavy screens. */
@Module
abstract class RepositoryModule {
    @Singleton @Binds abstract fun bindDatesheetRepository(impl: DesktopDatesheetRepository): DatesheetRepository
    @Singleton @Binds abstract fun bindInsightsRepository(impl: DesktopInsightsRepository): InsightsRepository
    @Singleton @Binds abstract fun bindUserRepository(impl: DesktopUserRepositoryImpl): UserRepository
    @Singleton @Binds abstract fun bindAdministratorRepository(impl: DesktopAdministratorRepository): AdministratorRepository
    @Singleton @Binds abstract fun bindNotificationRepository(impl: DesktopNotificationRepositoryImpl): NotificationRepository
    @Singleton @Binds abstract fun bindTeacherRepository(impl: DesktopTeacherRepositoryImpl): TeacherRepository
    @Singleton @Binds abstract fun bindDepartmentRepository(impl: DesktopDepartmentRepositoryImpl): DepartmentRepository
    @Singleton @Binds abstract fun bindMarkEditRequestRepository(impl: DesktopMarkEditRequestRepository): MarkEditRequestRepository
    @Singleton @Binds abstract fun bindCalendarRepository(impl: DesktopCalendarRepository): CalendarRepository
    @Singleton @Binds abstract fun bindAcademicSessionRepository(impl: DesktopAcademicSessionRepositoryImpl): AcademicSessionRepository
    @Singleton @Binds abstract fun bindCurriculumRepository(impl: DesktopCurriculumRepositoryImpl): CurriculumRepository
    @Singleton @Binds abstract fun bindSessionFeeRepository(impl: DesktopSessionFeeRepositoryImpl): SessionFeeRepository
    @Singleton @Binds abstract fun bindSessionTimetableRepository(impl: DesktopSessionTimetableRepositoryImpl): SessionTimetableRepository
    @Singleton @Binds abstract fun bindStudentLinkRequestRepository(impl: DesktopStudentLinkRequestRepositoryImpl): StudentLinkRequestRepository
    @Singleton @Binds abstract fun bindSessionAttendanceRepository(impl: DesktopSessionAttendanceRepositoryImpl): SessionAttendanceRepository
    @Singleton @Binds abstract fun bindSessionMarksRepository(impl: DesktopSessionMarksRepositoryImpl): SessionMarksRepository
    @Singleton @Binds abstract fun bindFineRepository(impl: DesktopFineRepository): FineRepository
    @Singleton @Binds abstract fun bindExamPaperSubmissionRepository(impl: DesktopExamPaperSubmissionRepository): ExamPaperSubmissionRepository
}
