# Appendix B — General Coding Standards and Guidelines

The codebase follows a small set of conventions consistently, and they are worth stating as they actually appear in the source rather than as generic best-practice. The whole project is Kotlin under a single `com.mbd.*` package root — `cmscommon` for the shared code, and `cmsadmin`/`cmsteacher`/`cmsstudent` (with their desktop counterparts) for the apps. The architecture is layered MVVM with a repository boundary: the domain model, DTOs, and repositories live in the shared modules, screens observe view-models or controllers, and nothing in the UI reaches the database directly. Access control is deliberately not a coding standard the UI enforces — it is enforced by Row-Level Security in the database, so a screen that forgets a check still cannot read rows the role does not own.

Naming is regular enough to navigate by: types are PascalCase, members camelCase, and files carry their role as a suffix — `*Screen`, `*ViewModel`, `*Controller`, `*Repository`/`*RepositoryImpl`, `*Dao`, `*Dto`. Asynchronous work uses coroutines and `Flow` rather than callbacks, DTOs are `kotlinx.serialization` classes, local storage is Room entities and DAOs, and the UI is Jetpack Compose sharing one theme (`ModernistColors` in `Theme.kt`) across every app so the six look like one product.

**Table B-1 — Coding standards in force**

| Area | Standard | Example from the codebase |
|---|---|---|
| Package layout | One `com.mbd.*` root; shared code in `cmscommon`, apps in `cms{admin,teacher,student}` + desktop variants | `com.mbd.cmscommon.controller`, `com.mbd.cmsadmin.feature.*` |
| Architecture | Layered MVVM + repository; domain/DTOs/repos in shared modules; UI never hits the DB directly | `core` domain + repositories, per-feature `*ViewModel`/`*Controller` |
| Naming | PascalCase types, camelCase members, role suffix on files | `MarksEntryController`, `TeacherRepositoryImpl`, `SessionStudentsScreen` |
| Dependency injection | Hilt on Android, its Dagger core on desktop | `@HiltViewModel` on Android view-models; `@Inject` constructors throughout |
| Asynchronous code | Coroutines + `Flow`/`StateFlow`, `suspend` functions — no callback chains | repositories expose `suspend fun`; view-models expose `StateFlow` |
| Data transfer | `kotlinx.serialization` DTOs for the PostgREST JSON | `@Serializable` DTO classes in `core` |
| Local storage | Room entities and DAOs behind the repositories | `@Entity`/`@Dao` types feeding the offline cache |
| UI | Jetpack Compose with one shared theme across all six apps | `@Composable` screens; `ModernistColors` in `Theme.kt` |
| Security | Enforced by Row-Level Security in the database, not by the client alone | RLS policies per table; the UI relies on, not replaces, them |
