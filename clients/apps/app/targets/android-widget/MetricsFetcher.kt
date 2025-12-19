package com.polarsource.Polar.widget

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

object MetricsFetcher {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchMetrics(
        apiToken: String,
        organizationId: String,
        days: Int
    ): CombinedMetrics? {
        try {
            val endDate = Date()
            val calendar = Calendar.getInstance()
            calendar.time = endDate
            calendar.add(Calendar.DAY_OF_YEAR, -days)
            val startDate = calendar.time

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val startDateStr = dateFormat.format(startDate)
            val endDateStr = dateFormat.format(endDate)

            val urlString = "https://api.polar.sh/v1/metrics/?organization_id=$organizationId&start_date=$startDateStr&end_date=$endDateStr&interval=day&timezone=${TimeZone.getDefault().id}"
            val url = URL(urlString)
            
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $apiToken")
            connection.setRequestProperty("Accept", "application/json")

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val decoded = json.decodeFromString<MetricsResponse>(responseText)
                
                return transformMetrics(decoded)
            } else {
                Log.e("PolarWidget", "Error fetching metrics: ${connection.responseCode}")
                return null
            }
        } catch (e: Exception) {
            Log.e("PolarWidget", "Exception fetching metrics", e)
            return null
        }
    }

    private fun transformMetrics(response: MetricsResponse): CombinedMetrics {
        val revenueValue = (response.totals.revenue ?: 0) / 100
        val ordersValue = response.totals.orders ?: 0
        
        var cumulativeRevenue = 0.0
        val revenueChartData = response.periods.mapIndexed { index, period ->
            val periodValue = (period.revenue ?: 0).toDouble() / 100.0
            cumulativeRevenue += periodValue
            RevenueData(index + 1, cumulativeRevenue)
        }

        var cumulativeOrders = 0.0
        val ordersChartData = response.periods.mapIndexed { index, period ->
            val periodValue = (period.orders ?: 0).toDouble()
            cumulativeOrders += periodValue
            RevenueData(index + 1, cumulativeOrders)
        }

        val aovValue = if (ordersValue > 0) (response.totals.revenue ?: 0).toDouble() / ordersValue.toDouble() / 100.0 else 0.0
        
        var cumulativeAOV = 0.0
        val aovChartData = response.periods.mapIndexed { index, period ->
            val periodRevenue = period.revenue ?: 0
            val periodOrders = period.orders ?: 0
            val periodAOV = if (periodOrders > 0) periodRevenue.toDouble() / periodOrders.toDouble() / 100.0 else 0.0
            cumulativeAOV += periodAOV
            RevenueData(index + 1, cumulativeAOV)
        }

        return CombinedMetrics(
            revenueValue = revenueValue,
            revenueChartData = revenueChartData,
            ordersValue = ordersValue,
            ordersChartData = ordersChartData,
            averageOrderValue = aovValue,
            averageOrderValueChartData = aovChartData
        )
    }
}
