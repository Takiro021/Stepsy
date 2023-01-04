package activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.stepsy.R
import notifications.NotificationHelper
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class StepCounterActivity : AppCompatActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
    private var defaultTotalGoal = 5000

    companion object {
        private const val PHYSICAL_ACTIVITY_CODE = 100
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO Move to splashscreen activity
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.scheduleWalkNotification(this)

        setContentView(R.layout.activity_stepcounter)
        checkSensorPermission(Manifest.permission.ACTIVITY_RECOGNITION, PHYSICAL_ACTIVITY_CODE)
        loadProgressFromSharedPreferences()
        resetSteps()
        sensorManager = getSystemService((Context.SENSOR_SERVICE)) as SensorManager
    }

    override fun onResume() {
        super.onResume()
        running = true
        val syncSteps = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (syncSteps == null) {
            Toast.makeText(this, R.string.step_counter_no_sensor, Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, syncSteps, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun loadProgressFromSharedPreferences() {
        val fileName = "stepsyData" + getDayInWeek()
        val sharedPreferences = getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val savedSteps = sharedPreferences.getFloat("stepsToday", 0f)
        val savedDate = sharedPreferences.getString("date", "")

        previousTotalSteps = if (savedDate == getCurrentDate()) {
            savedSteps
        } else {
            0f
        }
    }

    /**When saving steps, we need also to save the
     * date so we can check if there is a new day
     * when the application is launched*/
    private fun saveProgressToSharedPreferences() {
        val currentDate = getCurrentDate()
        val fileName = "stepsyData" + getDayInWeek()

        val sharedPreferences = getSharedPreferences(fileName, Context.MODE_PRIVATE) ?: return
        with (sharedPreferences.edit()) {
            putFloat("stepsToday", previousTotalSteps)
            putString("date", currentDate)
            apply()
        }
    }

    /** Check if access to the activity sensor is granted */
    private fun checkSensorPermission(permission: String, requestCode: Int) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
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

    private fun resetSteps() {
        //TODO("Reset steps when there is a new day")

        val tvStepsTaken = findViewById<TextView>(R.id.todays_progress)
        tvStepsTaken.setOnClickListener {
            Toast.makeText(this, "Hold to reset steps", Toast.LENGTH_SHORT).show()
        }

        tvStepsTaken.setOnLongClickListener {
            previousTotalSteps = totalSteps
            tvStepsTaken.text = getString(R.string.progress_and_goal,"0", defaultTotalGoal.toString())
            saveProgressToSharedPreferences()
            true
        }
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        val tvStepsProgress = findViewById<TextView>(R.id.todays_progress)
        val tvProgressBar = findViewById<ProgressBar>(R.id.simpleProgressBar)

        if(running) {
            totalSteps = sensorEvent!!.values[0]
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            tvStepsProgress.text = getString(R.string.progress_and_goal,"$currentSteps", defaultTotalGoal.toString())
            tvProgressBar.progress = currentSteps
            // Todo: progressbar should be daily step goal
            tvProgressBar.max = defaultTotalGoal
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

}