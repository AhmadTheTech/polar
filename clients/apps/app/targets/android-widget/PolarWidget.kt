package com.polarsource.Polar.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf

class PolarWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val isAuthorized = prefs[stringPreferencesKey("widget_api_token")] != null
            val isError = prefs[booleanPreferencesKey("is_error")] ?: false
            
            val primaryBlue = ColorProvider(Color(0xFF005FFF))
            val textPrimary = ColorProvider(day = Color.Black, night = Color.White)
            val textSecondary = ColorProvider(day = Color.Black.copy(alpha = 0.6f), night = Color.White.copy(alpha = 0.6f))

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .appWidgetBackground()
                    .background(ColorProvider(day = Color(0xFFF2F2F7), night = Color(0xFF0D0E10)))
                    .padding(8.dp)
            ) {
                if (!isAuthorized) {
                    UnauthorizedView(textPrimary)
                } else if (isError) {
                    ErrorView(textPrimary)
                } else {
                    MetricsView(prefs, textPrimary, textSecondary)
                }
            }
        }
    }

    @Composable
    private fun UnauthorizedView(textColor: ColorProvider) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Log in to see your analytics",
                style = TextStyle(color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            )
        }
    }

    @Composable
    private fun ErrorView(textColor: ColorProvider) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error fetching data",
                style = TextStyle(color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
            )
        }
    }

    @Composable
    private fun MetricsView(prefs: Preferences, textColor: ColorProvider, secondaryTextColor: ColorProvider) {
        val metricLabel = prefs[stringPreferencesKey("widget_metric_label")] ?: "Revenue"
        val timeFrame = prefs[stringPreferencesKey("widget_time_frame")] ?: "30 days"
        val formattedValue = prefs[stringPreferencesKey("widget_metric_value")] ?: "$\u2014"

        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.polar_logo),
                    contentDescription = "Logo",
                    modifier = GlanceModifier.size(18.dp)
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Text(
                    text = "$metricLabel | $timeFrame",
                    style = TextStyle(color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = formattedValue,
                    style = TextStyle(color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
            }
            
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            val chartBitmap = ChartRenderer.render(400, 200, emptyList())
            
            Image(
                provider = ImageProvider(chartBitmap),
                contentDescription = "Chart",
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}
