package com.eiman.servifast.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {
    @SuppressLint("ObsoleteSdkInt")
    fun setLocale(context: Context, language: String): Context {
        Locale.setDefault(Locale(language))
        val res    = context.resources
        val config = Configuration(res.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(Locale(language))
            return context.createConfigurationContext(config)
        } else {
            config.locale = Locale(language)
            @Suppress("DEPRECATION")
            res.updateConfiguration(config, res.displayMetrics)
            return context
        }
    }
}
