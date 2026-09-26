package com.mbd.cmscommon.domain.model

/** Topics an admin lists in a subject's outline, comma (or line) separated. Blank entries and repeats are dropped. */
fun outlineTopics(outline: String?): List<String> =
    outline.orEmpty().split(',', '\n', ';').map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }

/** The topics recorded in an attendance entry's "taught" text (same comma-separated form). */
fun taughtTopics(text: String?): List<String> = outlineTopics(text)

/** Adds [topic] to the comma-separated [current] text, or removes it if it is already there. */
fun toggleTopic(current: String, topic: String): String {
    val topics = taughtTopics(current)
    val present = topics.any { it.equals(topic, ignoreCase = true) }
    val next = if (present) topics.filterNot { it.equals(topic, ignoreCase = true) } else topics + topic
    return next.joinToString(", ")
}

/** Every distinct topic taught across [entries], first-taught order, each listed once. */
fun distinctTaughtTopics(entries: List<String?>): List<String> =
    entries.flatMap { taughtTopics(it) }.distinctBy { it.lowercase() }
