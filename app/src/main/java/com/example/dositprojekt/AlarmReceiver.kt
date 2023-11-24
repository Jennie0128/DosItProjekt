package com.example.dositprojekt
import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        //Log.d("AlarmDebug", "Alarm received")
        // Ensure that the context is not null
        if (context == null) {
            return
        }
        // Check for permissions and request if needed
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ){
            // Request the missing permissions if needed
        }
        // Create an intent to open the destination activity
        val i = Intent(context,MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT) //inte samma som videon

        // Create and configure the notification
        val builder = NotificationCompat.Builder(context!!,"Take medication")
            .setSmallIcon(R.drawable.dosit2023) //byt ut mot vår logga?
            .setContentTitle("DosIt")
            .setContentText("Take your medication")
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)


        // visar notifikationen
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(123,builder.build())
        //Log.d("AlarmApp", "Alarm received") // This logs when the alarm is received
    }
}
