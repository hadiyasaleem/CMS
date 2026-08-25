package com.mbd.cmscommon.auth

import com.mbd.cmscommon.data.remote.SupabaseTables
import io.github.jan.supabase.functions.Functions
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import javax.inject.Inject
import kotlinx.serialization.Serializable

@Serializable
private data class CreateUserRequest(
    val email: String,
    val password: String,
    val role: String,
    val name: String? = null,
    val deptId: String? = null,
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
        val response = functions.invoke(SupabaseTables.FN_ADMIN_CREATE_USER) { setBody(body) }
        return response.body<CreateUserResponse>().uid
    }

    suspend fun createAdmin(email: String, password: String): String {
        val body = CreateUserRequest(email.normalizeEmail(), password, "ADMIN")
        val response = functions.invoke(SupabaseTables.FN_ADMIN_CREATE_USER) { setBody(body) }
        return response.body<CreateUserResponse>().uid
    }

    suspend fun setTeacherStatus(email: String, status: String) {
        val body = SetStatusRequest(email.normalizeEmail(), status)
        functions.invoke(SupabaseTables.FN_SET_TEACHER_STATUS) { setBody(body) }
    }
}
