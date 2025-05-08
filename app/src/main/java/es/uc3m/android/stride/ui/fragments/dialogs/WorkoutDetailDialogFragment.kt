package es.uc3m.android.stride.ui.fragments.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import es.uc3m.android.stride.R

class WorkoutDetailDialogFragment : DialogFragment(), OnMapReadyCallback {

    private lateinit var workoutData: Map<String, Any>
    private var path: List<Map<String, Double>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("UNCHECKED_CAST")
        workoutData = requireArguments().getSerializable("workoutData") as Map<String, Any>
        path = workoutData["path"] as? List<Map<String, Double>> ?: emptyList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_workout_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvTitle).text = workoutData["title"] as? String ?: "Untitled"
        view.findViewById<TextView>(R.id.tvDistance).text = "Distance: ${workoutData["distanceKm"]} km"
        view.findViewById<TextView>(R.id.tvCalories).text = "Calories: ${workoutData["calories"]} kcal"

        val weather = workoutData["weather"] as? Map<*, *>
        weather?.let {
            view.findViewById<TextView>(R.id.tvWeather).text =
                "Weather: ${it["condition"]}, ${it["temperature"]}, Humidity: ${it["humidity"]}, Wind: ${it["windSpeed"]}"
        }

        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.mapFragment, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (path.isNotEmpty()) {
            val polylineOptions = PolylineOptions()
                .color(requireContext().getColor(R.color.teal_200))
                .width(8f)

            val latLngList = path.map {
                LatLng(it["lat"] ?: 0.0, it["lng"] ?: 0.0)
            }

            polylineOptions.addAll(latLngList)
            googleMap.addPolyline(polylineOptions)

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngList.first(), 15f))
        }
    }

    companion object {
        fun newInstance(workoutData: Map<String, Any>): WorkoutDetailDialogFragment {
            val fragment = WorkoutDetailDialogFragment()
            val bundle = Bundle()
            bundle.putSerializable("workoutData", HashMap(workoutData))
            fragment.arguments = bundle
            return fragment
        }
    }
}
