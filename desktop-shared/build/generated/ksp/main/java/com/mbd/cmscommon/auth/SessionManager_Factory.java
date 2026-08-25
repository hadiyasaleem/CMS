package com.mbd.cmscommon.auth;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.auth.Auth;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SessionManager_Factory implements Factory<SessionManager> {
  private final Provider<Auth> authProvider;

  public SessionManager_Factory(Provider<Auth> authProvider) {
    this.authProvider = authProvider;
  }

  @Override
  public SessionManager get() {
    return newInstance(authProvider.get());
  }

  public static SessionManager_Factory create(Provider<Auth> authProvider) {
    return new SessionManager_Factory(authProvider);
  }

  public static SessionManager newInstance(Auth auth) {
    return new SessionManager(auth);
  }
}
