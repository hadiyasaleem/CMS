package com.mbd.cmscommon.di

import com.mbd.cmscommon.data.repository.AcademicSessionRepositoryImpl
import com.mbd.cmscommon.data.repository.AdministratorRepositoryLocalImpl
import com.mbd.cmscommon.data.repository.AppLogRepositoryImpl
import com.mbd.cmscommon.data.repository.BuildingRepositoryImpl
import com.mbd.cmscommon.data.repository.CalendarRepositoryLocalImpl
import com.mbd.cmscommon.data.repository.CurriculumRepositoryImpl
import com.mbd.cmscommon.data.repository.DatesheetRepositoryLocalImpl
import com.mbd.cmscommon.data.repository.DepartmentRepositoryImpl
import com.mbd.cmscommon.data.repository.ExamPaperSubmissionRepositoryImpl
import com.mbd.cmscommon.data.repository.FineRepositoryLocalImpl
import com.mbd.cmscommon.data.repository.InsightsRepositoryLocalImpl
import com.mbd.cmscommon.data.repository.MarkEditRequestRepositoryLocalImpl
import com.mbd.cmscommon.data.repository.NotificationRepositoryImpl
import com.mbd.cmscommon.data.repository.RoomRepositoryImpl
import com.mbd.cmscommon.data.repository.SessionAttendanceRepositoryImpl
import com.mbd.cmscommon.data.repository.SessionFeeRepositoryImpl
import com.mbd.cmscommon.data.repository.SessionMarksRepositoryImpl
import com.mbd.cmscommon.data.repository.SessionTimetableRepositoryImpl
import com.mbd.cmscommon.data.repository.StudentLinkRequestRepositoryImpl
import com.mbd.cmscommon.data.repository.RoomLogSink
import com.mbd.cmscommon.data.repository.TeacherRepositoryImpl
import com.mbd.cmscommon.data.repository.UserRepositoryImpl
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.domain.repository.AppLogRepository
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
import com.mbd.cmscommon.util.LogSink
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindAcademicSessionRepository(impl: AcademicSessionRepositoryImpl): AcademicSessionRepository

    @Singleton
    @Binds
    abstract fun bindAdministratorRepository(impl: AdministratorRepositoryLocalImpl): AdministratorRepository

    @Singleton
    @Binds
    abstract fun bindCalendarRepository(impl: CalendarRepositoryLocalImpl): CalendarRepository

    @Singleton
    @Binds
    abstract fun bindCurriculumRepository(impl: CurriculumRepositoryImpl): CurriculumRepository

    @Singleton
    @Binds
    abstract fun bindDatesheetRepository(impl: DatesheetRepositoryLocalImpl): DatesheetRepository

    @Singleton
    @Binds
    abstract fun bindDepartmentRepository(impl: DepartmentRepositoryImpl): DepartmentRepository

    @Singleton
    @Binds
    abstract fun bindBuildingRepository(impl: BuildingRepositoryImpl): BuildingRepository

    @Singleton
    @Binds
    abstract fun bindRoomRepository(impl: RoomRepositoryImpl): RoomRepository

    @Singleton
    @Binds
    abstract fun bindExamPaperSubmissionRepository(impl: ExamPaperSubmissionRepositoryImpl): ExamPaperSubmissionRepository

    @Singleton
    @Binds
    abstract fun bindFineRepository(impl: FineRepositoryLocalImpl): FineRepository

    @Singleton
    @Binds
    abstract fun bindInsightsRepository(impl: InsightsRepositoryLocalImpl): InsightsRepository

    @Singleton
    @Binds
    abstract fun bindMarkEditRequestRepository(impl: MarkEditRequestRepositoryLocalImpl): MarkEditRequestRepository

    @Singleton
    @Binds
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Singleton
    @Binds
    abstract fun bindSessionAttendanceRepository(impl: SessionAttendanceRepositoryImpl): SessionAttendanceRepository

    @Singleton
    @Binds
    abstract fun bindSessionFeeRepository(impl: SessionFeeRepositoryImpl): SessionFeeRepository

    @Singleton
    @Binds
    abstract fun bindSessionMarksRepository(impl: SessionMarksRepositoryImpl): SessionMarksRepository

    @Singleton
    @Binds
    abstract fun bindSessionTimetableRepository(impl: SessionTimetableRepositoryImpl): SessionTimetableRepository

    @Singleton
    @Binds
    abstract fun bindStudentLinkRequestRepository(impl: StudentLinkRequestRepositoryImpl): StudentLinkRequestRepository

    @Singleton
    @Binds
    abstract fun bindTeacherRepository(impl: TeacherRepositoryImpl): TeacherRepository

    @Singleton
    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Singleton
    @Binds
    abstract fun bindAppLogRepository(impl: AppLogRepositoryImpl): AppLogRepository

    @Singleton
    @Binds
    abstract fun bindLogSink(impl: RoomLogSink): LogSink
}
