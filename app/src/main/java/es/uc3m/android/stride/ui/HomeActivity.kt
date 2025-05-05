package es.uc3m.android.stride.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import es.uc3m.android.stride.R
import es.uc3m.android.stride.ui.fragments.MyActivitiesFragment
import es.uc3m.android.stride.ui.fragments.OtherActivitiesFragment
import es.uc3m.android.stride.ui.fragments.TrackingFragment

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
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
