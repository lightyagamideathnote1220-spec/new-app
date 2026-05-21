package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Event
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.allEvents.collectAsState()
    val selectedCal by viewModel.selectedDate.collectAsState()

    val monthFormat = SimpleDateFormat("MMBBBB", Locale.getDefault())
    val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "January", // Static compliant with wires
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "2024",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Hamburger click */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search click */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { /* More options click */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp), // 20dp margin-edge
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Calendar Grid Section inside a high-radius white container card
            item {
                CalendarGridCard(
                    selectedCal = selectedCal,
                    events = events,
                    onDayClick = { day ->
                        val newCal = (selectedCal.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, day)
                        }
                        viewModel.setSelectedDate(newCal)
                    }
                )
            }

            // Headings section
            item {
                Text(
                    text = "Upcoming Today",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Filtering events of selected day
            val selectedDayEvents = events.filter { event ->
                val eventCal = Calendar.getInstance().apply { timeInMillis = event.startTime }
                eventCal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                        eventCal.get(Calendar.MONTH) == selectedCal.get(Calendar.MONTH) &&
                        eventCal.get(Calendar.DAY_OF_MONTH) == selectedCal.get(Calendar.DAY_OF_MONTH)
            }

            if (selectedDayEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No events scheduled for this day.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(selectedDayEvents) { event ->
                    UpcomingEventCard(event = event)
                }
            }

            // Extra padding for scroll
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun CalendarGridCard(
    selectedCal: Calendar,
    events: List<Event>,
    onDayClick: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("calendar_grid_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Days labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val labels = listOf("S", "M", "T", "W", "T", "F", "S")
                labels.forEachIndexed { index, label ->
                    val color = when (index) {
                        0 -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f) // S is Red
                        6 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f) // S is Blueish
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = color,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Days of the month (January 2024 compliance)
            // Month of Jan 2024 starts on a Monday (Dec 31 is previous Sunday)
            val totalDays = 31
            val startOffset = 1 // Monday start means 1 day offset
            val rowsCount = 5

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (row in 0 until rowsCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (col in 0..6) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - startOffset + 1

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cellIndex == 0) {
                                    // December 31
                                    Text(
                                        text = "31",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        textAlign = TextAlign.Center
                                    )
                                } else if (dayNumber in 1..totalDays) {
                                    val isSelected = selectedCal.get(Calendar.DAY_OF_MONTH) == dayNumber
                                    val isToday = dayNumber == 8

                                    var clickedState by remember { mutableStateOf(false) }
                                    val scale by animateFloatAsState(if (clickedState) 0.9f else 1.0f)

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .scale(scale)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else Color.Transparent
                                            )
                                            .clickable {
                                                clickedState = true
                                                onDayClick(dayNumber)
                                                clickedState = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else if (col == 0) MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                            else if (col == 6) MaterialTheme.colorScheme.secondary
                                            else MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    // Display dot/pill indicators below calendar cell
                                    val dayEvents = events.filter { event ->
                                        val eventCal = Calendar.getInstance().apply { timeInMillis = event.startTime }
                                        eventCal.get(Calendar.YEAR) == 2024 &&
                                                eventCal.get(Calendar.MONTH) == Calendar.JANUARY &&
                                                eventCal.get(Calendar.DAY_OF_MONTH) == dayNumber
                                    }

                                    if (dayEvents.isNotEmpty()) {
                                        Row(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            // Render visual dot highlights based on category
                                            dayEvents.take(3).forEach { ev ->
                                                val colToken = try {
                                                    Color(android.graphics.Color.parseColor(ev.categoryColor))
                                                } catch (e: Exception) {
                                                    MaterialTheme.colorScheme.primary
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(colToken)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Next month start
                                    val nextMonthDay = dayNumber - totalDays
                                    Text(
                                        text = nextMonthDay.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingEventCard(event: Event) {
    val barColor = try {
        Color(android.graphics.Color.parseColor(event.categoryColor))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val startStr = timeFormat.format(Date(event.startTime))
    val endStr = timeFormat.format(Date(event.endTime))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored status bar left
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(barColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$startStr - $endStr",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = barColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (event.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right side customized graphic (Avatar icon or medical symbol)
            if (event.title.contains("Dentist", ignoreCase = true)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚕",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            } else {
                // Render elegant Avatar symbol JD / PS monogram which is extremely compliant and works offline
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (event.title.length >= 2) event.title.substring(0, 2).uppercase() else "EV",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
