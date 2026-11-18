package com.example.sampleapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DayDutyRecord(
    val date: LocalDate,
    val morning: DutyShiftType = DutyShiftType.OFF,
    val evening: DutyShiftType = DutyShiftType.OFF,
    val night: DutyShiftType = DutyShiftType.OFF
)

enum class DutyShiftType(val label: String, val color: Color) {
    OFF("Off Duty", StardewParchmentDark),
    DAY("Morning Shift", StardewGreen),
    EVENING("Evening Shift", StardewGold),
    NIGHT("Midnight Shift", StardewTerracotta),
    STANDBY("Standby", StardewStandby)
}

@Composable
fun ShiftAlmanacScreen(coinBalance: Int, onMenuClick: () -> Unit) {
    val startDate = remember { LocalDate.now().withDayOfMonth(1) }
    val daysOfWeekLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedShiftIndex by remember { mutableIntStateOf(0) }
    
    // Local state map for testing the grid
    val dutySchedule = remember { mutableStateMapOf<LocalDate, DayDutyRecord>() }

    val shiftTimeWindows = listOf("Morning (06:00-14:00)", "Evening (14:00-22:00)", "Midnight (22:00-06:00)")

    Column(modifier = Modifier.fillMaxSize().background(StardewCanvasBg)) {
        StardewWoodenBanner(title = "Shift Almanac", coinBalance = coinBalance, onMenuClick = onMenuClick)

        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StardewPanel(modifier = Modifier.fillMaxWidth()) {
                Text("ATC Rotation Log", style = StardewTypography.titleMedium)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Spacer(modifier = Modifier.width(34.dp))
                    (1..4).forEach { weekNum -> Text("Wk $weekNum", style = StardewTypography.labelSmall, modifier = Modifier.weight(1f)) }
                }
                Spacer(modifier = Modifier.height(6.dp))

                daysOfWeekLabels.forEachIndexed { dayIndex, dayName ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(dayName, style = StardewTypography.labelSmall, modifier = Modifier.width(34.dp))
                        (0..3).forEach { weekIndex ->
                            val cellDate = startDate.plusDays((weekIndex * 7 + dayIndex).toLong())
                            val record = dutySchedule[cellDate] ?: DayDutyRecord(cellDate)
                            val isSelectedDate = cellDate == selectedDate
                            
                            Box(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    listOf(record.morning, record.evening, record.night).forEachIndexed { index, shift ->
                                        val isSelected = isSelectedDate && selectedShiftIndex == index
                                        Box(modifier = Modifier.size(16.dp).background(shift.color).border(if (isSelected) 2.5.dp else 1.5.dp, if (isSelected) StardewTextPrimary else StardewWoodDark)
                                            .clickable { selectedDate = cellDate; selectedShiftIndex = index })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            StardewPanel(modifier = Modifier.fillMaxWidth(), contentPadding = 10.dp) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    listOf("Off", "Day", "Eve", "Night").forEachIndexed { i, label ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(if(i==0) StardewParchmentDark else if(i==1) StardewGreen else if(i==2) StardewGold else StardewTerracotta).border(1.dp, StardewWoodDark))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(label, style = StardewTypography.labelSmall)
                        }
                    }
                }
            }

            val activeRecord = dutySchedule[selectedDate] ?: DayDutyRecord(selectedDate)
            val currentShiftStatus = when (selectedShiftIndex) {
                0 -> activeRecord.morning
                1 -> activeRecord.evening
                else -> activeRecord.night
            }
            val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

            StardewPanel(modifier = Modifier.fillMaxWidth(), backgroundColor = StardewParchmentDark) {
                Text("$formattedDate • ${shiftTimeWindows[selectedShiftIndex]}", style = StardewTypography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Status: ${currentShiftStatus.label}", style = StardewTypography.bodyMedium)
                Spacer(modifier = Modifier.height(10.dp))

                StardewButton(
                    text = if (currentShiftStatus == DutyShiftType.OFF) "Mark On Duty" else "Set Off Duty",
                    onClick = {
                        val nextStatus = if (currentShiftStatus == DutyShiftType.OFF) {
                            when (selectedShiftIndex) { 0 -> DutyShiftType.DAY; 1 -> DutyShiftType.EVENING; else -> DutyShiftType.NIGHT }
                        } else DutyShiftType.OFF
                        
                        dutySchedule[selectedDate] = activeRecord.copy(
                            morning = if(selectedShiftIndex==0) nextStatus else activeRecord.morning,
                            evening = if(selectedShiftIndex==1) nextStatus else activeRecord.evening,
                            night = if(selectedShiftIndex==2) nextStatus else activeRecord.night
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
