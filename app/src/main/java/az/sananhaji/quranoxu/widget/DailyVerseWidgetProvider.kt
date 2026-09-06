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

class DailyVerseWidgetProvider : AppWidgetProvider() {

    private val executor = Executors.newSingleThreadExecutor()

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_DAILY_VERSE || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateWidget(context, appWidgetManager, appWidgetId)
            } else {
                val appWidgetIds = appWidgetManager.getAppWidgetIds(android.content.ComponentName(context, DailyVerseWidgetProvider::class.java))
                for (id in appWidgetIds) {
                    updateWidget(context, appWidgetManager, id)
                }
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        // Step 1: Immediately submit initial valid RemoteViews (0ms latency, prevents Launcher "Problem loading widget")
        val views = RemoteViews(context.packageName, R.layout.widget_daily_verse)
        views.setTextViewText(R.id.widget_header_title, "Quranoxu • Günün Ayəsi")
        views.setTextViewText(R.id.widget_btn_refresh, "🔄 Yeni")
        views.setTextViewText(R.id.widget_verse_text, "📖 Ayə yüklənir...")
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

        val refreshIntent = Intent(context, DailyVerseWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_DAILY_VERSE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)

        // Step 2: Asynchronously load database and update widget smoothly
        executor.execute {
            try {
                val dbHelper = QuranDatabaseHelper(context.applicationContext)
                val surahs = dbHelper.getAllSurahs()

                if (surahs.isNotEmpty()) {
                    val randomSurah = surahs.random()
                    val verseCount = randomSurah.verseCount
                    val randomVerseIndex = (0 until verseCount).random()
                    val verseNum = randomVerseIndex + 1
                    val azeriText = randomSurah.versesAzerbaijani.getOrElse(randomVerseIndex) { "" }
                    val surahName = randomSurah.nameAzeri

                    val updatedViews = RemoteViews(context.packageName, R.layout.widget_daily_verse)
                    updatedViews.setTextViewText(R.id.widget_header_title, "Quranoxu • Günün Ayəsi")
                    updatedViews.setTextViewText(R.id.widget_btn_refresh, "🔄 Yeni")
                    updatedViews.setTextViewText(R.id.widget_verse_text, "\"$azeriText\"")
                    updatedViews.setTextViewText(R.id.widget_surah_info, "$surahName surəsi, $verseNum-ci ayə")

                    val appIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("surah_index", randomSurah.index)
                        putExtra("verse_number", verseNum)
                    }
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        appIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    updatedViews.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                    updatedViews.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, updatedViews)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val ACTION_REFRESH_DAILY_VERSE = "az.sananhaji.quranoxu.widget.ACTION_REFRESH_DAILY_VERSE"
    }
}
