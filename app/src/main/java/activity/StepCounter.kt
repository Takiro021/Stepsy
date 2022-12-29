package activity

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.stepsy.R

class StepCounter : AppCompatActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null

    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stepcounter)
        loadData()
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

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)

        previousTotalSteps = savedNumber
    }

    /**When saving steps, we need also to save the
     * date so we can check if there is a new day
     * when the application is launched*/
    private fun saveData() {
        // Shared Preferences will allow us to save
        // and retrieve data in the form of key,value pair.
        // In this function we will save data
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }


    private fun resetSteps() {
        //TODO("Reset steps when there is a new day")

        val tvStepsTaken = findViewById<TextView>(R.id.todays_progress)
        tvStepsTaken.setOnClickListener {
            // This will give a toast message if the user want to reset the steps
            Toast.makeText(this, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }

        tvStepsTaken.setOnLongClickListener {

            previousTotalSteps = totalSteps.toFloat()

            // When the user will click long tap on the screen,
            // the steps will be reset to 0
            tvStepsTaken.text = getString(R.string.default_goal,"0")

            // This will save the data
            saveData()

            true
        }
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        val tvStepsProgress = findViewById<TextView>(R.id.todays_progress)
        val tvProgressBar = findViewById<ProgressBar>(R.id.simpleProgressBar)

        if(running) {
            totalSteps = sensorEvent!!.values[0]
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            tvStepsProgress.text = getString(R.string.default_goal,"$currentSteps")
            tvProgressBar.progress = currentSteps
            // Todo: progressbar should be daily step goal
            // tvProgressBar.max = 50
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

}