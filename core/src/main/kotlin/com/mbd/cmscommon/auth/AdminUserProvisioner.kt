package com.mbd.cmscommon.auth

import com.mbd.cmscommon.data.remote.SupabaseTables
import io.github.jan.supabase.functions.Functions
import io.ktor.client.call.body
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// The edge function reads req.json() with plain JS destructuring (camelCase keys), but the
// client's global Json serializer rewrites every property to snake_case for Postgrest's sake --
// @SerialName pins deptId back to the literal camelCase the function expects.
@Serializable
private data class CreateUserRequest(
    val email: String,
    val password: String,
    val role: String,
    val name: String? = null,
    @SerialName("deptId") val deptId: String? = null,
    val designation: String? = null,
    val phone: String? = null,
)

@Serializable
private data class CreateUserResponse(
    val uid: String,
    val email: String,
    val role: String,
)

@Serializable
private data class SetStatusRequest(
    val email: String,
    val status: String,
)

@Serializable
private data class ResetPasswordRequest(
    val email: String,
    @SerialName("newPassword") val newPassword: String,
)

class AdminUserProvisioner @Inject constructor(
    private val functions: Functions,
) {
    suspend fun createTeacher(
        email: String,
        password: String,
        name: String,
        deptId: String?,
        designation: String?,
        phone: String?,
    ): String {
        val body = CreateUserRequest(email.normalizeEmail(), password, "TEACHER", name, deptId, designation, phone)
        val response = functions.invoke(SupabaseTables.FN_ADMIN_CREATE_USER, body)
        return response.body<CreateUserResponse>().uid
    }

    suspend fun createAdmin(email: String, password: String): String {
        val body = CreateUserRequest(email.normalizeEmail(), password, "ADMIN")
        val response = functions.invoke(SupabaseTables.FN_ADMIN_CREATE_USER, body)
        return response.body<CreateUserResponse>().uid
    }

    suspend fun setTeacherStatus(email: String, status: String) {
        val body = SetStatusRequest(email.normalizeEmail(), status)
        functions.invoke(SupabaseTables.FN_SET_TEACHER_STATUS, body)
    }

    suspend fun resetTeacherPassword(email: String, newPassword: String) {
        val body = ResetPasswordRequest(email.normalizeEmail(), newPassword)
        // The typed `invoke(function, body)` overload (reified) hands Ktor the compile-time type
        // so it can serialize correctly -- unlike `invoke(function) { setBody(body) }`, which loses
        // that type info inside the builder lambda and fails at runtime with a null Content-Type.
        functions.invoke(SupabaseTables.FN_RESET_TEACHER_PASSWORD, body)
    }
}
