package com.example.sens

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sens.ui.theme.SensTheme
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    private val SENSOR_PERMISSION_REQUEST_CODE = 1
    private lateinit var sensorThread: Thread
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor
    private lateinit var sensorEventListener: SensorEventListener
    private val viewModel: SensorDataViewModel by viewModels()
    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")
    private var now: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SensTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }

            // Display the sensor data Composable
            DisplaySensorData(viewModel.sensorData)
        }

        // Request sensor permission when the activity is created
        requestSensorPermission()

        // Start sensor data collection
        startSensorDataCollection()
    }

    private fun requestSensorPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                SENSOR_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startSensorDataCollection() {
        sensorThread = Thread {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            sensorEventListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    // Handle sensor data here
                    val sensorValues = event.values.joinToString(", ")
                    viewModel.sensorData = "Sensor Data: $sensorValues"  // Update the greeting text
                    writeToSensorDataFile(event.values)  // Write data to the file
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // Handle accuracy changes if needed
                }
            }

            sensorManager.registerListener(
                sensorEventListener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        sensorThread.start()
    }


    private fun writeToSensorDataFile(sensorData: FloatArray) {
        try {
            val file = File(getExternalFilesDir(null), "sensor_data.txt")
            val writer = FileWriter(file, true) // Append data to the file

            now = Date()
            writer.write("[" + sdf.format(now) + "] " + sensorData.joinToString(", ") + "\n")

            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

@Composable
fun DisplaySensorData(sensorData: String) {
    LaunchedEffect(sensorData) {
        // This block will be executed whenever sensorData changes
        // Update the UI to display the new sensorData
    }

    Text(
        text = sensorData,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SensTheme {
        Greeting("Android")
    }
}
