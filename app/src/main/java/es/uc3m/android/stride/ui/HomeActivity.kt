package es.uc3m.android.stride.ui

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import es.uc3m.android.stride.R
import es.uc3m.android.stride.ui.fragments.MyActivitiesFragment
import es.uc3m.android.stride.ui.fragments.OtherActivitiesFragment
import es.uc3m.android.stride.ui.fragments.TrackingFragment
import java.util.*
import es.uc3m.android.stride.notifications.NotificationReceiver


class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_other_activities -> loadFragment(OtherActivitiesFragment())
                R.id.nav_my_activities -> loadFragment(MyActivitiesFragment())
                R.id.nav_tracking -> loadFragment(TrackingFragment())
            }
            true
        }

        // Default selected tab
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_tracking
        }

        // 🔔 Schedule daily notifications
        scheduleDailyNotification()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun scheduleDailyNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)  // Use `this` for current Activity context
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Set the time: 9:00 AM daily
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9)  // Set the time to 9:00 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1) // move to next day if already past 9:00 AM
            }
        }

        // Set a repeating alarm that triggers daily at 9:00 AM
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}
