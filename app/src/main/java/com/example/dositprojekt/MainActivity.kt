package com.example.dositprojekt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions(
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)//Jitse lagt till
        val SelectADay = findViewById<TextView>(R.id.SelectADay)
        val MondayButton = findViewById<Button>(R.id.monday)
        val TuesdayButton = findViewById<Button>(R.id.tuesday)
        val WednesdayButton = findViewById<Button>(R.id.wednesday)
        val ThursdayButton = findViewById<Button>(R.id.thursday)
        val FridayButton = findViewById<Button>(R.id.friday)
        val SaturdayButton = findViewById<Button>(R.id.saturday)
        val SundayButton = findViewById<Button>(R.id.sunday)

        MondayButton.setOnClickListener {
            startAlarmScheduleActivity(AlarmScheduleMonday::class.java, "Monday")
        }
        TuesdayButton.setOnClickListener {
            startAlarmScheduleActivity(AlarmScheduleTuesday::class.java, "Friday")
        }
    }
    private fun startAlarmScheduleActivity(activityClass: Class<*>, selectedDay: String) {
        val intent = Intent(this, activityClass)
        intent.putExtra("selected_day", selectedDay)
        startActivity(intent)
    }
}