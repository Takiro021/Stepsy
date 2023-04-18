package com.example.stepsy

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.stepsy.databinding.FragmentHomeBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HomeFragment : Fragment(), SensorEventListener {
    companion object {
        const val PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 100
    }
    private lateinit var binding: FragmentHomeBinding
    private var sensorManager: SensorManager?= null
    private var running = false

    private var totalAmountOfSteps = 0f
    private var previousSteps = 0f
    private var dailyGoal = 10000
    private var currentSteps = 0f

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkSensorPermission(Manifest.permission.ACTIVITY_RECOGNITION, PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION)
        getDailyGoalFromSharedPreferences()
        loadProgressFromSharedPreferences()
        sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment using view binding
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.simpleProgressBar.max = dailyGoal
        return binding.root
    }

    private fun checkSensorPermission(permission: String, requestCode: Int) {
        if(context?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.ACTIVITY_RECOGNITION) } != PackageManager.PERMISSION_GRANTED) {
            activity?.let { ActivityCompat.requestPermissions(it, arrayOf(permission), requestCode) }
        }
    }

    private fun getDailyGoalFromSharedPreferences() {
        val fileName = "com.example.stepsy_preferences"
        val sharedPreferences = requireContext().getSharedPreferences(fileName, MODE_PRIVATE)

        try {
            dailyGoal = sharedPreferences.getString("dailyGoal", "")?.toInt() ?: 10000
        } catch (e: NumberFormatException) {
            // or you can log an error here to help with debugging
            Log.e(TAG, "Error parsing daily goal from shared preferences: ${e.message}")
        }
    }

    private fun loadProgressFromSharedPreferences() {
        var fileName = "stepsyData" + getDayInWeek()
        var sharedPreferences = requireContext().getSharedPreferences(fileName, MODE_PRIVATE)
        val savedDate = sharedPreferences.getString("date", "")
        var savedSteps = sharedPreferences.getFloat("stepsTotal", 0f)

        if (savedDate != getCurrentDate()) {
            fileName = "stepsyData" + getYesterday()
            sharedPreferences = requireContext().getSharedPreferences(fileName, MODE_PRIVATE)
            savedSteps = sharedPreferences.getFloat("stepsTotal", 0f)
        } else {
            val current = sharedPreferences.getFloat("stepsToday", 0f)
            savedSteps -= current
        }

        previousSteps = savedSteps
    }

    /**When saving steps, we need also to save the
     * date so we can check if there is a new day
     * when the application is launched*/
    private fun saveProgressToSharedPreferences() {
        val currentDate = getCurrentDate()
        val fileName = "stepsyData" + getDayInWeek()

        val sharedPreferences = requireContext().getSharedPreferences(fileName, MODE_PRIVATE) ?: return
        with (sharedPreferences.edit()) {
            putFloat("stepsToday", currentSteps)
            putString("date", currentDate)
            putFloat("stepsTotal", totalAmountOfSteps)

            println("123: Total $totalAmountOfSteps")
            println("123: Current $currentSteps")
            apply()
        }
    }

    private fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDateTime.now().format(formatter)
    }

    private fun getDayInWeek(): String {
        val instant: Instant = Instant.now()
        val zoneDateTime: ZonedDateTime = instant.atZone(ZoneId.of("ECT"))
        return zoneDateTime.dayOfWeek.toString()
    }

    private fun getYesterday(): String {
        val instant: Instant = Instant.now()
        val yesterday: Instant = instant.minus(1, ChronoUnit.DAYS)
        val zoneDateTime: ZonedDateTime = yesterday.atZone(ZoneId.of("ECT"))
        return zoneDateTime.dayOfWeek.toString()
    }


    override fun onResume() {
        super.onResume()
        running = true

        val syncSteps = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (syncSteps == null) {
            Toast.makeText(requireContext(), "NO SENSOR FOUND", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, syncSteps, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        saveProgressToSharedPreferences()
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(running) {
            totalAmountOfSteps = event!!.values[0]

            currentSteps = totalAmountOfSteps - previousSteps

            binding.textViewSteps.text = getString(
                R.string.progress_and_goal,
                "${currentSteps.toInt()}",
                dailyGoal.toString()
            )
            binding.simpleProgressBar.progress = currentSteps.toInt()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
