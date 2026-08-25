package com.mbd.cmscommon.domain.model

const val MAX_EXAM_PAPER_BYTES = 5242880

fun examPaperUploadError(fileName: String, fileBytes: ByteArray): String? {
    val cleanName = fileName.trim()
    val extension = cleanName.substringAfterLast('.', "").lowercase()

    if (cleanName.isEmpty()) return "The selected file must have a name."
    if (cleanName.length > 255) return "The file name must not exceed 255 characters."
    if (cleanName.any { it == '/' || it == '\\' || it < ' ' }) return "The file name contains unsupported characters."
    if (extension !in setOf("pdf", "docx")) return "Exam papers must be PDF or DOCX files."
    if (fileBytes.isEmpty()) return "The selected file is empty."
    if (fileBytes.size > MAX_EXAM_PAPER_BYTES) return "The selected file exceeds the 5 MB limit."
    return null
}
