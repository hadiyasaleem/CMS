package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetDraft
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.datesheetScheduleQuality
import com.mbd.cmscommon.domain.model.normalized
import com.mbd.cmscommon.domain.model.validationMessage
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DatesheetsController(
    private val repo: DatesheetRepository,
    private val createdBy: String = "",
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _sheets = MutableStateFlow<List<Datesheet>?>(null)
    val sheets: StateFlow<List<Datesheet>?> = _sheets.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _slots = MutableStateFlow<Map<String, List<DatesheetSlot>>>(emptyMap())
    val slots: StateFlow<Map<String, List<DatesheetSlot>>> = _slots.asStateFlow()

    private val _loadingSlots = MutableStateFlow<Set<String>>(emptySet())
    val loadingSlots: StateFlow<Set<String>> = _loadingSlots.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = launch {
        clearError()
        _refreshing.value = true
        try {
            _sheets.value = normalizeSheets(repo.getDatesheets())
        } finally {
            _refreshing.value = false
        }
    }

    fun loadSlots(id: String, force: Boolean = false) {
        if (id.isBlank()) return
        if ((!force && _slots.value.containsKey(id)) || _loadingSlots.value.contains(id)) return
        launch {
            _loadingSlots.value = _loadingSlots.value + id
            try {
                val sheet = _sheets.value.orEmpty().firstOrNull { it.id == id }
                val fetched = repo.getSlots(id)
                val slots = if (sheet != null) datesheetScheduleQuality(sheet, fetched).slots else fetched
                _slots.value = _slots.value + (id to slots)
            } finally {
                _loadingSlots.value = _loadingSlots.value - id
            }
        }
    }

    fun createDatesheet(draft: DatesheetDraft) = mutate {
        validationMessage(draft)?.let { throw IllegalArgumentException(it) }
        require(!draft.published) { "Create the datesheet as a draft, add at least one paper, then publish it." }
        repo.createDatesheet(
            title = draft.title.trim(),
            examType = draft.examType.orEmpty(),
            sessionId = draft.sessionId,
            instructions = draft.instructions?.trim().orEmpty(),
            published = draft.published,
            createdBy = createdBy,
        )
        _actionMessage.value = "Datesheet saved as a draft."
    }

    fun updateDatesheet(id: String, draft: DatesheetDraft) = mutate {
        requireCurrentSheet(id)
        validationMessage(draft)?.let { throw IllegalArgumentException(it) }
        repo.updateDatesheet(
            id = id,
            title = draft.title.trim(),
            examType = draft.examType.orEmpty(),
            sessionId = draft.sessionId,
            instructions = draft.instructions?.trim().orEmpty(),
            published = draft.published,
        )
        _actionMessage.value = if (draft.published) "Datesheet published." else "Datesheet updated."
    }

    fun setPublished(id: String, published: Boolean) = mutate {
        val sheet = requireCurrentSheet(id)
        if (published) {
            val quality = datesheetScheduleQuality(sheet, repo.getSlots(id))
            require(quality.canPublish) { quality.issues.joinToString(" ") }
        }
        repo.setPublished(id, published)
        _actionMessage.value = if (published) "Datesheet published." else "Datesheet moved to drafts."
    }

    fun deleteDatesheet(id: String) = mutate {
        requireCurrentSheet(id)
        repo.deleteDatesheet(id)
        _slots.value = _slots.value - id
        _actionMessage.value = "Datesheet deleted."
    }

    fun addSlot(slot: DatesheetSlot) = mutate(refreshSheets = false) {
        val sheet = requireCurrentSheet(slot.datesheetId)
        val normalizedSlot = normalized(slot)
        validationMessage(normalizedSlot)?.let { throw IllegalArgumentException(it) }
        val candidate = normalizedSlot.copy(id = "pending-new-paper")
        val existingSlots = repo.getSlots(slot.datesheetId)
        val quality = datesheetScheduleQuality(sheet, existingSlots + candidate)
        require(quality.issues.isEmpty()) { quality.issues.joinToString(" ") }
        repo.addSlot(normalizedSlot)
        _slots.value = _slots.value + (slot.datesheetId to repo.getSlots(slot.datesheetId))
        _actionMessage.value = "Exam paper added."
    }

    fun updateSlot(slot: DatesheetSlot) = mutate(refreshSheets = false) {
        val sheet = requireCurrentSheet(slot.datesheetId)
        require(slot.id.isNotBlank()) { "This paper has no database ID and cannot be updated safely." }
        val normalizedSlot = normalized(slot)
        validationMessage(normalizedSlot)?.let { throw IllegalArgumentException(it) }
        val currentSlots = repo.getSlots(slot.datesheetId)
        require(currentSlots.any { it.id == slot.id }) { "This paper is no longer in the datesheet. Refresh and try again." }
        val merged = currentSlots.map { if (it.id == slot.id) normalizedSlot else it }
        val quality = datesheetScheduleQuality(sheet, merged)
        require(quality.issues.isEmpty()) { quality.issues.joinToString(" ") }
        repo.updateSlot(normalizedSlot)
        _slots.value = _slots.value + (slot.datesheetId to repo.getSlots(slot.datesheetId))
        _actionMessage.value = "Exam paper updated."
    }

    fun deleteSlot(datesheetId: String, id: String) = mutate(refreshSheets = false) {
        requireCurrentSheet(datesheetId)
        require(id.isNotBlank()) { "This paper has no database ID and cannot be removed safely." }
        val currentSlots = repo.getSlots(datesheetId)
        require(currentSlots.any { it.id == id }) { "This paper is no longer in the datesheet. Refresh and try again." }
        val published = _sheets.value.orEmpty().firstOrNull { it.id == datesheetId }?.published == true
        if (published && currentSlots.size <= 1) {
            throw IllegalArgumentException("Move the datesheet to drafts before removing its final paper.")
        }
        repo.deleteSlot(id)
        _slots.value = _slots.value + (datesheetId to repo.getSlots(datesheetId))
        _actionMessage.value = "Exam paper removed."
    }

    private fun mutate(refreshSheets: Boolean = true, block: suspend () -> Unit) {
        if (_busy.value) return
        launch {
            clearError()
            _actionMessage.value = null
            _busy.value = true
            try {
                block()
                if (refreshSheets) {
                    _sheets.value = normalizeSheets(repo.getDatesheets())
                }
            } finally {
                _busy.value = false
            }
        }
    }

    private fun requireCurrentSheet(id: String): Datesheet {
        require(id.isNotBlank()) { "This datesheet has no database ID and cannot be managed safely." }
        return _sheets.value.orEmpty().firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("This datesheet is no longer available. Refresh and try again.")
    }

    private fun normalizeSheets(sheets: List<Datesheet>): List<Datesheet> {
        val seen = mutableSetOf<String>()
        return sheets.filter { it.id.isBlank() || seen.add(it.id) }
    }
}
