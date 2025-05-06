package es.uc3m.android.stride.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.uc3m.android.stride.R

class MyActivitiesFragment : Fragment() {

    private lateinit var listView: ListView
    private val activitiesList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_activities, container, false)
        listView = view.findViewById(R.id.activitiesListView)
        fetchActivities()
        return view
    }

    private fun fetchActivities() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("workouts")
            .whereEqualTo("email", user.email)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                activitiesList.clear()
                for (document in result) {
                    val title = document.getString("title") ?: "Untitled"
                    val distance = document.getDouble("distanceKm") ?: 0.0
                    val calories = document.getLong("calories") ?: 0
                    val summary = "$title - ${"%.2f".format(distance)} km - $calories kcal"
                    activitiesList.add(summary)
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, activitiesList)
                listView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch workouts", Toast.LENGTH_SHORT).show()
            }
    }
}
