package com.example.sampleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sampleapp.data.GroceryEntity
import com.example.sampleapp.data.LeaveDayEntity
import com.example.sampleapp.data.QuestLogEntity
import com.example.sampleapp.data.ShiftEntity
import com.example.sampleapp.data.TimeLogEntity
import com.example.sampleapp.data.WorkoutLogEntity
import com.example.sampleapp.ui.theme.*
import com.example.sampleapp.util.AppSettings
import com.example.sampleapp.util.Stats
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// 11 colours, 2 points each: tan (0-1) -> amber (middle, 10-11) -> red (20+). Edit these three to restyle the heatmap.
private val HEAT_TAN = Color(0xFFE4CFA9)
private val HEAT_AMBER = Color(0xFFE8A030)
private val HEAT_RED = Color(0xFFB8322A)
private val HEAT_COLORS: List<Color> = List(Stats.HEAT_LEVELS) { i ->
    val mid = Stats.HEAT_LEVELS / 2
    if (i <= mid) lerp(HEAT_TAN, HEAT_AMBER, i / mid.toFloat())
    else lerp(HEAT_AMBER, HEAT_RED, (i - mid) / (Stats.HEAT_LEVELS - 1 - mid).toFloat())
}

@Composable
fun StatsScreen(
    shifts: List<ShiftEntity>,
    timeLogs: List<TimeLogEntity>,
    questLog: List<QuestLogEntity>,
    workoutLogs: List<WorkoutLogEntity>,
    groceries: List<GroceryEntity>,
    leaveDays: List<LeaveDayEntity>,
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit
) {
    var lastWeek by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val thisWeekStart = Stats.weekStart(today)
    val shownWeek = if (lastWeek) thisWeekStart.minusWeeks(1) else thisWeekStart
    val recap = Stats.recap(shownWeek, questLog, timeLogs, workoutLogs, groceries)
    val heat = remember(questLog, timeLogs, workoutLogs) { Stats.heatmap(12, LocalDate.now(), questLog, timeLogs, workoutLogs) }

    val monthStart = today.withDayOfMonth(1)
    val nextMonth = monthStart.plusMonths(1)
    val weekFrom = thisWeekStart.toEpochDay()
    val payWeek = Stats.pay(timeLogs, weekFrom, weekFrom + 7, settings)
    val payMonth = Stats.pay(timeLogs, monthStart.toEpochDay(), nextMonth.toEpochDay(), settings)
    val projected = Stats.projectedPay(shifts, LocalDateTime.now(), monthStart.toEpochDay(), nextMonth.toEpochDay(), settings)
    val punctuality = Stats.punctuality(timeLogs, shifts, settings, LocalDateTime.now())
    val leave = Stats.leaveUsed(leaveDays, today.year)
    val budget = Stats.timeBudget(thisWeekStart, shifts, settings)
    val cur = settings.currency
    val rangeFmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header + settings
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PixelIcon(Sprites.chart, 28.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Your numbers", style = StardewTypography.titleMedium)
                    Text("Recap, pay, punctuality, leave and time.", style = StardewTypography.bodySmall, color = StardewTextPrimary)
                }
                StardewChip("Settings", false, { showSettings = true }, sprite = Sprites.gear)
            }
        }

        // Weekly recap
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Weekly recap", sprite = Sprites.scroll)
                StardewChip("This week", !lastWeek, { lastWeek = false })
                StardewChip("Last week", lastWeek, { lastWeek = true })
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${shownWeek.format(rangeFmt)} - ${shownWeek.plusDays(6).format(rangeFmt)}",
                style = StardewTypography.bodySmall,
                color = StardewTextPrimary
            )
            Spacer(Modifier.height(4.dp))
            StatRow("Quests done", "${recap.quests}  (+${recap.gold}g)")
            StatRow("Hours worked", "${Stats.hoursLabel(recap.hours)} in ${recap.shifts} shifts")
            StatRow("Gym days", "${recap.gymSessions}")
            StatRow("Groceries bought", Stats.money(cur, recap.spend))
        }

        // Heatmap
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
            SectionLabel("Last 12 weeks", sprite = Sprites.calendar)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                heat.forEach { week ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        week.forEach { level ->
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .then(
                                        if (level >= 0) Modifier.background(HEAT_COLORS[level]).border(1.dp, StardewWoodDark)
                                        else Modifier
                                    )
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("0", style = StardewTypography.labelSmall)
                HEAT_COLORS.forEach { c -> Box(Modifier.size(12.dp).background(c).border(1.dp, StardewWoodDark)) }
                Text("20+", style = StardewTypography.labelSmall)
            }
            Text(
                "Points per day: hard quest ${Stats.HEAT_HARD}, medium ${Stats.HEAT_MEDIUM}, easy ${Stats.HEAT_EASY}, " +
                    "shift ${Stats.HEAT_SHIFT}, overtime ${Stats.HEAT_OVERTIME}, gym ${Stats.HEAT_TRAINING}. Each colour = 2 points, red = 20+.",
                style = StardewTypography.bodySmall,
                color = StardewTextPrimary
            )
        }

        // Work: pay
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
            SectionLabel("Pay estimate", sprite = Sprites.bag)
            Spacer(Modifier.height(4.dp))
            if (settings.hourlyRate <= 0.0) {
                Text("Set your hourly rate in Settings to see earnings. Hours are counted below.", style = StardewTypography.bodySmall, color = StardewTextPrimary)
                Spacer(Modifier.height(4.dp))
            }
            StatRow("This week", Stats.money(cur, payWeek.amount) + "  (" + Stats.hoursLabel(payWeek.regularHours + payWeek.overtimeHours) + ")")
            StatRow("This month so far", Stats.money(cur, payMonth.amount) + "  (" + Stats.hoursLabel(payMonth.regularHours + payMonth.overtimeHours) + ")")
            StatRow("Month estimate", Stats.money(cur, payMonth.amount + projected.amount))
            StatRow("Overtime this month", Stats.hoursLabel(payMonth.overtimeHours))
            StatRow("Night hours this month", Stats.hoursLabel(payMonth.nightHours))
            Text(
                "Estimate only: it uses your Settings and the punches you recorded. Allowances and tax are not included.",
                style = StardewTypography.bodySmall,
                color = StardewTextPrimary
            )
        }

        // Work: punctuality
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
            SectionLabel("Punctuality", sprite = Sprites.clock)
            Spacer(Modifier.height(4.dp))
            if (punctuality.samples == 0) {
                Text("Time in on a scheduled shift to start collecting stats.", style = StardewTypography.bodySmall, color = StardewTextPrimary)
            } else {
                val off = punctuality.avgOffsetMinutes
                StatRow("On time", "${punctuality.onTimePercent}% of ${punctuality.samples} shifts")
                StatRow("Average arrival", if (off < 0) "${-off} min early" else if (off == 0) "right on time" else "$off min late")
                StatRow("Average shift length", Stats.hoursLabel(punctuality.avgShiftMinutes / 60.0))
                StatRow("On-time streak", "${punctuality.onTimeStreak} shifts")
                StatRow("In and out streak", "${punctuality.punchStreak} shifts")
            }
        }

        // Leave
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
            SectionLabel("Leave ${today.year}", sprite = Sprites.sun)
            Spacer(Modifier.height(4.dp))
            StatRow("Vacation", "${leave.vacation} used, ${maxOf(0, settings.vacationDays - leave.vacation)} left of ${settings.vacationDays}")
            StatRow("Sick", "${leave.sick} used, ${maxOf(0, settings.sickDays - leave.sick)} left of ${settings.sickDays}")
            Text("Mark leave on the Shifts calendar with the Leave brush.", style = StardewTypography.bodySmall, color = StardewTextPrimary)
        }

        // Time budget
        StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 4.dp) {
            SectionLabel("Time budget (this week)", sprite = Sprites.moon)
            Spacer(Modifier.height(6.dp))
            val total = 168.0
            Row(modifier = Modifier.fillMaxWidth().height(18.dp).border(2.dp, StardewWoodDark)) {
                if (budget.shiftHours > 0) Box(Modifier.weight(budget.shiftHours.toFloat()).fillMaxHeight().background(StardewTerracotta))
                if (budget.sleepHours > 0) Box(Modifier.weight(budget.sleepHours.toFloat()).fillMaxHeight().background(Color(0xFF4F7FB8)))
                if (budget.freeHours > 0) Box(Modifier.weight(budget.freeHours.toFloat()).fillMaxHeight().background(StardewGreen))
                if (total - budget.shiftHours - budget.sleepHours - budget.freeHours > 0.5) {
                    Box(Modifier.weight((total - budget.shiftHours - budget.sleepHours - budget.freeHours).toFloat()).fillMaxHeight().background(StardewParchmentDark))
                }
            }
            Spacer(Modifier.height(6.dp))
            StatRow("Shifts", Stats.hoursLabel(budget.shiftHours))
            StatRow("Sleep (${Stats.fmtKg(settings.sleepHours)}h a night)", Stats.hoursLabel(budget.sleepHours))
            StatRow("Free time", Stats.hoursLabel(budget.freeHours))
        }
    }

    if (showSettings) {
        SettingsDialog(settings = settings, onSave = onSaveSettings, onDismiss = { showSettings = false })
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, style = StardewTypography.bodyMedium, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        Text(value, style = StardewTypography.bodyLarge)
    }
}

@Composable
private fun SettingsDialog(settings: AppSettings, onSave: (AppSettings) -> Unit, onDismiss: () -> Unit) {
    var rate by remember { mutableStateOf(if (settings.hourlyRate > 0) Stats.fmtKg(settings.hourlyRate) else "") }
    var otAfter by remember { mutableStateOf(Stats.fmtKg(settings.overtimeAfterHours)) }
    var otMult by remember { mutableStateOf(settings.overtimeMultiplier.toString()) }
    var night by remember { mutableStateOf(Stats.fmtKg(settings.nightDiffPercent)) }
    var grace by remember { mutableStateOf(settings.graceMinutes.toString()) }
    var vacation by remember { mutableStateOf(settings.vacationDays.toString()) }
    var sick by remember { mutableStateOf(settings.sickDays.toString()) }
    var sleep by remember { mutableStateOf(Stats.fmtKg(settings.sleepHours)) }
    var currency by remember { mutableStateOf(settings.currency) }

    fun num(v: String): Double? = v.replace(',', '.').trim().toDoubleOrNull()

    StardewDialog(
        title = "Settings",
        confirmText = "Save",
        onDismiss = onDismiss,
        onConfirm = {
            onSave(
                AppSettings(
                    hourlyRate = num(rate) ?: 0.0,
                    overtimeAfterHours = num(otAfter) ?: settings.overtimeAfterHours,
                    overtimeMultiplier = num(otMult) ?: settings.overtimeMultiplier,
                    nightDiffPercent = num(night) ?: 0.0,
                    graceMinutes = grace.trim().toIntOrNull() ?: settings.graceMinutes,
                    vacationDays = vacation.trim().toIntOrNull() ?: settings.vacationDays,
                    sickDays = sick.trim().toIntOrNull() ?: settings.sickDays,
                    sleepHours = num(sleep) ?: settings.sleepHours,
                    currency = currency.trim().ifEmpty { settings.currency }
                )
            )
            onDismiss()
        }
    ) {
        SettingField("Hourly rate", rate, { rate = it }, "e.g. 250")
        SettingField("Overtime quick-fill after (hours per shift)", otAfter, { otAfter = it }, "8")
        SettingField("Overtime multiplier", otMult, { otMult = it }, "1.25")
        SettingField("Night differential % (22:00-06:00)", night, { night = it }, "0 or 10")
        SettingField("On-time grace (minutes late)", grace, { grace = it }, "5")
        SettingField("Vacation days per year", vacation, { vacation = it }, "15")
        SettingField("Sick days per year", sick, { sick = it }, "7")
        SettingField("Sleep hours per night", sleep, { sleep = it }, "7")
        SettingField("Currency symbol", currency, { currency = it }, "\u20B1", number = false)
    }
}

@Composable
private fun SettingField(label: String, value: String, onChange: (String) -> Unit, hint: String, number: Boolean = true) {
    Text(label, style = StardewTypography.bodySmall, color = StardewTextPrimary)
    StardewTextField(
        value = value,
        onValueChange = onChange,
        placeholder = hint,
        modifier = Modifier.fillMaxWidth(),
        height = 38.dp,
        keyboardType = if (number) KeyboardType.Decimal else KeyboardType.Text
    )
}
