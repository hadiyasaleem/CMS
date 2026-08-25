package com.mbd.cmsdesktop.di

import com.mbd.cmscommon.data.repository.DesktopAcademicSessionRepository
import com.mbd.cmscommon.data.repository.DesktopAdministratorRepository
import com.mbd.cmscommon.data.repository.DesktopCalendarRepository
import com.mbd.cmscommon.data.repository.DesktopCurriculumRepository
import com.mbd.cmscommon.data.repository.DesktopDatesheetRepository
import com.mbd.cmscommon.data.repository.DesktopDepartmentRepository
import com.mbd.cmscommon.data.repository.DesktopDocumentRepository
import com.mbd.cmscommon.data.repository.DesktopExamPaperSubmissionRepository
import com.mbd.cmscommon.data.repository.DesktopFineRepository
import com.mbd.cmscommon.data.repository.DesktopInsightsRepository
import com.mbd.cmscommon.data.repository.DesktopMarkEditRequestRepository
import com.mbd.cmscommon.data.repository.DesktopNotificationRepository
import com.mbd.cmscommon.data.repository.DesktopSessionAttendanceRepository
import com.mbd.cmscommon.data.repository.DesktopSessionFeeRepository
import com.mbd.cmscommon.data.repository.DesktopSessionMarksRepository
import com.mbd.cmscommon.data.repository.DesktopSessionTimetableRepository
import com.mbd.cmscommon.data.repository.DesktopStudentLinkRequestRepository
import com.mbd.cmscommon.data.repository.DesktopTeacherRepository
import com.mbd.cmscommon.data.repository.DesktopUserRepository
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
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindDepartmentRepository(impl: DesktopDepartmentRepository): DepartmentRepository
    @Binds @Singleton abstract fun bindUserRepository(impl: DesktopUserRepository): UserRepository
    @Binds @Singleton abstract fun bindAdministratorRepository(impl: DesktopAdministratorRepository): AdministratorRepository
    @Binds @Singleton abstract fun bindCalendarRepository(impl: DesktopCalendarRepository): CalendarRepository
    @Binds @Singleton abstract fun bindDatesheetRepository(impl: DesktopDatesheetRepository): DatesheetRepository
    @Binds @Singleton abstract fun bindDocumentRepository(impl: DesktopDocumentRepository): DocumentRepository
    @Binds @Singleton abstract fun bindExamPaperSubmissionRepository(impl: DesktopExamPaperSubmissionRepository): ExamPaperSubmissionRepository
    @Binds @Singleton abstract fun bindFineRepository(impl: DesktopFineRepository): FineRepository
    @Binds @Singleton abstract fun bindInsightsRepository(impl: DesktopInsightsRepository): InsightsRepository
    @Binds @Singleton abstract fun bindMarkEditRequestRepository(impl: DesktopMarkEditRequestRepository): MarkEditRequestRepository
    @Binds @Singleton abstract fun bindNotificationRepository(impl: DesktopNotificationRepository): NotificationRepository
    @Binds @Singleton abstract fun bindStudentLinkRequestRepository(impl: DesktopStudentLinkRequestRepository): StudentLinkRequestRepository
    @Binds @Singleton abstract fun bindTeacherRepository(impl: DesktopTeacherRepository): TeacherRepository
    @Binds @Singleton abstract fun bindAcademicSessionRepository(impl: DesktopAcademicSessionRepository): AcademicSessionRepository
    @Binds @Singleton abstract fun bindCurriculumRepository(impl: DesktopCurriculumRepository): CurriculumRepository
    @Binds @Singleton abstract fun bindSessionAttendanceRepository(impl: DesktopSessionAttendanceRepository): SessionAttendanceRepository
    @Binds @Singleton abstract fun bindSessionMarksRepository(impl: DesktopSessionMarksRepository): SessionMarksRepository
    @Binds @Singleton abstract fun bindSessionTimetableRepository(impl: DesktopSessionTimetableRepository): SessionTimetableRepository
    @Binds @Singleton abstract fun bindSessionFeeRepository(impl: DesktopSessionFeeRepository): SessionFeeRepository
}
