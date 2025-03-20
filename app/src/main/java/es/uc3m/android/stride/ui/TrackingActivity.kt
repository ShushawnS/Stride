package es.uc3m.android.stride.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import es.uc3m.android.stride.R
import es.uc3m.android.stride.databinding.ActivityTrackingBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityTrackingBinding
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private val pathPoints = mutableListOf<LatLng>()
    private var isTracking = false
    private var timeStarted = 0L
    private var timeElapsed = 0L
    private var distanceTraveled = 0f
    private var lastLocation: Location? = null

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val UPDATE_INTERVAL = 5000L // 5 seconds
        private const val FASTEST_UPDATE_INTERVAL = 2000L // 2 seconds
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set up the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        createLocationCallback()

        // Set up timer runnable
        timerRunnable = object : Runnable {
            override fun run() {
                timeElapsed = SystemClock.elapsedRealtime() - timeStarted
                updateTimerUI()
                handler.postDelayed(this, 1000)
            }
        }

        // Set up click listeners
        binding.btnStartTracking.setOnClickListener {
            toggleTracking()
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (isTracking) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        pathPoints.add(latLng)
                        updatePathOnMap()
                        updateDistanceUI(location)
                        moveCamera(latLng)
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    moveCamera(latLng)
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied. Cannot track workout.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun toggleTracking() {
        if (!isTracking) {
            startTracking()
        } else {
            stopTracking()
        }
    }

    private fun startTracking() {
        isTracking = true
        binding.btnStartTracking.text = getString(R.string.stop_tracking)
        binding.btnStartTracking.setBackgroundColor(ContextCompat.getColor(this, R.color.stop_color))

        // Start location updates
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

        // Start timer
        timeStarted = SystemClock.elapsedRealtime() - timeElapsed
        handler.post(timerRunnable)
    }

    private fun stopTracking() {
        isTracking = false
        binding.btnStartTracking.text = getString(R.string.start_tracking)
        binding.btnStartTracking.setBackgroundColor(ContextCompat.getColor(this, R.color.accent_color))

        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // Stop timer
        handler.removeCallbacks(timerRunnable)

        // TODO: Save workout data
        showWorkoutSummary()
    }

    private fun updatePathOnMap() {
        if (pathPoints.size > 1) {
            mMap.clear()
            val polylineOptions = PolylineOptions()
                .color(Color.BLUE)
                .width(10f)
                .addAll(pathPoints)
            mMap.addPolyline(polylineOptions)
        }
    }

    private fun updateDistanceUI(location: Location) {
        lastLocation?.let {
            val segmentDistance = it.distanceTo(location)
            distanceTraveled += segmentDistance

            // Update distance display
            val distanceInKm = distanceTraveled / 1000f
            binding.tvDistance.text = String.format("%.2f km", distanceInKm)

            // Update pace
            if (timeElapsed > 0) {
                val paceInMinutesPerKm = (timeElapsed / 60000f) / distanceInKm
                val paceMinutes = paceInMinutesPerKm.toInt()
                val paceSeconds = ((paceInMinutesPerKm - paceMinutes) * 60).roundToInt()
                binding.tvCurrentPace.text = String.format("%d:%02d /km", paceMinutes, paceSeconds)
            }

            // Update calories (very rough estimate)
            val caloriesBurned = (distanceTraveled / 1000f * 65).roundToInt() // ~65 calories per km
            binding.tvCalories.text = "$caloriesBurned kcal"
        }
        lastLocation = location
    }

    private fun moveCamera(latLng: LatLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
    }

    private fun updateTimerUI() {
        val formattedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(timeElapsed))
        binding.tvTimeElapsed.text = formattedTime
    }

    private fun showWorkoutSummary() {
        // TODO: Implement workout summary dialog or navigate to summary screen
        Toast.makeText(
            this,
            "Workout completed! Distance: ${String.format("%.2f", distanceTraveled / 1000f)} km",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPause() {
        super.onPause()
        if (isTracking) {
            // Stop location updates but keep the timer running
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isTracking) {
            // Resume location updates
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
        fusedLocationClient.removeLocationUpdates(locationCallback)
        handler.removeCallbacks(timerRunnable)
    }
}