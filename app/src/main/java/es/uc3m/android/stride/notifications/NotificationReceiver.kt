package es.uc3m.android.stride.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import es.uc3m.android.stride.R
import es.uc3m.android.stride.ui.HomeActivity

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val channelId = "stride_daily_reminder"
        val channelName = "Daily Activity Reminder"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For Android 8 and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open HomeActivity when the notification is clicked
        val notificationIntent = Intent(context, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Use your notification icon
            .setContentTitle("👟 Ready to move?")
            .setContentText("Start your activity now!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification
        notificationManager.notify(1002, notification)
    }
}
