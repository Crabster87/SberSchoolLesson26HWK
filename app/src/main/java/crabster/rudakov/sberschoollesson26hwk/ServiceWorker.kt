package crabster.rudakov.sberschoollesson26hwk

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class ServiceWorker : Service() {

    companion object {
        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

        private const val TAG = "ServiceWorker"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "CHANNEL_ID_1"
        private const val ACTION_START = "ACTION_START"
        private const val ACTION_PAUSE = "ACTION_PAUSE"
        private const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d("START LAUNCH", "SERVICE IS WORKING")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Название"
            val description = "Описание"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(remoteViews: RemoteViews): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOnlyAlertOnce(true)
            .setContent(remoteViews)
        return builder.build()
    }

    private fun getRemoteViews(): RemoteViews {
        val deleteIntent = Intent(this, ServiceWorker::class.java)
        deleteIntent.action = ACTION_STOP_SERVICE
        val deletePendingIntent = PendingIntent.getService(this, 0, deleteIntent, 0)

        val startIntent = Intent(this, ServiceWorker::class.java)
        startIntent.action = ACTION_START
        val startPendingIntent = PendingIntent.getService(this, 0, startIntent, 0)

        val pauseIntent = Intent(this, ServiceWorker::class.java)
        pauseIntent.action = ACTION_PAUSE
        val pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0)

        val stopIntent = Intent(this, ServiceWorker::class.java)
        stopIntent.action = ACTION_STOP
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0)

        val remoteViews = RemoteViews(packageName, R.layout.notification_custom)
        remoteViews.setTextViewText(R.id.tv_charge, "90%")
        remoteViews.setOnClickPendingIntent(R.id.btn_stop_service, deletePendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.btn_start, startPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.btn_pause, pausePendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.btn_stop, stopPendingIntent)

        return remoteViews
    }

    private fun stopTimer(remoteViews: RemoteViews): RemoteViews {
        diff = 0L
        remoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime(), null, false)
        return remoteViews
    }

    private fun startTimer(remoteViews: RemoteViews): RemoteViews {
        start = SystemClock.elapsedRealtime() - diff
        remoteViews.setChronometer(R.id.chronometer, start, null, true)
        return remoteViews
    }


    private fun pauseTimer(remoteViews: RemoteViews): RemoteViews {
        pause = SystemClock.elapsedRealtime()
        diff = pause - start
        remoteViews.setChronometer(
            R.id.chronometer, pause - diff,
            null, false
        )
        return remoteViews
    }

    private var start = 0L
    private var pause = 0L
    private var diff = 0L

    private var isStart = false
    private var isStop = false
    private var isPause = false

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_START -> {
                if (!isStart) {
                    Toast.makeText(this, "START", Toast.LENGTH_SHORT).show()
                    updateNotification(createNotification(startTimer(getRemoteViews())))
                    isStart = true
                    isStop = false
                    isPause = false
                }
            }
            ACTION_PAUSE -> {
                if (!isPause && isStart) {
                    Toast.makeText(this, "PAUSE", Toast.LENGTH_SHORT).show()
                    updateNotification(createNotification(pauseTimer(getRemoteViews())))
                    isStart = false
                    isStop = false
                    isPause = true
                }

            }
            ACTION_STOP -> {
                if (!isStop) {
                    Toast.makeText(this, "STOP", Toast.LENGTH_SHORT).show()
                    updateNotification(createNotification(stopTimer(getRemoteViews())))
                    isStart = false
                    isStop = true
                    isPause = false
                }

            }
            ACTION_START_SERVICE -> startForeground(
                NOTIFICATION_ID, createNotification(
                    stopTimer(
                        getRemoteViews()
                    )
                )
            )
            ACTION_STOP_SERVICE -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun updateNotification(notification: Notification) {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

}