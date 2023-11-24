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
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.dositprojekt.databinding.ActivityAlarmScheduleTuesdayBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar


class AlarmScheduleTuesday : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmScheduleTuesdayBinding
    private lateinit var picker: MaterialTimePicker
    private lateinit var calendar: Calendar
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent //kanske ta bort


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmScheduleTuesdayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createNotificationChannel()

        // Get the selected day from the intent
        val selectedDay = intent.getStringExtra("selected_day") ?: "Friday"

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val hour = sharedPref.getInt("hour", 12) // Default to 12 if not found
        val minute = sharedPref.getInt("minute", 0) // Default to 0 if not found
        val amPm = sharedPref.getString("am_pm", "AM") // Default to AM if not found

        // Update your UI with the retrieved alarm information
        binding.SelectedTime2.text = String.format("%02d", hour) + ":" + String.format("%02d", minute) + " $amPm"

        calendar = Calendar.getInstance()

        binding.SelectedTime2.setOnClickListener {
            showtimePicker()
        }
        binding.Save2.setOnClickListener {
            setAlarm(selectedDay)
        }
        binding.Cancel2.setOnClickListener {
            cancelAlarm()
        }
    }


    private fun cancelAlarm() {

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val context = this  // If you're inside an Activity
        val requestCode = 456  // Use a unique integer value
        // Now, you can create the PendingIntent
        val pendingIntent =
            PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.cancel(pendingIntent)
        Toast.makeText(this, "Alarm cancelled", Toast.LENGTH_LONG).show()
    }

    private fun setAlarm(selectedDay: String) {
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(
            this,
            AlarmReceiver::class.java
        )

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
                binding.SelectedTime2.text =
                    String.format("%02d", formattedHour) + ":" + String.format(
                        "%02d",
                        picker.minute
                    ) + " PM"
            } else {
                binding.SelectedTime2.text =
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
