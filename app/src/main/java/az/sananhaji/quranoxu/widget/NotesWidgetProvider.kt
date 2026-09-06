package az.sananhaji.quranoxu.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import az.sananhaji.quranoxu.MainActivity
import az.sananhaji.quranoxu.R
import az.sananhaji.quranoxu.data.db.QuranDatabaseHelper
import java.util.concurrent.Executors

class NotesWidgetProvider : AppWidgetProvider() {

    private val executor = Executors.newSingleThreadExecutor()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, 0)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_NEXT_NOTE_WIDGET || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val currentIndex = intent.getIntExtra("current_index", 0)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateWidget(context, appWidgetManager, appWidgetId, currentIndex)
            } else {
                val appWidgetIds = appWidgetManager.getAppWidgetIds(android.content.ComponentName(context, NotesWidgetProvider::class.java))
                for (id in appWidgetIds) {
                    updateWidget(context, appWidgetManager, id, currentIndex)
                }
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, noteOffsetIndex: Int) {
        // Step 1: Immediately submit initial safe RemoteViews (0ms latency, prevents Launcher "Problem loading widget")
        val views = RemoteViews(context.packageName, R.layout.widget_notes)
        views.setTextViewText(R.id.widget_header_title, "Quranoxu • Qeydlərim")
        views.setTextViewText(R.id.widget_btn_next, "▶ Növbəti")
        views.setTextViewText(R.id.widget_note_text, "📌 Qeydlər yüklənir...")
        views.setTextViewText(R.id.widget_surah_info, "Quranoxu")

        val defaultIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val defaultPendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            defaultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, defaultPendingIntent)

        val nextIntent = Intent(context, NotesWidgetProvider::class.java).apply {
            action = ACTION_NEXT_NOTE_WIDGET
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("current_index", noteOffsetIndex + 1)
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)

        // Step 2: Asynchronously load database and update widget smoothly
        executor.execute {
            try {
                val dbHelper = QuranDatabaseHelper(context.applicationContext)
                val notes = dbHelper.getAllNotes()
                val bookmarks = dbHelper.getAllBookmarks()

                val updatedViews = RemoteViews(context.packageName, R.layout.widget_notes)
                updatedViews.setTextViewText(R.id.widget_header_title, "Quranoxu • Qeydlərim")
                updatedViews.setTextViewText(R.id.widget_btn_next, "▶ Növbəti")

                if (notes.isEmpty() && bookmarks.isEmpty()) {
                    updatedViews.setTextViewText(R.id.widget_note_text, "📌 Hələ ki heç bir ayəyə qeyd vurulmayıb və ya əlfəcin edilməyib.")
                    updatedViews.setTextViewText(R.id.widget_surah_info, "Qeydlərinizi görmək üçün tətbiqə keçin")
                    updatedViews.setOnClickPendingIntent(R.id.widget_container, defaultPendingIntent)
                    updatedViews.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)
                } else {
                    val totalItems = notes.size + bookmarks.size
                    val safeIndex = Math.abs(noteOffsetIndex) % totalItems

                    val surahIndex: Int
                    val verseNumber: Int
                    val surahName: String
                    val mainText: String

                    if (safeIndex < notes.size) {
                        val note = notes[safeIndex]
                        surahIndex = note.surahIndex
                        verseNumber = note.verseNumber
                        surahName = note.surahName
                        mainText = "📝 Qeydiniz: \"${note.noteText}\""
                    } else {
                        val bm = bookmarks[safeIndex - notes.size]
                        surahIndex = bm.surahIndex
                        verseNumber = bm.verseNumber
                        surahName = bm.surahName
                        mainText = "🔖 Əlfəcin olunmuş ayə"
                    }

                    val surahObj = dbHelper.getSurahByIndex(surahIndex)
                    val verseAzeri = surahObj?.versesAzerbaijani?.getOrElse(verseNumber - 1) { "" } ?: ""

                    val displayText = "$mainText\n\n📖 \"$verseAzeri\""

                    updatedViews.setTextViewText(R.id.widget_note_text, displayText)
                    updatedViews.setTextViewText(R.id.widget_surah_info, "$surahName surəsi, $verseNumber-ci ayə (${safeIndex + 1}/$totalItems)")

                    val appIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("surah_index", surahIndex)
                        putExtra("verse_number", verseNumber)
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        appIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    updatedViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                    updatedViews.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)
                }

                appWidgetManager.updateAppWidget(appWidgetId, updatedViews)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val ACTION_NEXT_NOTE_WIDGET = "az.sananhaji.quranoxu.widget.ACTION_NEXT_NOTE_WIDGET"
    }
}
