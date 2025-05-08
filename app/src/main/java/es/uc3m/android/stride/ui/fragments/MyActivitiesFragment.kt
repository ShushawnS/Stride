package es.uc3m.android.stride.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import es.uc3m.android.stride.R
import es.uc3m.android.stride.ui.adapters.ActivityAdapter
import es.uc3m.android.stride.ui.fragments.dialogs.WorkoutDetailDialogFragment
import es.uc3m.android.stride.ui.models.ActivityItem
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class MyActivitiesFragment : Fragment() {

    private lateinit var listView: ListView
    private val activityItems = mutableListOf<ActivityItem>()
    private val workoutDocuments = mutableListOf<Map<String, Any>>() // Keep for dialog details

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
                activityItems.clear()
                workoutDocuments.clear()

                for (document in result) {
                    val data = document.data
                    val title = data["title"] as? String ?: "Untitled"
                    val distance = (data["distanceKm"] as? Double)?.toString() ?: "0.00"
                    val calories = (data["calories"] as? Long)?.toString() ?: "0"
                    val timestamp = (data["timestamp"] as? Timestamp)?.toDate()?.let {
                        SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(it)
                    } ?: "Unknown date"
                    val weather = data["weather"] as? Map<*, *>
                    val condition = weather?.get("condition") as? String ?: "default"
                    val userName = data["fullname"] as? String ?: "Anonymous"
                    val formattedDate = (data["timestamp"] as? Timestamp)?.toDate()?.let {
                        SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(it)
                    } ?: "Unknown date"

                    val item = ActivityItem(
                        title = title,
                        distance = "%.2f".format(distance.toDouble()),
                        calories = calories,
                        date = formattedDate,
                        weatherCondition = condition,
                        userName = userName
                    )

                    activityItems.add(item)
                    workoutDocuments.add(data)
                }

                val adapter = ActivityAdapter(requireContext(), activityItems)
                listView.adapter = adapter

                listView.setOnItemClickListener { _, _, position, _ ->
                    val workoutData = workoutDocuments[position]
                    showWorkoutDetailDialog(workoutData)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to fetch workouts", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showWorkoutDetailDialog(workoutData: Map<String, Any>) {
        val dialog = WorkoutDetailDialogFragment.newInstance(workoutData)
        dialog.show(parentFragmentManager, "WorkoutDetailDialog")
    }
}
