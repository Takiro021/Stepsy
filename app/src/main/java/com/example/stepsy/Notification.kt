package com.example.stepsy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import java.time.Duration

object NotificationHelper {
    private const val SERVICE_CHANNEL = "service_channel"
    private const val WALK_NOTIFICATION = "walk_notification"
    private const val WALK_NOTIFICATION_ID = 111

    private val walkNotificationDelay = Duration.ofHours(1)

    fun createNotificationChannel(context: Context) {
        val name = context.getString(R.string.notification_service)
        val descriptionText = context.getString(R.string.notification_service_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(SERVICE_CHANNEL, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun scheduleWalkNotification(context: Context) {
        val work = OneTimeWorkRequestBuilder<WalkNotificationWorker>()
            .setInitialDelay(walkNotificationDelay)
            .build()
        WorkManager.getInstance(context).enqueue(work)
    }

    private fun showWalkNotification(context: Context) {
        val notification = NotificationCompat.Builder(context, SERVICE_CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_title))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notification_message))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(WALK_NOTIFICATION, WALK_NOTIFICATION_ID, notification)
        }
    }

    class WalkNotificationWorker(private val context: Context, workerParameters: WorkerParameters) :
        Worker(context, workerParameters) {
        override fun doWork(): Result {
            showWalkNotification(context)
            return Result.success()
        }
    }
}