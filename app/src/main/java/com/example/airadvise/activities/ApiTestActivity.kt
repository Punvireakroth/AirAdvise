package com.example.airadvise.activities

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.airadvise.api.ApiClient
import com.example.airadvise.api.ApiService
import com.example.airadvise.databinding.ActivityApiTestBinding
import com.example.airadvise.models.AirQualityData
import com.example.airadvise.models.User
import com.example.airadvise.utils.LogUtils
import com.google.gson.Gson

class ApiTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApiTestBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApiTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiClient.createApiService(this)

        binding.btnTestApi.setOnClickListener {
            testApiWithMockData()
        }
    }

    private fun testApiWithMockData() {
        val gson = Gson()

        // Test User deserialization
        val userJson = """
            {
                "id": 1,
                "name": "Test User",
                "email": "test@example.com",
                "role": "user"
            }
        """.trimIndent()

        try {
            val user = gson.fromJson(userJson, User::class.java)
            LogUtils.d("Successfully parsed User: ${user.name}")

            // Test AirQualityData deserialization
            val airQualityJson = """
                {
                    "id": 1,
                    "location_id": 1,
                    "aqi": 85,
                    "pm25": 25.4,
                    "pm10": 48.2,
                    "o3": 35.1,
                    "no2": 15.2,
                    "so2": 5.1,
                    "co": 0.8,
                    "category": "Moderate",
                    "source": "Test API",
                    "timestamp": "2023-06-15T10:30:00Z"
                }
            """.trimIndent()

            val airQualityData = gson.fromJson(airQualityJson, AirQualityData::class.java)
            LogUtils.d("Successfully parsed AQI data: ${airQualityData.aqi}")

            binding.tvTestResult.text = "API models tested successfully"
            binding.tvTestResult.setTextColor(Color.GREEN)
        } catch (e: Exception) {
            LogUtils.e("Failed to parse JSON", e)
            binding.tvTestResult.text = "Error: ${e.message}"
            binding.tvTestResult.setTextColor(Color.RED)
        }
    }
}