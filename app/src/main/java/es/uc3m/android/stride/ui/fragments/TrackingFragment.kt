// Add these dependencies in your app/build.gradle before using Firebase:
// implementation 'com.google.firebase:firebase-auth'
// implementation 'com.google.firebase:firebase-firestore'

package es.uc3m.android.stride.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaPlayer
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import es.uc3m.android.stride.R
import es.uc3m.android.stride.databinding.FragmentTrackingBinding
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class TrackingFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

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

    private val weatherScope = CoroutineScope(Dispatchers.Main + Job())
    private var lastWeatherUpdate = 0L
    private var currentTemperature = ""
    private var currentWeatherCondition = ""
    private var currentHumidity = ""
    private var currentWindSpeed = ""

    private var mediaPlayer: MediaPlayer? = null
    private val musicUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val UPDATE_INTERVAL = 5000L
        private const val FASTEST_UPDATE_INTERVAL = 2000L
        private const val WEATHER_UPDATE_INTERVAL = 10 * 60 * 1000L
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        createLocationRequest()
        createLocationCallback()

        timerRunnable = object : Runnable {
            override fun run() {
                timeElapsed = SystemClock.elapsedRealtime() - timeStarted
                updateTimerUI()
                handler.postDelayed(this, 1000)
            }
        }

        binding.btnStartTracking.setOnClickListener { toggleTracking() }
        binding.layoutWeatherInfo.setOnClickListener { showDetailedWeatherInfo() }

        binding.btnPlayMusic.setOnClickListener {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(musicUrl)
                    setOnPreparedListener {
                        start()
                        Toast.makeText(requireContext(), "\uD83C\uDFB5 Music started", Toast.LENGTH_SHORT).show()
                    }
                    prepareAsync()
                }
            } else if (!mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
                Toast.makeText(requireContext(), "▶️ Playing", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnPauseMusic.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                Toast.makeText(requireContext(), "⏸️ Paused", Toast.LENGTH_SHORT).show()
            }
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

                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastWeatherUpdate > WEATHER_UPDATE_INTERVAL) {
                            fetchWeatherData(location.latitude, location.longitude)
                            lastWeatherUpdate = currentTime
                        }
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    moveCamera(latLng)
                    fetchWeatherData(it.latitude, it.longitude)
                }
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission denied. Cannot track workout.", Toast.LENGTH_LONG).show()
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
        binding.btnStartTracking.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.stop_color))

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }

        timeStarted = SystemClock.elapsedRealtime() - timeElapsed
        handler.post(timerRunnable)
    }

    private fun stopTracking() {
        isTracking = false
        binding.btnStartTracking.text = getString(R.string.start_tracking)
        binding.btnStartTracking.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_color))

        fusedLocationClient.removeLocationUpdates(locationCallback)
        handler.removeCallbacks(timerRunnable)

        showSaveWorkoutDialog()
    }

    private fun showSaveWorkoutDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val input = EditText(requireContext())
        input.hint = "Enter activity title"
        builder.setTitle("Save Workout")
        builder.setView(input)

        builder.setPositiveButton("Save") { _, _ ->
            val title = input.text.toString()
            saveWorkoutToFirestore(title)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun saveWorkoutToFirestore(title: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        val route = pathPoints.map { point ->
            mapOf("lat" to point.latitude, "lng" to point.longitude)
        }

        val workout = hashMapOf(
            "email" to user.email,
            "fullname" to user.displayName,
            "title" to title,
            "timestamp" to FieldValue.serverTimestamp(),
            "distanceKm" to distanceTraveled / 1000f,
            "durationMs" to timeElapsed,
            "calories" to (distanceTraveled / 1000f * 65).roundToInt(),
            "path" to route,
            "weather" to mapOf(
                "temperature" to currentTemperature,
                "condition" to currentWeatherCondition,
                "humidity" to currentHumidity,
                "windSpeed" to currentWindSpeed
            )
        )

        db.collection("workouts")
            .add(workout)
            .addOnSuccessListener {
                binding.btnStartTracking.text = getString(R.string.start_tracking)
                binding.btnStartTracking.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.accent_color))
                Toast.makeText(requireContext(), "\u2705 Workout saved with map & weather!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "\u274C Failed to save workout.", Toast.LENGTH_LONG).show()
            }
    }

    private fun updatePathOnMap() {
        if (pathPoints.size > 1) {
            mMap.clear()
            val polylineOptions = PolylineOptions()
                .color(android.graphics.Color.BLUE)
                .width(10f)
                .addAll(pathPoints)
            mMap.addPolyline(polylineOptions)
        }
    }

    private fun updateDistanceUI(location: Location) {
        lastLocation?.let {
            val segmentDistance = it.distanceTo(location)
            distanceTraveled += segmentDistance

            val distanceInKm = distanceTraveled / 1000f
            binding.tvDistance.text = String.format("%.2f km", distanceInKm)

            if (timeElapsed > 0 && distanceInKm > 0) {
                val paceInMinutesPerKm = (timeElapsed / 60000f) / distanceInKm
                val paceMinutes = paceInMinutesPerKm.toInt()
                val paceSeconds = ((paceInMinutesPerKm - paceMinutes) * 60).roundToInt()
                binding.tvCurrentPace.text = String.format("%d:%02d /km", paceMinutes, paceSeconds)
            }

            val caloriesBurned = (distanceTraveled / 1000f * 65).roundToInt()
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

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        weatherScope.launch {
            try {
                val weatherData = withContext(Dispatchers.IO) {
                    fetchWeatherFromApi(latitude, longitude)
                }
                updateWeatherUI(weatherData)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvTemperature.text = "--°C"
                    binding.tvWeatherCondition.text = "Weather unavailable"
                    Toast.makeText(requireContext(), "Failed to fetch weather data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun fetchWeatherFromApi(latitude: Double, longitude: Double): JSONObject {
        val apiKey = requireContext().packageManager
            .getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
            .metaData.getString("weather_api_key") ?: ""

        val url = URL("https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$apiKey")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }

        reader.close()
        connection.disconnect()

        return JSONObject(response.toString())
    }

    private fun updateWeatherUI(weatherData: JSONObject) {
        try {
            val main = weatherData.getJSONObject("main")
            val weather = weatherData.getJSONArray("weather").getJSONObject(0)
            val wind = weatherData.getJSONObject("wind")

            currentTemperature = "${main.getDouble("temp").roundToInt()}°C"
            currentWeatherCondition = weather.getString("main")
            currentHumidity = "${main.getInt("humidity")}%"
            currentWindSpeed = "${wind.getDouble("speed").roundToInt()} m/s"

            binding.tvTemperature.text = currentTemperature
            binding.tvWeatherCondition.text = currentWeatherCondition

            when (currentWeatherCondition.lowercase(Locale.ROOT)) {
                "clear" -> binding.ivWeatherIcon.setImageResource(R.drawable.weather_sunny)
                "clouds" -> binding.ivWeatherIcon.setImageResource(R.drawable.weather_cloudy)
                "rain" -> binding.ivWeatherIcon.setImageResource(R.drawable.weather_rainy)
                "snow" -> binding.ivWeatherIcon.setImageResource(R.drawable.weather_snowy)
                "thunderstorm" -> binding.ivWeatherIcon.setImageResource(R.drawable.weather_thunder)
                else -> binding.ivWeatherIcon.setImageResource(R.drawable.weather_cloudy)
            }
        } catch (e: Exception) {
            binding.tvTemperature.text = "--°C"
            binding.tvWeatherCondition.text = "Weather error"
        }
    }

    private fun showDetailedWeatherInfo() {
        if (currentTemperature.isNotEmpty() && currentWeatherCondition.isNotEmpty()) {
            Toast.makeText(requireContext(), "Current weather: $currentWeatherCondition, Temperature: $currentTemperature, Humidity: $currentHumidity, Wind: $currentWindSpeed", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Weather data not available yet", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isTracking) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        handler.removeCallbacks(timerRunnable)
        weatherScope.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
        _binding = null
    }
}
