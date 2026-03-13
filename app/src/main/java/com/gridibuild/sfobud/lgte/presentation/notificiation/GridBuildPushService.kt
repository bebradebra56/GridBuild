package com.gridibuild.sfobud.lgte.presentation.notificiation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gridibuild.sfobud.GridBuildActivity
import com.gridibuild.sfobud.R

private const val GRID_BUILD_CHANNEL_ID = "grid_build_notifications"
private const val GRID_BUILD_CHANNEL_NAME = "GridBuild Notifications"
private const val GRID_BUILD_NOT_TAG = "GridBuild"

class GridBuildPushService : FirebaseMessagingService(){
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Обработка notification payload
        remoteMessage.notification?.let {
            if (remoteMessage.data.contains("url")) {
                gridBuildShowNotification(it.title ?: GRID_BUILD_NOT_TAG, it.body ?: "", data = remoteMessage.data["url"])
            } else {
                gridBuildShowNotification(it.title ?: GRID_BUILD_NOT_TAG, it.body ?: "", data = null)
            }
        }

    }

    private fun gridBuildShowNotification(title: String, message: String, data: String?) {
        val gridBuildNotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Создаем канал уведомлений для Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GRID_BUILD_CHANNEL_ID,
                GRID_BUILD_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            gridBuildNotificationManager.createNotificationChannel(channel)
        }

        val gridBuildIntent = Intent(this, GridBuildActivity::class.java).apply {
            putExtras(bundleOf(
                "url" to data
            ))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val gridBuildPendingIntent = PendingIntent.getActivity(
            this,
            0,
            gridBuildIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val gridBuildNotification = NotificationCompat.Builder(this, GRID_BUILD_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.grid_build_noti_ic)
            .setAutoCancel(true)
            .setContentIntent(gridBuildPendingIntent)
            .build()

        gridBuildNotificationManager.notify(System.currentTimeMillis().toInt(), gridBuildNotification)
    }

}