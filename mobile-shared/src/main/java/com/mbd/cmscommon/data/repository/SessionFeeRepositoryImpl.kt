package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.requireAffected
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.CollegeFeeDao
import com.mbd.cmscommon.data.local.dao.SessionFeeDao
import com.mbd.cmscommon.data.mapper.SessionFeeMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.CollegeFeeDto
import com.mbd.cmscommon.data.remote.dto.CollegeFeeHeadDto
import com.mbd.cmscommon.data.remote.dto.SessionFeeDto
import com.mbd.cmscommon.data.remote.dto.SessionFeeHeadDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class SessionFeeRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val feeDao: SessionFeeDao,
    private val collegeDao: CollegeFeeDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : SessionFeeRepository {
    private fun syncOwnerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    override suspend fun getSessionFee(sessionId: String, shift: Session): SessionFeeStructure? {
        val fee = feeDao.getFee(sessionId, shift.name)
        if (fee != null && !fee.isDeleted) return SessionFeeMapper.toDomain(fee, feeDao.getHeads(sessionId, shift.name))
        // No structure of its own: the session pays the college-wide base for its shift.
        return getCollegeFee(shift)?.copy(sessionId = sessionId, inherited = true)
    }

    override suspend fun getAllSessionFees(): List<SessionFeeStructure> =
        feeDao.getAllFees().map { fee -> SessionFeeMapper.toDomain(fee, feeDao.getHeads(fee.sessionId, fee.shift)) }

    override suspend fun removeSessionFee(sessionId: String, shift: Session, updatedBy: String) {
        postgrest.from(SupabaseTables.SESSION_FEES).update({
            set("is_deleted", true)
            set("updated_by", updatedBy)
        }) {
            select()
            filter {
                eq("session_id", sessionId)
                eq("shift", shift.name)
            }
        }.requireAffected(onNone = {
            feeDao.deleteFee(sessionId, shift.name)
            feeDao.deleteHeadsFor(sessionId, shift.name)
        })
        postgrest.from(SupabaseTables.SESSION_FEE_HEADS).update({
            set("is_deleted", true)
            set("updated_by", updatedBy)
        }) {
            filter {
                eq("session_id", sessionId)
                eq("shift", shift.name)
            }
        }
        feeDao.deleteFee(sessionId, shift.name)
        feeDao.deleteHeadsFor(sessionId, shift.name)
    }

    override suspend fun getCollegeFee(shift: Session): SessionFeeStructure? {
        val fee = collegeDao.getFee(shift.name) ?: return null
        return SessionFeeMapper.collegeToDomain(fee, collegeDao.getHeads(shift.name))
    }

    override suspend fun getCollegeFees(): List<SessionFeeStructure> =
        collegeDao.getFees().map { fee -> SessionFeeMapper.collegeToDomain(fee, collegeDao.getHeads(fee.shift)) }

    override suspend fun saveCollegeFee(structure: SessionFeeStructure, updatedBy: String) {
        if (!(structure.heads.all { it.label.trim().isNotBlank() })) throw CmsException.Validation("Every fee head needs a label.")
        if (!(structure.heads.all { it.amount > 0.0 })) throw CmsException.Validation("Every fee amount must be greater than zero.")

        val shift = structure.shift.name
        val feeDto = CollegeFeeDto(
            shift = shift,
            cadence = structure.cadence.name,
            academicYear = structure.academicYear,
            dueDate = structure.dueDate,
            paymentNote = structure.paymentNote,
            updatedBy = updatedBy,
        )
        postgrest.from(SupabaseTables.COLLEGE_FEES).upsert(feeDto) { onConflict = "shift" }

        postgrest.from(SupabaseTables.COLLEGE_FEE_HEADS).update({
            set("is_deleted", true)
            set("updated_by", updatedBy)
        }) {
            filter { eq("shift", shift) }
        }
        val heads = structure.heads.mapIndexed { index, head ->
            CollegeFeeHeadDto(shift = shift, label = head.label, amount = head.amount, position = index, updatedBy = updatedBy, isDeleted = false)
        }
        if (heads.isNotEmpty()) {
            postgrest.from(SupabaseTables.COLLEGE_FEE_HEADS).upsert(heads) { onConflict = "shift,label" }
        }

        val now = System.currentTimeMillis()
        collegeDao.applyFeeDelta(listOf(SessionFeeMapper.collegeFeeDtoToEntity(feeDto).copy(createdAt = now, updatedAt = now)), emptyList())
        collegeDao.deleteHeadsFor(shift)
        collegeDao.applyHeadDelta(heads.map { SessionFeeMapper.collegeHeadDtoToEntity(it).copy(createdAt = now, updatedAt = now) }, emptyList())
    }

    /** Pulls the college-wide base (every signed-in role may read it: students need it for their challan). */
    private suspend fun syncCollege() {
        val owner = syncOwnerKey()
        val scope = SyncCheckpointDefaults.globalScope()
        fetchIncrementalDelta(
            checkpointStore = checkpointStore,
            ownerKey = owner,
            tableName = SupabaseTables.COLLEGE_FEES,
            scopeKey = scope,
            updatedAtOf = CollegeFeeDto::updatedAt,
            applyDelta = { delta ->
                val entities = delta.map(SessionFeeMapper::collegeFeeDtoToEntity)
                val (deleted, active) = entities.partition { it.isDeleted }
                collegeDao.applyFeeDelta(active, deleted.map { it.shift })
            },
        ) { since, from, to ->
            postgrest.from(SupabaseTables.COLLEGE_FEES).select {
                filter { gt("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        fetchIncrementalDelta(
            checkpointStore = checkpointStore,
            ownerKey = owner,
            tableName = SupabaseTables.COLLEGE_FEE_HEADS,
            scopeKey = scope,
            updatedAtOf = CollegeFeeHeadDto::updatedAt,
            applyDelta = { delta ->
                val entities = delta.map(SessionFeeMapper::collegeHeadDtoToEntity)
                val (deleted, active) = entities.partition { it.isDeleted }
                collegeDao.applyHeadDelta(active, deleted.map { it.id })
            },
        ) { since, from, to ->
            postgrest.from(SupabaseTables.COLLEGE_FEE_HEADS).select {
                filter { gt("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
    }

    override suspend fun getSessionFees(sessionId: String): List<SessionFeeStructure> =
        feeDao.getFees(sessionId).map { fee -> SessionFeeMapper.toDomain(fee, feeDao.getHeads(sessionId, fee.shift)) }

    override suspend fun saveSessionFee(structure: SessionFeeStructure, updatedBy: String) {
        if (!(structure.heads.all { it.label.trim().isNotBlank() })) throw CmsException.Validation("Every fee head needs a label.")
        if (!(structure.heads.all { it.amount > 0.0 })) throw CmsException.Validation("Every fee amount must be greater than zero.")

        val shift = structure.shift.name
        val feeDto = SessionFeeDto(
            sessionId = structure.sessionId,
            shift = shift,
            cadence = structure.cadence.name,
            academicYear = structure.academicYear,
            dueDate = structure.dueDate,
            paymentNote = structure.paymentNote,
            updatedBy = updatedBy,
        )
        postgrest.from(SupabaseTables.SESSION_FEES).upsert(feeDto) { onConflict = "session_id,shift" }

        postgrest.from(SupabaseTables.SESSION_FEE_HEADS).update({
            set("is_deleted", true)
            set("updated_by", updatedBy)
        }) {
            filter {
                eq("session_id", structure.sessionId)
                eq("shift", shift)
            }
        }

        val heads = structure.heads.mapIndexed { index, head ->
            SessionFeeHeadDto(
                sessionId = structure.sessionId,
                shift = shift,
                label = head.label,
                amount = head.amount,
                position = index,
                updatedBy = updatedBy,
                isDeleted = false,
            )
        }
        if (heads.isNotEmpty()) {
            postgrest.from(SupabaseTables.SESSION_FEE_HEADS).upsert(heads) {
                onConflict = "session_id,shift,label"
            }
        }

        val now = System.currentTimeMillis()
        feeDao.applyFeeDelta(
            listOf(SessionFeeMapper.feeDtoToEntity(feeDto).copy(createdAt = now, updatedAt = now)),
            emptyList(),
        )
        feeDao.deleteHeadsFor(structure.sessionId, shift)
        feeDao.applyHeadDelta(
            heads.map { SessionFeeMapper.headDtoToEntity(it).copy(createdAt = now, updatedAt = now) },
            emptyList(),
        )
    }

    override suspend fun syncSession(sessionId: String) {
        syncCollege()
        val scope = SyncCheckpointDefaults.scoped("session" to sessionId)
        val owner = syncOwnerKey()
        fetchIncrementalDelta(
            checkpointStore = checkpointStore,
            ownerKey = owner,
            tableName = SupabaseTables.SESSION_FEES,
            scopeKey = scope,
            updatedAtOf = SessionFeeDto::updatedAt,
            applyDelta = { feeDelta ->
                val feeEntities = feeDelta.map(SessionFeeMapper::feeDtoToEntity)
                val (deletedFees, activeFees) = feeEntities.partition { it.isDeleted }
                feeDao.applyFeeDelta(activeFees, deletedFees.map { it.sessionId to it.shift })
            },
        ) { since, from, to ->
            postgrest.from(SupabaseTables.SESSION_FEES).select {
                filter { eq("session_id", sessionId); gt("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        fetchIncrementalDelta(
            checkpointStore = checkpointStore,
            ownerKey = owner,
            tableName = SupabaseTables.SESSION_FEE_HEADS,
            scopeKey = scope,
            updatedAtOf = SessionFeeHeadDto::updatedAt,
            applyDelta = { headDelta ->
                val headEntities = headDelta.map(SessionFeeMapper::headDtoToEntity)
                val (deletedHeads, activeHeads) = headEntities.partition { it.isDeleted }
                feeDao.applyHeadDelta(activeHeads, deletedHeads.map { it.id })
            },
        ) { since, from, to ->
            postgrest.from(SupabaseTables.SESSION_FEE_HEADS).select {
                filter { eq("session_id", sessionId); gt("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
    }

    override suspend fun syncAll() {
        syncCollege()
        val scope = SyncCheckpointDefaults.globalScope()
        val owner = syncOwnerKey()
        fetchIncrementalDelta(
            checkpointStore = checkpointStore,
            ownerKey = owner,
            tableName = SupabaseTables.SESSION_FEES,
            scopeKey = scope,
            updatedAtOf = SessionFeeDto::updatedAt,
            applyDelta = { feeDelta ->
                val feeEntities = feeDelta.map(SessionFeeMapper::feeDtoToEntity)
                val (deletedFees, activeFees) = feeEntities.partition { it.isDeleted }
                feeDao.applyFeeDelta(activeFees, deletedFees.map { it.sessionId to it.shift })
            },
        ) { since, from, to ->
            postgrest.from(SupabaseTables.SESSION_FEES).select {
                filter { gt("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        fetchIncrementalDelta(
            checkpointStore = checkpointStore,
            ownerKey = owner,
            tableName = SupabaseTables.SESSION_FEE_HEADS,
            scopeKey = scope,
            updatedAtOf = SessionFeeHeadDto::updatedAt,
            applyDelta = { headDelta ->
                val headEntities = headDelta.map(SessionFeeMapper::headDtoToEntity)
                val (deletedHeads, activeHeads) = headEntities.partition { it.isDeleted }
                feeDao.applyHeadDelta(activeHeads, deletedHeads.map { it.id })
            },
        ) { since, from, to ->
            postgrest.from(SupabaseTables.SESSION_FEE_HEADS).select {
                filter { gt("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
    }
}
