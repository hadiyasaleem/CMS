package com.mbd.cmscommon.domain.model

// Matches the "photos" storage bucket's own file_size_limit/allowed_mime_types cap.
const val MAX_TEACHER_PHOTO_BYTES = 1048576
val TEACHER_PHOTO_MIME_TYPES = setOf("image/jpeg", "image/png", "image/webp")

fun teacherPhotoUploadError(mimeType: String?, fileBytes: ByteArray): String? {
    if (fileBytes.isEmpty()) return "The selected image is empty."
    if (fileBytes.size > MAX_TEACHER_PHOTO_BYTES) return "The selected image exceeds the 1 MB limit."
    if (mimeType !in TEACHER_PHOTO_MIME_TYPES) return "Photos must be JPEG, PNG, or WebP."
    return null
}

fun teacherPhotoExtension(mimeType: String): String = when (mimeType) {
    "image/png" -> "png"
    "image/webp" -> "webp"
    else -> "jpg"
}
