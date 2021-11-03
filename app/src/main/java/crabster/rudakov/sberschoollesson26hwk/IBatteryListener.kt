package crabster.rudakov.sberschoollesson26hwk

/**
 * Специализированный интерфейс, используемый для передачи параметров
 * батареи из BroadcastReceiver в Service
 * */
interface BatteryListener {

    /**
     * Метод передаёт статус и уровень заряда батареи
     * */
    fun onBatteryStatusChanged(batteryLevel: Int, deviceStatus: Int)

}