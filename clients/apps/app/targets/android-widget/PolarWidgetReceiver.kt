package com.polarsource.Polar.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import android.util.Log

class PolarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PolarWidget()

    private val scope = MainScope()

    override fun onUpdate(context: Context, appWidgetManager: android.appwidget.AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateWidgetData(context)
    }

    private fun updateWidgetData(context: Context) {
        scope.launch {
            try {
                val sharedPrefs = context.getSharedPreferences("group.com.polarsource.Polar", Context.MODE_PRIVATE)
                val apiToken = sharedPrefs.getString("widget_api_token", null)
                val organizationId = sharedPrefs.getString("widget_organization_id", null)
                val days = 30

                if (apiToken != null && organizationId != null) {
                    val metrics = MetricsFetcher.fetchMetrics(apiToken, organizationId, days)
                    if (metrics != null) {
                        val glanceManager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
                        val glanceIds = glanceManager.getGlanceIds(PolarWidget::class.java)
                        
                        glanceIds.forEach { glanceId ->
                            androidx.glance.appwidget.state.updateAppWidgetState(context, glanceId) { prefs ->
                            }
                        }
                    }
                }
                
                PolarWidget().updateAll(context)
            } catch (e: Exception) {
                Log.e("PolarWidget", "Error in onUpdate", e)
            }
        }
    }
}
