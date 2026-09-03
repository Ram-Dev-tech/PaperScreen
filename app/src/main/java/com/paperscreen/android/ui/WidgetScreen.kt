package com.paperscreen.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

@Composable
fun WidgetScreen(viewModel: TimerViewModel = viewModel()) {
    val timerMode by viewModel.timerMode.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Focus",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mode Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TimerModeButton(
                    text = "Pomodoro",
                    isSelected = timerMode == TimerMode.POMODORO,
                    onClick = { viewModel.switchMode(TimerMode.POMODORO) }
                )
                Spacer(modifier = Modifier.width(16.dp))
                TimerModeButton(
                    text = "Standard",
                    isSelected = timerMode == TimerMode.STANDARD,
                    onClick = { viewModel.switchMode(TimerMode.STANDARD) }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Timer Display
            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 80.sp,
                    fontWeight = FontWeight.Light
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (isRunning) {
                    Button(
                        onClick = { viewModel.pauseTimer() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        modifier = Modifier.size(width = 120.dp, height = 48.dp)
                    ) {
                        Text("Pause")
                    }
                } else {
                    Button(
                        onClick = { viewModel.startTimer() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        ),
                        modifier = Modifier.size(width = 120.dp, height = 48.dp)
                    ) {
                        Text("Start")
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { viewModel.resetTimer() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier
                        .size(width = 120.dp, height = 48.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                ) {
                    Text("Reset")
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Task reminders & widgets will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TimerModeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background
    val contentColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .border(1.dp, if (!isSelected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onBackground, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
        )
    }
}
