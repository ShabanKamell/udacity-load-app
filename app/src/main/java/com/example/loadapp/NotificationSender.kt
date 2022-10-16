package com.example.loadapp

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

object NotificationSender {
    private const val NOTIFICATION_ID = 0

    fun sendLoadCompletedNotification(
        messageBody: String,
        status: String,
        context: Context
    ) {
        val contentIntent = Intent(context, DetailActivity::class.java)
        contentIntent.apply {
            putExtra(DetailActivity.FILE_NAME, messageBody)
            putExtra(DetailActivity.STATUS, status)
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val action =
            NotificationCompat.Action.Builder(
                0,
                context.getString(R.string.show_details),
                contentPendingIntent
            ).build()
        val notificationBuilder = NotificationCompat.Builder(
            context,
            context.getString(R.string.githubRepo_notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(context.getString(R.string.download_complete))
            .setContentText(messageBody)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(action)

        val manager = ContextCompat.getSystemService(
            context,
            NotificationManager::class.java
        ) as NotificationManager

        manager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

}