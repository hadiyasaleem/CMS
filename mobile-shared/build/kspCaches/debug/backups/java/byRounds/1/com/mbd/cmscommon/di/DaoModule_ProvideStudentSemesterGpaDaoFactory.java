package com.mbd.cmscommon.di;

import com.mbd.cmscommon.data.local.CmsDatabase;
import com.mbd.cmscommon.data.local.dao.StudentSemesterGpaDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class DaoModule_ProvideStudentSemesterGpaDaoFactory implements Factory<StudentSemesterGpaDao> {
  private final Provider<CmsDatabase> dbProvider;

  public DaoModule_ProvideStudentSemesterGpaDaoFactory(Provider<CmsDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public StudentSemesterGpaDao get() {
    return provideStudentSemesterGpaDao(dbProvider.get());
  }

  public static DaoModule_ProvideStudentSemesterGpaDaoFactory create(
      Provider<CmsDatabase> dbProvider) {
    return new DaoModule_ProvideStudentSemesterGpaDaoFactory(dbProvider);
  }

  public static StudentSemesterGpaDao provideStudentSemesterGpaDao(CmsDatabase db) {
    return Preconditions.checkNotNullFromProvides(DaoModule.INSTANCE.provideStudentSemesterGpaDao(db));
  }
}
