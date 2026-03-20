package com.example.batterymonitorpro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


class MainActivity : AppCompatActivity() {

    private lateinit var batteryPercentText: TextView
    private lateinit var batteryStatusText: TextView
    private lateinit var healthText: TextView
    private lateinit var tempText: TextView
    private lateinit var voltageText: TextView
    private lateinit var techText: TextView
    private lateinit var batteryProgress: ProgressBar

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 0

                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val statusString = when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> getString(R.string.status_charging)
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> getString(R.string.status_discharging)
                    BatteryManager.BATTERY_STATUS_FULL -> getString(R.string.status_full)
                    else -> "UNKNOWN"
                }

                val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                val healthString = when (health) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "GOOD"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OVERHEAT"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "DEAD"
                    else -> "HEALTHY"
                }

                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) / 1000.0
                val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

                updateUI(batteryPct, statusString, healthString, temp, voltage, tech)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Control navigation icon color
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightNavigationBars = false  // false → light icons on transparent background
        controller.isAppearanceLightStatusBars = false


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        batteryPercentText = findViewById(R.id.batteryPercentText)
        batteryStatusText = findViewById(R.id.batteryStatusText)
        healthText = findViewById(R.id.healthText)
        tempText = findViewById(R.id.tempText)
        voltageText = findViewById(R.id.voltageText)
        techText = findViewById(R.id.techText)
        batteryProgress = findViewById(R.id.batteryProgress)
    }

    private fun updateUI(percent: Int, status: String, health: String, temp: Double, voltage: Double, tech: String) {
        batteryPercentText.text = getString(R.string.battery_percent_format, percent)
        batteryStatusText.text = status
        healthText.text = health
        tempText.text = getString(R.string.temp_format, temp)
        voltageText.text = getString(R.string.voltage_format, voltage)
        techText.text = tech
        batteryProgress.progress = percent
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryReceiver)
    }
}
