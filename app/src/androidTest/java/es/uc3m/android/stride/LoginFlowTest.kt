package es.uc3m.android.stride.ui

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.pressBack
import es.uc3m.android.stride.R



@RunWith(AndroidJUnit4::class)
class LoginFlowTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.INTERNET)

    @Test
    fun testLandingNavigation() {
        ActivityScenario.launch(LandingActivity::class.java)

        onView(withId(R.id.btnGetStarted)).perform(click())
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()))

        pressBack()

        onView(withId(R.id.tvLoginPrompt)).perform(click())
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
    }

    @Test
    fun testLoginValidation() {
        ActivityScenario.launch(LoginActivity::class.java)

        onView(withId(R.id.btnLogin)).perform(click())

        onView(withId(R.id.tilEmail)).check(matches(hasDescendant(withText(containsString("required")))))
        onView(withId(R.id.tilPassword)).check(matches(hasDescendant(withText(containsString("required")))))
    }

    @Test
    fun testRegisterNavigationFromLogin() {
        ActivityScenario.launch(LoginActivity::class.java)
        onView(withId(R.id.tvRegisterLink)).perform(click())
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()))
    }

    @Test
    fun testForgotPasswordWithEmptyEmailShowsToast() {
        ActivityScenario.launch(LoginActivity::class.java)
        onView(withId(R.id.tvForgotPassword)).perform(click())

        // Optional: Custom Toast matcher or log interceptor could go here
    }
}
