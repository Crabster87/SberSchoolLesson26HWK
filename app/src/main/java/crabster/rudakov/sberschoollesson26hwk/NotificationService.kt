package crabster.rudakov.sberschoollesson26hwk

import android.app.*
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_CHANGED
import android.content.IntentFilter
import android.os.*
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class NotificationService : Service(), BatteryListener {

    private lateinit var broadcastReceiver: BatteryBroadcastReceiver

    private var start = 0L
    private var pause = 0L
    private var diff = 0L

    private var isStarted = false
    private var isStopped = false
    private var isPaused = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupBroadcastReceiver()
        Log.d(Constants.LOG_ON_CREATE_TAG, Constants.LOG_ON_CREATE_MESSAGE)
    }

    /**
     * Метод создаёт Notification Channel
     * */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = Constants.CHANNEL_NAME
            val description = Constants.CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(
                NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Метод создаёт макет Notification
     * */
    private fun createNotification(remoteViews: RemoteViews): Notification {
        val builder = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
        builder
            .setSmallIcon(R.drawable.ic_baseline_format_align_justify_24)
            .setOnlyAlertOnce(true)
            .setContent(remoteViews)
        return builder.build()
    }

    /**
     * Метод получает RemoteViews кнопок управления хронометром и
     * устанавливает на каждую слушатель, который создаёт по нажатию
     * кнопки соответствующий PendingIntent
     * */
    private fun getRemoteViews(): RemoteViews {
        val remoteViews = RemoteViews(packageName, R.layout.notification_custom)
        remoteViews.setOnClickPendingIntent(
            R.id.btn_stop_service,
            createPendingIntent(Constants.ACTION_STOP_SERVICE))
        remoteViews.setOnClickPendingIntent(
            R.id.btn_start,
            createPendingIntent(Constants.ACTION_START))
        remoteViews.setOnClickPendingIntent(
            R.id.btn_pause,
            createPendingIntent(Constants.ACTION_PAUSE))
        remoteViews.setOnClickPendingIntent(
            R.id.btn_stop,
            createPendingIntent(Constants.ACTION_STOP))
        return remoteViews
    }

    /**
     * Метод создаёт PendingIntent, в который помещается ACTION, соответсвующий
     * типу операции с Chronometer
     * */
    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, NotificationService::class.java)
        intent.action = action
        return PendingIntent.getService(this, 0, intent, 0)
    }

    /**
     * Метод осуществляет обновление TextView со статусом батареи и уровне её
     * заряда в Notification
     * */
    private fun showBatteryStatus(remoteViews: RemoteViews, text: String): RemoteViews {
        remoteViews.setTextViewText(R.id.tv_charge, text)
        return remoteViews
    }

    /**
     * Метод реализует логику действий при нажатии пользовтелем кнопки "STOP"
     * в Notification, останавливая Chronometer
     * */
    private fun stopChronometer(remoteViews: RemoteViews): RemoteViews {
        diff = 0L
        remoteViews.setChronometer(R.id.chronometer, SystemClock.elapsedRealtime(), null, false)
        sendChronometerToActivity(
            Constants.INTENT_CHRONOMETER_COMMAND_STOP,
            SystemClock.elapsedRealtime())
        return remoteViews
    }

    /**
     * Метод реализует логику действий при нажатии пользовтелем кнопки "START"
     * в Notification, стартуя Chronometer
     * */
    private fun startChronometer(remoteViews: RemoteViews): RemoteViews {
        start = SystemClock.elapsedRealtime() - diff
        remoteViews.setChronometer(R.id.chronometer, start, null, true)
        sendChronometerToActivity(Constants.INTENT_CHRONOMETER_COMMAND_START, start)
        return remoteViews
    }

    /**
     * Метод реализует логику действий при нажатии пользовтелем кнопки "PAUSE"
     * в Notification, ставя Chronometer на паузу
     * */
    private fun pauseChronometer(remoteViews: RemoteViews): RemoteViews {
        pause = SystemClock.elapsedRealtime()
        diff = pause - start
        remoteViews.setChronometer(R.id.chronometer, pause - diff, null, false)
        sendChronometerToActivity(Constants.INTENT_CHRONOMETER_COMMAND_PAUSE, pause - diff)
        return remoteViews
    }

    /**
     * Метод создаёт Intent, в который помещаются тип команды, которую получил
     * хронометр Notification, и текущее значение показаний хронометра. Далее
     * отправляется уведомление в BroadcastReceiver
     * */
    private fun sendChronometerToActivity(command: String, time: Long) {
        val intent = Intent()
        intent.action = Constants.ACTION_ACTIVITY_CHRONOMETER
        intent.putExtra(Constants.INTENT_CHRONOMETER_COMMAND_KEY, command)
        intent.putExtra(Constants.INTENT_CHRONOMETER_TIME_KEY, time)
        sendBroadcast(intent)
    }

    /**
     * Метод вызывается при запуске текущего Service и реализует логику отклика
     * на действия пользователя путём анализа полученных Intent, в зависимости
     * от которых производятся запуск, остановка текущего Service и обновления
     * Notification по нажатию соответсвующих кнопок
     * */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            Constants.ACTION_START -> {
                if (!isStarted) {
                    updateNotification(createNotification(startChronometer(getRemoteViews())))
                    isStarted = true
                    isStopped = false
                    isPaused = false
                }
            }
            Constants.ACTION_PAUSE -> {
                if (!isPaused && isStarted) {
                    updateNotification(createNotification(pauseChronometer(getRemoteViews())))
                    isStarted = false
                    isStopped = false
                    isPaused = true
                }
            }
            Constants.ACTION_STOP -> {
                if (!isStopped) {
                    updateNotification(createNotification(stopChronometer(getRemoteViews())))
                    isStarted = false
                    isStopped = true
                    isPaused = false
                }
            }
            Constants.ACTION_START_SERVICE -> startForeground(
                Constants.NOTIFICATION_ID, createNotification(stopChronometer(getRemoteViews())))
            Constants.ACTION_STOP_SERVICE -> stopSelf()
        }
        return START_NOT_STICKY
    }

    /**
     * Метод осуществляет обновление Notification
     * */
    private fun updateNotification(notification: Notification) {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
    }

    /**
     * Метод помимо основного функционала отменяет регистрацию BroadcastReceiver
     * */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    /**
     * Метод возвращает коммуникационный канал с Service
     * */
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    /**
     * Метод создаёт и запускает BroadcastReceiver, который отслеживает
     * изменение состояния батареи
     * */
    private fun setupBroadcastReceiver() {
        val filter = IntentFilter()
        filter.addAction(ACTION_BATTERY_CHANGED)
        broadcastReceiver = BatteryBroadcastReceiver(this)
        registerReceiver(broadcastReceiver, filter)
        Log.d(Constants.LOG_BROADCAST_RECEIVER_TAG, Constants.LOG_BROADCAST_RECEIVER_MESSAGE)
    }

    /**
     * Метод создаёт и запускает BroadcastReceiver, который отслеживает
     * изменение состояния батареи и передаёт данные через специализированный
     * интерфейс в текущий Service, устанавливая изменения в Notification
     * */
    override fun onBatteryStatusChanged(batteryLevel: Int, deviceStatus: Int) {
        Log.d(Constants.LOG_BROADCAST_RECEIVER_TAG,
            "Battery level as $batteryLevel & battery status as $deviceStatus")
        var status: String? = null
        when (deviceStatus) {
            BatteryManager.BATTERY_STATUS_CHARGING -> status = "CHARGING $batteryLevel%"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> status = "DISCHARGING $batteryLevel%"
            BatteryManager.BATTERY_STATUS_FULL -> status = "FULL $batteryLevel%"
            BatteryManager.BATTERY_STATUS_UNKNOWN -> status = "UNKNOWN $batteryLevel%"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> status = "NOT CHARGING $batteryLevel%"
        }
        updateNotification(createNotification(showBatteryStatus(getRemoteViews(), status!!)))
    }

}