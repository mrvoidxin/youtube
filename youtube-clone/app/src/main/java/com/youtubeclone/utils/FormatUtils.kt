package com.youtubeclone.utils

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object FormatUtils {

    fun formatViewCount(count: Long): String {
        return when {
            count >= 1_000_000_000 -> {
                val value = count / 1_000_000_000.0
                String.format(Locale.US, "%.1fB views", value)
            }
            count >= 1_000_000 -> {
                val value = count / 1_000_000.0
                String.format(Locale.US, "%.1fM views", value)
            }
            count >= 1_000 -> {
                val value = count / 1_000.0
                String.format(Locale.US, "%.1fK views", value)
            }
            else -> {
                val df = DecimalFormat("#,###")
                "${df.format(count)} views"
            }
        }
    }

    fun formatViewCount(count: Int): String = formatViewCount(count.toLong())

    fun formatDuration(duration: String): String {
        return try {
            val cleaned = duration.replace("PT", "")
            var hours = 0
            var minutes = 0
            var seconds = 0

            val hIndex = cleaned.indexOf('H')
            val mIndex = cleaned.indexOf('M')
            val sIndex = cleaned.indexOf('S')

            if (hIndex != -1) {
                hours = cleaned.substring(0, hIndex).toIntOrNull() ?: 0
            }
            if (mIndex != -1) {
                val start = if (hIndex != -1) hIndex + 1 else 0
                minutes = cleaned.substring(start, mIndex).toIntOrNull() ?: 0
            }
            if (sIndex != -1) {
                val start = if (mIndex != -1) mIndex + 1 else if (hIndex != -1) hIndex + 1 else 0
                seconds = cleaned.substring(start, sIndex).toIntOrNull() ?: 0
            }

            if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%d:%02d", minutes, seconds)
            }
        } catch (e: Exception) {
            "0:00"
        }
    }

    fun formatRelativeTime(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(dateString) ?: return dateString
            val now = Date()
            val diffMs = now.time - date.time

            val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMs)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
            val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
            val days = TimeUnit.MILLISECONDS.toDays(diffMs)

            when {
                seconds < 60 -> "Just now"
                minutes < 60 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
                hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
                days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
                days < 30 -> "${days / 7} week${if (days / 7 > 1) "s" else ""} ago"
                days < 365 -> "${days / 30} month${if (days / 30 > 1) "s" else ""} ago"
                else -> {
                    val years = days / 365
                    "$years year${if (years > 1) "s" else ""} ago"
                }
            }
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatSubscriberCount(count: Long): String {
        return when {
            count >= 1_000_000_000 -> {
                val value = count / 1_000_000_000.0
                String.format(Locale.US, "%.2fB subscribers", value)
            }
            count >= 1_000_000 -> {
                val value = count / 1_000_000.0
                String.format(Locale.US, "%.2fM subscribers", value)
            }
            count >= 1_000 -> {
                val value = count / 1_000.0
                String.format(Locale.US, "%.1fK subscribers", value)
            }
            else -> {
                val df = DecimalFormat("#,###")
                "${df.format(count)} subscribers"
            }
        }
    }

    data class Chapter(
        val timeSeconds: Int,
        val formattedTime: String,
        val title: String
    )

    fun parseChapters(text: String): List<Chapter> {
        val lines = text.trim().split("\n").filter { it.isNotBlank() }
        val chapters = mutableListOf<Chapter>()

        val timeRegex = Regex("""^(\d{1,2}:)?(\d{1,2}):(\d{2})\s+(.+)$""")

        for (line in lines) {
            val match = timeRegex.find(line.trim()) ?: continue
            val groups = match.groupValues
            val hours = groups[1].replace(":", "").toIntOrNull() ?: 0
            val minutes = groups[2].toIntOrNull() ?: 0
            val seconds = groups[3].toIntOrNull() ?: 0
            val title = groups[4].trim()

            val totalSeconds = hours * 3600 + minutes * 60 + seconds
            val formatted = if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%d:%02d", minutes, seconds)
            }

            chapters.add(Chapter(totalSeconds, formatted, title))
        }

        return chapters
    }

    fun toMinutesAndSeconds(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format(Locale.US, "%d:%02d", minutes, seconds)
    }

    fun toFormattedDate(millis: Long): String {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.US)
        return sdf.format(Date(millis))
    }

    fun extractVideoId(url: String): String? {
        val patterns = listOf(
            Regex("""(?:youtube\.com/watch\?v=|youtu\.be/|youtube\.com/embed/|youtube\.com/v/|youtube\.com/shorts/)([a-zA-Z0-9_-]{11})"""),
            Regex("""^([a-zA-Z0-9_-]{11})$""")
        )
        for (pattern in patterns) {
            val match = pattern.find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }
}
