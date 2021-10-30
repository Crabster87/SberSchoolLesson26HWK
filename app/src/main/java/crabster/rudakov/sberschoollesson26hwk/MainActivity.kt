package crabster.rudakov.sberschoollesson26hwk

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val launchServiceButton: AppCompatButton = findViewById(R.id.launch_service_button)
        launchServiceButton.setOnClickListener { startService() }

    }

    private fun startService() {
        val intent = Intent(this, ServiceWorker::class.java)
        intent.action = ServiceWorker.ACTION_START_SERVICE
        startService(intent)
    }

}