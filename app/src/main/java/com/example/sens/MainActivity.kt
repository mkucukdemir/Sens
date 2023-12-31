package com.example.sens

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_ACCELEROMETER
import android.hardware.Sensor.TYPE_ALL
import android.hardware.Sensor.TYPE_GYROSCOPE
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.os.Looper
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
import java.util.concurrent.locks.ReentrantLock

class MainActivity : ComponentActivity() {
    private val SENSOR_PERMISSION_REQUEST_CODE = 1
    private val STORAGE_PERMISSION_REQUEST_CODE = 123
    private lateinit var sensorManager: SensorManager
    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS")

    private lateinit var accelerometerSensor: Sensor
    private lateinit var gyroscopeSensor: Sensor
    private lateinit var locationManager: LocationManager

    private val accelerometerLock = ReentrantLock()
    private val gyroscopeLock = ReentrantLock()
    private val gpsLock = ReentrantLock()

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

        }

        // Request sensor permission when the activity is created
        requestSensorPermission()

        // Start sensor data collection
        startSensorDataCollection()
    }

    private fun requestSensorPermission() {
        val locationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION
        val sensorPermission = android.Manifest.permission.BODY_SENSORS

        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, locationPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(locationPermission)
        }

        if (ContextCompat.checkSelfPermission(this, sensorPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(sensorPermission)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), SENSOR_PERMISSION_REQUEST_CODE)
        }

        val permissions = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_REQUEST_CODE)
    }


    private fun startSensorDataCollection() {
        val sensorThread = Thread {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            accelerometerSensor = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
            gyroscopeSensor = sensorManager.getDefaultSensor(TYPE_GYROSCOPE)

            val accelerometerListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val sensorValues = event.values.joinToString(", ")
                    val sensorData = "$sensorValues"
                    writeDataToFile(sensorData, "accelerometer_data.txt", accelerometerLock)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // Handle accuracy changes if needed
                }
            }

            val gyroscopeListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val sensorValues = event.values.joinToString(", ")
                    val sensorData = "$sensorValues"
                    writeDataToFile(sensorData, "gyroscope_data.txt", gyroscopeLock)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // Handle accuracy changes if needed
                }
            }

            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val locationData = "${location.latitude}, ${location.longitude}"
                    writeDataToFile(locationData, "gps_data.txt", gpsLock)
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }

            sensorManager.registerListener(
                accelerometerListener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )

            sensorManager.registerListener(
                gyroscopeListener,
                gyroscopeSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )

            // Request GPS updates
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.requestLocationUpdates(
                    //LocationManager.GPS_PROVIDER,
                    //LocationManager.PASSIVE_PROVIDER,
                    LocationManager.NETWORK_PROVIDER,
                    0,
                    0f,
                    locationListener,
                    Looper.getMainLooper()
                )
            }
        }

        sensorThread.start()
    }

    private fun writeDataToFile(data: String, fileName: String, lock: ReentrantLock) {
        lock.lock()
        //println(data)
        try {
            val file = File(Environment.getExternalStorageDirectory(), fileName)
            val writer = FileWriter(file, true) // Append data to the file

            writer.write(sdf.format(Date()) + " " + data + "\n")

            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            lock.unlock()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Running",
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
