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
import com.example.sampleapp.data.ShiftEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class DutyShiftType(val label: String, val color: Color) {
    OFF("Off Duty", StardewParchmentDark),
    DAY("Morning Shift", StardewGreen),
    EVENING("Evening Shift", StardewGold),
    NIGHT("Midnight Shift", StardewTerracotta),
    STANDBY("Standby", StardewStandby)
}

@Composable
fun ShiftAlmanacScreen(
    shifts: List<ShiftEntity>,
    coinBalance: Int, 
    onMenuClick: () -> Unit,
    onUpdateShift: (String, Int, String) -> Unit // date, index, status
) {
    val startDate = remember { LocalDate.now().withDayOfMonth(1) }
    val daysOfWeekLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedShiftIndex by remember { mutableIntStateOf(0) }

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
                            val dateString = cellDate.toString() // "yyyy-MM-dd"
                            
                            // Find from database, or default
                            val record = shifts.find { it.date == dateString } ?: ShiftEntity(date = dateString)
                            val isSelectedDate = cellDate == selectedDate
                            
                            Box(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    listOf(record.morning, record.evening, record.night).forEachIndexed { index, shiftStatus ->
                                        val shiftEnum = try { DutyShiftType.valueOf(shiftStatus) } catch(e: Exception) { DutyShiftType.OFF }
                                        val isSelected = isSelectedDate && selectedShiftIndex == index
                                        
                                        Box(modifier = Modifier
                                            .size(16.dp)
                                            .background(shiftEnum.color)
                                            .border(if (isSelected) 2.5.dp else 1.5.dp, if (isSelected) StardewTextPrimary else StardewWoodDark)
                                            .clickable { selectedDate = cellDate; selectedShiftIndex = index }
                                        )
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

            val activeDateStr = selectedDate.toString()
            val activeRecord = shifts.find { it.date == activeDateStr } ?: ShiftEntity(date = activeDateStr)
            val currentStatusString = when (selectedShiftIndex) {
                0 -> activeRecord.morning
                1 -> activeRecord.evening
                else -> activeRecord.night
            }
            val currentStatusEnum = try { DutyShiftType.valueOf(currentStatusString) } catch(e: Exception) { DutyShiftType.OFF }
            val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

            StardewPanel(modifier = Modifier.fillMaxWidth(), backgroundColor = StardewParchmentDark) {
                Text("$formattedDate • ${shiftTimeWindows[selectedShiftIndex]}", style = StardewTypography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Status: ${currentStatusEnum.label}", style = StardewTypography.bodyMedium)
                Spacer(modifier = Modifier.height(10.dp))

                StardewButton(
                    text = if (currentStatusEnum == DutyShiftType.OFF) "Mark On Duty" else "Set Off Duty",
                    onClick = {
                        val nextStatus = if (currentStatusEnum == DutyShiftType.OFF) {
                            when (selectedShiftIndex) { 0 -> DutyShiftType.DAY.name; 1 -> DutyShiftType.EVENING.name; else -> DutyShiftType.NIGHT.name }
                        } else DutyShiftType.OFF.name
                        
                        onUpdateShift(activeDateStr, selectedShiftIndex, nextStatus)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
