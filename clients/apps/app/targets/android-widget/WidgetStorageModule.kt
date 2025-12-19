package com.polarsource.Polar.widget

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import android.content.Context
import android.content.SharedPreferences

class WidgetStorageModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("WidgetStorage")

    Function("setItem") { key: String, value: String ->
      val prefs = context.getSharedPreferences("group.com.polarsource.Polar", Context.MODE_PRIVATE)
      prefs.edit().putString(key, value).apply()
    }
    
    Function("getItem") { key: String ->
      val prefs = context.getSharedPreferences("group.com.polarsource.Polar", Context.MODE_PRIVATE)
      prefs.getString(key, null)
    }
  }

  private val context: Context
    get() = appContext.reactContext ?: throw Exception("React context not available")
}
