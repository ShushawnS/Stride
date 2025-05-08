package es.uc3m.android.stride.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import es.uc3m.android.stride.R
import es.uc3m.android.stride.ui.models.ActivityItem

class ActivityAdapter(
    private val context: Context,
    private val activities: List<ActivityItem>
) : BaseAdapter() {

    override fun getCount(): Int = activities.size
    override fun getItem(position: Int): Any = activities[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_activity, parent, false)

        val title = view.findViewById<TextView>(R.id.tvTitle)
        val date = view.findViewById<TextView>(R.id.tvDate)
        val distance = view.findViewById<TextView>(R.id.tvDistance)
        val calories = view.findViewById<TextView>(R.id.tvCalories)
        val weather = view.findViewById<TextView>(R.id.tvWeather)
        val userName = view.findViewById<TextView>(R.id.tvUserName)



        val item = activities[position]

        title.text = item.title
        date.text = item.date
        distance.text = "${item.distance} km"
        calories.text = "${item.calories} kcal"
        userName.text = "👤 ${item.userName}"

        // Emoji weather representation
        val emoji = when (item.weatherCondition.lowercase()) {
            "sunny" -> "☀️"
            "rain" -> "🌧"
            "cloudy" -> "☁️"
            "snow" -> "❄️"
            else -> "🌤"
        }
        weather.text = emoji

        return view
    }
}
