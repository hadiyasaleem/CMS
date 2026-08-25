package com.mbd.cmscommon.data.repository;

import com.mbd.cmscommon.data.local.dao.InsightsDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.postgrest.Postgrest;
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
public final class InsightsRepositoryLocalImpl_Factory implements Factory<InsightsRepositoryLocalImpl> {
  private final Provider<Postgrest> postgrestProvider;

  private final Provider<InsightsDao> insightsDaoProvider;

  public InsightsRepositoryLocalImpl_Factory(Provider<Postgrest> postgrestProvider,
      Provider<InsightsDao> insightsDaoProvider) {
    this.postgrestProvider = postgrestProvider;
    this.insightsDaoProvider = insightsDaoProvider;
  }

  @Override
  public InsightsRepositoryLocalImpl get() {
    return newInstance(postgrestProvider.get(), insightsDaoProvider.get());
  }

  public static InsightsRepositoryLocalImpl_Factory create(Provider<Postgrest> postgrestProvider,
      Provider<InsightsDao> insightsDaoProvider) {
    return new InsightsRepositoryLocalImpl_Factory(postgrestProvider, insightsDaoProvider);
  }

  public static InsightsRepositoryLocalImpl newInstance(Postgrest postgrest,
      InsightsDao insightsDao) {
    return new InsightsRepositoryLocalImpl(postgrest, insightsDao);
  }
}
