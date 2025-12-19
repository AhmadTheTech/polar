package com.polarsource.Polar.widget

import kotlinx.serialization.Serializable

@Serializable
data class MetricsResponse(
    val totals: MetricsTotals,
    val periods: [MetricsPeriod]
)

@Serializable
data class MetricsTotals(
    val revenue: Int? = null,
    val orders: Int? = null
)

@Serializable
data class MetricsPeriod(
    val timestamp: String,
    val revenue: Int? = null,
    val orders: Int? = null
)

data class RevenueData(
    val day: Int,
    val amount: Double
)

data class CombinedMetrics(
    val revenueValue: Int,
    val revenueChartData: List<RevenueData>,
    val ordersValue: Int,
    val ordersChartData: List<RevenueData>,
    val averageOrderValue: Double,
    val averageOrderValueChartData: List<RevenueData>
)
