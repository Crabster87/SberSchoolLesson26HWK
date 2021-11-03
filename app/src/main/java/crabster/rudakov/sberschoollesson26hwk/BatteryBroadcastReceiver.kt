package crabster.rudakov.sberschoollesson26hwk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

/**
 * Класс расширяет BroadcastReceiver и служит для отслеживания изменений
 * статуса и уровня заряда батареи устройства
 * */
class BatteryBroadcastReceiver(private val batteryListener: BatteryListener) : BroadcastReceiver() {

    /**
     * Метод получает из Intent и преобразует к нужному виду параметры
     * батареи. Данные передаются через интерфейс
     * */
    override fun onReceive(context: Context?, intent: Intent?) {
        val deviceStatus = intent!!.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val level = intent!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent!!.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryLevel = (level.toFloat() / scale.toFloat() * 100.0f).toInt()

        batteryListener.onBatteryStatusChanged(batteryLevel, deviceStatus)
    }

}