import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.TimePicker

import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.vvadu.iot_project.NetworkHelper
import com.vvadu.iot_project.R
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ChartFragment : Fragment() {

    private lateinit var progress: ProgressBar
    private lateinit var lineChart: LineChart
    private lateinit var startFilterButton: Button
    private lateinit var endFilterButton: Button
    private lateinit var btnFilter: Button
    private lateinit var textStart: TextView
    private lateinit var textEnd: TextView
    private lateinit var networkHelper: NetworkHelper

    private var startDate: String? = null
    private var endDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chart, container, false)

        // Initialize UI components
        progress = view.findViewById(R.id.progressBar)
        lineChart = view.findViewById(R.id.lineChart)
        startFilterButton = view.findViewById(R.id.startFilterButton)
        endFilterButton = view.findViewById(R.id.endFilterButton)
        btnFilter = view.findViewById(R.id.btnFilter)
        textStart = view.findViewById(R.id.timeStart)
        textEnd = view.findViewById(R.id.timeEnd)

        progress.visibility = View.VISIBLE

        // Initialize NetworkHelper
        networkHelper = NetworkHelper(OkHttpClient())

        // Load data from API
        fetchData("https://d6464rhl-42352.asse.devtunnels.ms/alldata")

        // Set up date and time pickers
        setupDateTimePickers()

        return view
    }

    private fun setupDateTimePickers() {
        // Start Time Picker
        startFilterButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                    val timePickerDialog = TimePickerDialog(
                        requireContext(),
                        { _: TimePicker, hourOfDay: Int, minute: Int ->
                            // Format selected time and date
                            val selectedStart = "$year-${month + 1}-$dayOfMonth $hourOfDay:$minute"
                            startDate = selectedStart
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    )
                    timePickerDialog.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // End Time Picker
        endFilterButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                DatePickerDialog.OnDateSetListener { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                    val timePickerDialog = TimePickerDialog(
                        requireContext(),
                        { _: TimePicker, hourOfDay: Int, minute: Int ->
                            // Format selected time and date
                            val selectedEnd = "$year-${month + 1}-$dayOfMonth $hourOfDay:$minute"
                            endDate = selectedEnd
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    )
                    timePickerDialog.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Filter Button to load the filtered data
        btnFilter.setOnClickListener {
            textStart.text = startDate
            textEnd.text = endDate
            // Call the API with the date filter
            fetchData("https://d6464rhl-42352.asse.devtunnels.ms/alldata")
        }
    }

    private fun fetchData(url: String) {
        networkHelper.getRequest(url, object : NetworkHelper.NetworkCallback {
            override fun onSuccess(response: String) {
                activity?.runOnUiThread {
                    progress.visibility = View.GONE
                    processResponse(response)
                }
            }

            override fun onFailure(error: IOException) {
                activity?.runOnUiThread {
                    progress.visibility = View.GONE
                    Log.e("NetworkError", "Error fetching data: ${error.message}")
                }
            }
        })
    }

    private fun processResponse(response: String) {
        try {
            val jsonArray = JSONArray(response)
            val temperatureEntries = ArrayList<Entry>()
            val humidityEntries = ArrayList<Entry>()
            val gasLevelEntries = ArrayList<Entry>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val timestamp = jsonObject.getString("timestamp")
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault())
                val date = sdf.parse(timestamp)

                if (isDateWithinRange(date)) {
                    temperatureEntries.add(Entry(i.toFloat(), jsonObject.getString("temperature").toFloat()))
                    humidityEntries.add(Entry(i.toFloat(), jsonObject.getString("humidity").toFloat()))
                    gasLevelEntries.add(Entry(i.toFloat(), jsonObject.getString("gas_level").toFloat()))
                }
            }

            val tempDataSet = LineDataSet(temperatureEntries, "Temperature")
            val humidDataSet = LineDataSet(humidityEntries, "Humidity")
            val gasLevelDataSet = LineDataSet(gasLevelEntries, "Gas Level")

            tempDataSet.color = resources.getColor(R.color.red)
            humidDataSet.color = resources.getColor(R.color.blue)
            gasLevelDataSet.color = resources.getColor(R.color.green)

            val lineData = LineData(tempDataSet, humidDataSet, gasLevelDataSet)

            lineChart.data = lineData
            lineChart.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isDateWithinRange(date: Date?): Boolean {
        val startDateParsed = startDate?.let { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(it) }
        val endDateParsed = endDate?.let { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(it) }

        return when {
            startDateParsed == null && endDateParsed == null -> true
            startDateParsed != null && endDateParsed == null -> !date!!.before(startDateParsed)
            startDateParsed == null && endDateParsed != null -> !date!!.after(endDateParsed)
            else -> !date!!.before(startDateParsed) && !date.after(endDateParsed)
        }
    }
}
