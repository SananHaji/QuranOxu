package az.sananhaji.quranoxu.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import az.sananhaji.quranoxu.MainActivity

class DailyReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "QuranDailyReminderChannel"
        const val NOTIFICATION_ID = 2002
    }

    override fun onReceive(context: Context, intent: Intent?) {
        createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Quranoxu Xatırlatması")
            .setContentText("Bu gün Quran oxumağı unutmayın! 📖")
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)

        // Reschedule for next day
        ReminderManager.scheduleDailyReminder(context)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Quran Günlük Xatırlatma",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Hər gün axşam saat 20:00-da Quran oxumağı xatırladan bildiriş"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
