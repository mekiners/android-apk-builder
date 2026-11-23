package com.example.sampleapp.util

import android.content.Context

/** User-adjustable numbers for pay, punctuality, leave and the time budget (stored in SharedPreferences). */
data class AppSettings(
    val hourlyRate: Double = 0.0,
    val overtimeAfterHours: Double = 8.0,
    val overtimeMultiplier: Double = 1.25,
    val nightDiffPercent: Double = 0.0,
    val graceMinutes: Int = 5,
    val vacationDays: Int = 15,
    val sickDays: Int = 7,
    val sleepHours: Double = 7.0,
    val currency: String = "\u20B1"
)

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("cozy_settings", Context.MODE_PRIVATE)

    private fun d(key: String, def: Double): Double = prefs.getString(key, null)?.toDoubleOrNull() ?: def
    private fun i(key: String, def: Int): Int = prefs.getString(key, null)?.toIntOrNull() ?: def

    fun load(): AppSettings {
        val def = AppSettings()
        return AppSettings(
            hourlyRate = d("rate", def.hourlyRate),
            overtimeAfterHours = d("ot_after", def.overtimeAfterHours),
            overtimeMultiplier = d("ot_mult", def.overtimeMultiplier),
            nightDiffPercent = d("night", def.nightDiffPercent),
            graceMinutes = i("grace", def.graceMinutes),
            vacationDays = i("vacation", def.vacationDays),
            sickDays = i("sick", def.sickDays),
            sleepHours = d("sleep", def.sleepHours),
            currency = prefs.getString("currency", null) ?: def.currency
        )
    }

    fun save(s: AppSettings) {
        prefs.edit()
            .putString("rate", s.hourlyRate.toString())
            .putString("ot_after", s.overtimeAfterHours.toString())
            .putString("ot_mult", s.overtimeMultiplier.toString())
            .putString("night", s.nightDiffPercent.toString())
            .putString("grace", s.graceMinutes.toString())
            .putString("vacation", s.vacationDays.toString())
            .putString("sick", s.sickDays.toString())
            .putString("sleep", s.sleepHours.toString())
            .putString("currency", s.currency)
            .apply()
    }
}
