package es.uc3m.android.stride.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import es.uc3m.android.stride.R
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class TrackingFlowTest {

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION)

    @Test
    fun testLoginAndTrackActivityAppearsInMyActivities() {
        // 1. Launch LoginActivity
        ActivityScenario.launch(LoginActivity::class.java)

        // 2. Enter login credentials
        onView(withId(R.id.etEmail)).perform(typeText("abcd@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).perform(typeText("pass1234"), closeSoftKeyboard())
        onView(withId(R.id.btnLogin)).perform(click())

        // 3. Wait for HomeActivity to load
        Thread.sleep(3000)

        // 4. Select the tracking tab first to ensure its fragment loads
        onView(withId(R.id.bottom_nav)).perform(click())
        onView(withText("Tracking")).perform(click()) // Or use onView(withId(R.id.nav_tracking))

        // 5. Tap "Start Tracking"
        onView(withId(R.id.btnStartTracking)).perform(click())

        // Simulate a longer tracking duration: wait for 10 seconds instead of 3
        Thread.sleep(10000)

        // 6. Tap "Stop Tracking"
        onView(withId(R.id.btnStartTracking)).perform(click())

        // 7. Save workout with title
        onView(withHint("Enter activity title")).perform(typeText("Espresso Test Run"), closeSoftKeyboard())
        onView(withText("Save")).perform(click())

        // Wait for save confirmation
        Thread.sleep(3000)

        // 8. Navigate to My Activities tab
        onView(withId(R.id.bottom_nav)).perform(click())
        onView(withText("My Activities")).perform(click())

        // 9. Assert saved activity is shown
        onView(withText(containsString("Espresso Test Run"))).check(matches(isDisplayed()))
    }
}
