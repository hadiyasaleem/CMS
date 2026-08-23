package com.mbd.cmsstudent.feature.auth;

import com.mbd.cmscommon.auth.SessionManager;
import com.mbd.cmscommon.domain.repository.UserRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<SessionManager> sessionManagerProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  public AuthViewModel_Factory(Provider<SessionManager> sessionManagerProvider,
      Provider<UserRepository> userRepositoryProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(sessionManagerProvider.get(), userRepositoryProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<SessionManager> sessionManagerProvider,
      Provider<UserRepository> userRepositoryProvider) {
    return new AuthViewModel_Factory(sessionManagerProvider, userRepositoryProvider);
  }

  public static AuthViewModel newInstance(SessionManager sessionManager,
      UserRepository userRepository) {
    return new AuthViewModel(sessionManager, userRepository);
  }
}
