package crabster.rudakov.sberschoollesson26hwk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Chronometer
import androidx.appcompat.widget.AppCompatButton

class MainActivity : AppCompatActivity() {

    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var intentService: Intent

    /**
     * Метод помимо основного функционала устанавливает Listener кнопке,
     * запускающей Service и отображает Chronometer
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val launchServiceButton: AppCompatButton = findViewById(R.id.launch_service_button)
        launchServiceButton.setOnClickListener { launchService() }
        displayTimer()
    }

    /**
     * Метод создаёт и запускает foreground Service
     * */
    private fun launchService() {
        intentService = Intent(this, NotificationService::class.java)
        intentService.action = Constants.ACTION_START_SERVICE
        startService(intentService)
    }

    /**
     * Метод создаёт intentFilter для BroadcastReceiver
     * */
    private fun createIntentFilterChronometer(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Constants.ACTION_ACTIVITY_CHRONOMETER)
        return intentFilter
    }

    /**
     * Метод создаёт и запускает BroadcastReceiver, который отслеживает
     * изменения во View Chronometer сервиса NotificationService и передаёт
     * их в виде команд и базовых значений времени для Chronometer текущей
     * Activity
     * */
    private fun displayTimer() {
        val timer: Chronometer = findViewById(R.id.activity_timer)
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val time = intent!!.getLongExtra(Constants.INTENT_CHRONOMETER_TIME_KEY, 0)
                when (intent.getStringExtra(Constants.INTENT_CHRONOMETER_COMMAND_KEY)) {
                    Constants.INTENT_CHRONOMETER_COMMAND_START -> {
                        timer.start()
                        timer.base = time
                    }
                    Constants.INTENT_CHRONOMETER_COMMAND_PAUSE -> {
                        timer.stop()
                        timer.base = time
                    }
                    Constants.INTENT_CHRONOMETER_COMMAND_STOP -> {
                        timer.stop()
                        timer.base = time
                    }
                }
            }
        }
        registerReceiver(broadcastReceiver, createIntentFilterChronometer())
    }

    /**
     * Метод помимо основного функционала отменяет регистрацию Service и
     * BroadcastReceiver
     * */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
        stopService(intentService)
    }

}