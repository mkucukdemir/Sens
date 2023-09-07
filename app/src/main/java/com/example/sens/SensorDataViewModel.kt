package com.example.sens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SensorDataViewModel : ViewModel() {
    var sensorData: String by mutableStateOf("")
}

