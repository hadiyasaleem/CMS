package com.mbd.cmscommon.util

import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.AccessDeniedException
import java.nio.file.NoSuchFileException
import java.util.zip.ZipException

/**
 * Plain-words reasons a picked file (spreadsheet import, exam paper, photo) could not be read. The
 * pickers catch whatever the platform throws (`SecurityException`, `FileNotFoundException`,
 * `ZipException` from a damaged .xlsx, `OutOfMemoryError` from a huge file, ...) and previously all
 * of it collapsed into "Couldn't read this file."
 */
object FileReadErrors {

    private val UNSAFE = listOf("http://", "https://", "supabase", "exception", "stacktrace", "{", "}", "\\", "/data/", "content://")

    /** [noun] is what the user picked: "file", "photo", "spreadsheet". */
    fun describe(t: Throwable, noun: String = "file"): String {
        if (t is CmsException) return t.message ?: "Couldn't read that $noun."
        return when {
            t is ZipException ->
                "This $noun isn't a valid Excel (.xlsx) file. It may be damaged, or saved in another format."
            t is FileNotFoundException || t is NoSuchFileException ->
                "That $noun can no longer be found. It may have been moved or deleted; choose it again."
            t is SecurityException || t is AccessDeniedException ->
                "The app isn't allowed to read that $noun. Choose it again, or copy it to another folder first."
            t is OutOfMemoryError ->
                "That $noun is too large to open."
            (t is IllegalArgumentException || t is IllegalStateException) && isSafeSentence(t.message) ->
                t.message!!.trim()
            t is IOException ->
                "Couldn't read that $noun. If it is open in another program, close it and try again."
            else -> "Couldn't read that $noun."
        }
    }

    /**
     * Why a report/export could not be written or opened. [format] is what was being produced ("PDF",
     * "Excel (.xlsx)"). Covers a permission problem, a full disk, a file open in another program and a
     * missing viewer app (`ActivityNotFoundException` on Android).
     */
    fun describeWrite(t: Throwable, format: String): String {
        if (t is CmsException) return t.message ?: "Couldn't create the $format file."
        val text = generateSequence(t) { it.cause }.take(4).mapNotNull { it.message }.joinToString(" ").lowercase()
        return when {
            t is SecurityException || t is AccessDeniedException ->
                "The app isn't allowed to save there. Choose a different folder and try again."
            "no space left" in text || "not enough space" in text || "disk full" in text ->
                "There isn't enough free space to save the $format file. Free some space and try again."
            t::class.java.simpleName == "ActivityNotFoundException" ->
                "The $format file was created, but no app on this device can open it. Install a $format viewer."
            t is IOException ->
                "Couldn't write the $format file. If it is open in another program, close it, or choose another name or folder."
            else -> "Couldn't create the $format file."
        }
    }

    /** [t] as a [CmsException.Validation] carrying [describe]'s wording, so it reaches the user unchanged. */
    fun asCmsException(t: Throwable, noun: String = "file"): CmsException =
        t as? CmsException ?: CmsException.Validation(describe(t, noun), cause = t)

    private fun isSafeSentence(message: String?): Boolean {
        val line = message?.trim().orEmpty()
        return line.isNotBlank() && line.length <= 180 && UNSAFE.none { line.contains(it, ignoreCase = true) }
    }
}
