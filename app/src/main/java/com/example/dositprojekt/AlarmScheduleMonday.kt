package com.example.dositprojekt

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.voice.VoiceInteractionSession.VisibleActivityCallback
import android.widget.Button
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import com.example.dositprojekt.databinding.ActivityAlarmScheduleMondayBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar
import android.view.View.INVISIBLE
import android.view.View.VISIBLE

class AlarmScheduleMonday : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmScheduleMondayBinding
    private lateinit var picker: MaterialTimePicker
    private lateinit var calendar: Calendar
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent // possibly remove

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // View Binding
        binding = ActivityAlarmScheduleMondayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Create notification channel
        createNotificationChannel()

        // Get the selected day from the intent
        val selectedDay = intent.getStringExtra("selected_day") ?: "Friday"

        val open = findViewById<Button>(R.id.open)
        val close = findViewById<Button>(R.id.close)
        open.setOnClickListener {
            open.visibility = INVISIBLE
            close.visibility = VISIBLE

        }
        close.setOnClickListener {
            close.visibility = INVISIBLE
            open.visibility = VISIBLE
        }

        // Retrieve stored alarm information from SharedPreferences
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val hour = sharedPref.getInt("hour", 12) // Default to 12 if not found
        val minute = sharedPref.getInt("minute", 0) // Default to 0 if not found
        val amPm = sharedPref.getString("am_pm", "PM") // Default to AM if not found

        // Update your UI with the retrieved alarm information
        binding.SelectedTime.text = String.format("%02d", hour) + ":" + String.format("%02d", minute) + " $amPm"

        calendar = Calendar.getInstance()
        // Click listener for selecting time
        binding.SelectedTime.setOnClickListener {
            showtimePicker()
        }
        // Click listener for saving alarm
        binding.Save.setOnClickListener {
            setAlarm(selectedDay)
        }
        // Click listener for canceling alarm
        binding.Cancel.setOnClickListener {
            cancelAlarm()
        }
    }

    private fun cancelAlarm() {
        // Canceling the alarm using the AlarmManager
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val context = this
        val requestCode = 123
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.cancel(pendingIntent)
        Toast.makeText(this, "Alarm cancelled", Toast.LENGTH_LONG).show()
    }

    private fun setAlarm(selectedDay: String) {
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)

        // Calculate initial alarm time based on the selected day
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        val selectedDayOfWeek = getDayOfWeek(selectedDay)
        val requestCode = 456

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Convert picker time to 12-hour format with AM/PM
        val amPm = if (picker.hour >= 12) "PM" else "AM"
        val formattedHour = if (picker.hour > 12) picker.hour - 12 else picker.hour

        editor.putInt("hour", formattedHour)
        editor.putInt("minute", picker.minute)
        editor.putString("am_pm", amPm) // Store AM/PM information
        editor.apply()

        // If the selected day is before today, set the alarm for next week
        if (selectedDayOfWeek < today) {
            calendar.add(Calendar.DAY_OF_MONTH, 7)
        }

        calendar.set(Calendar.DAY_OF_WEEK, selectedDayOfWeek)
        calendar[Calendar.HOUR_OF_DAY] = picker.hour
        calendar[Calendar.MINUTE] = picker.minute
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0

        val pendingIntent = PendingIntent.getBroadcast(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the repeating alarm for a week
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,  // Repeat every week
            pendingIntent
        )

        Toast.makeText(this, "Alarm set successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showtimePicker() {
        picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Time")
            .build()

        picker.show(supportFragmentManager, "Take medication")
        picker.addOnPositiveButtonClickListener {

            // Convert picker time to 12-hour format with AM/PM
            val amPm = if (picker.hour >= 12) "PM" else "AM"
            val formattedHour = if (picker.hour > 12) picker.hour - 12 else picker.hour

            if (picker.hour > 12) {
                binding.SelectedTime.text =
                    String.format("%02d", formattedHour) + ":" + String.format(
                        "%02d",
                        picker.minute
                    ) + " PM"
            } else {
                binding.SelectedTime.text =
                    String.format("%02d", formattedHour) + ":" + String.format(
                        "%02d",
                        picker.minute
                    ) + " AM"
            }
            calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = picker.hour
            calendar[Calendar.MINUTE] = picker.minute
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0
        }
    }
     
    private fun getDayOfWeek(selectedDay: String): Int {
        // Converts the selected day (string) to the corresponding Calendar constant for the day of the week
        return when (selectedDay) {
            "Monday" -> Calendar.MONDAY
            "Tuesday" -> Calendar.TUESDAY
            "Wednesday" -> Calendar.WEDNESDAY
            "Thursday" -> Calendar.THURSDAY
            "Friday" -> Calendar.FRIDAY
            "Saturday" -> Calendar.SATURDAY
            "Sunday" -> Calendar.SUNDAY
            else -> throw IllegalArgumentException("Invalid day: $selectedDay")
        }
    }

    private fun createNotificationChannel() {
        // Creating a notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Take medication"
            val description = "Channel for Take your medication"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("Take medication", name, importance)
            channel.description = description
            val notificationManager = ContextCompat.getSystemService(
                this, NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
