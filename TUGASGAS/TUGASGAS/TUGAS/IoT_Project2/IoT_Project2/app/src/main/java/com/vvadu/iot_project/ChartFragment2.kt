package com.vvadu.iot_project

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.TimePicker
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import okhttp3.OkHttpClient
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.core.content.ContextCompat

class ChartFragment2 : Fragment() {
    private lateinit var progress: ProgressBar
    private lateinit var pieChart: PieChart
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
        val view = inflater.inflate(R.layout.fragment_chart2, container, false)

        // Initialize UI components
        progress = view.findViewById(R.id.progressBar)
        pieChart = view.findViewById(R.id.pieChart)
        startFilterButton = view.findViewById(R.id.startFilterButton)
        endFilterButton = view.findViewById(R.id.endFilterButton)
        btnFilter = view.findViewById(R.id.btnFilter)
        textStart = view.findViewById(R.id.timeStart)
        textEnd = view.findViewById(R.id.timeEnd)

        progress.visibility = View.VISIBLE
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        networkHelper = NetworkHelper(client)

        // Load data from API
        fetchData("https://2j5t9mtq-8090.asse.devtunnels.ms/alldata")

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
                            // Format selected date and time
                            calendar.set(year, month, dayOfMonth, hourOfDay, minute)

                            // Create a SimpleDateFormat for the desired format
                            val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
                            val formattedDate = sdf.format(calendar.time)

                            // Assign the formatted date to startDate
                            startDate = formattedDate
                            textStart.text = startDate // Update the UI with the formatted date
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
                            // Format selected date and time
                            calendar.set(year, month, dayOfMonth, hourOfDay, minute)

                            // Create a SimpleDateFormat for the desired format
                            val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
                            val formattedDate = sdf.format(calendar.time)

                            // Assign the formatted date to endDate
                            endDate = formattedDate
                            textEnd.text = endDate // Update the UI with the formatted date
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
            Log.d("Date Range", "Start: $startDate, End: $endDate")
            // Call the API with the date filter
            fetchData("https://2j5t9mtq-8090.asse.devtunnels.ms/alldata")
        }
    }

    private fun fetchData(url: String) {
        // Menambahkan startDate dan endDate sebagai parameter pada URL
        var urlWithParams = url
        if (startDate != null && endDate != null) {
            urlWithParams = "$urlWithParams?startDate=$startDate&endDate=$endDate"
        }

        networkHelper.getRequest(urlWithParams, object : NetworkHelper.NetworkCallback {
            override fun onSuccess(response: String) {
                activity?.runOnUiThread {
                    progress.visibility = View.GONE
                    processResponseForPieChart(response)
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

    private fun processResponseForPieChart(response: String) {
        try {
            val jsonArray = JSONArray(response)

            var trueCount = 0
            var falseCount = 0

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val timestamp = jsonObject.getString("createdAt")

                val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
                Log.d("Timestamp", "Original timestamp: $timestamp")
                val date = sdf.parse(timestamp)

                // Memeriksa apakah tanggal berada dalam rentang yang ditentukan
                if (isDateWithinRange(date)) {
                    val status = jsonObject.getBoolean("status")
                    if (status) {
                        trueCount++
                    } else {
                        falseCount++
                    }
                }
            }

            // Membuat chart
            val pieEntries = ArrayList<PieEntry>()
            if (trueCount > 0) pieEntries.add(PieEntry(trueCount.toFloat(), "Good"))
            if (falseCount > 0) pieEntries.add(PieEntry(falseCount.toFloat(), "Bad"))

            val pieDataSet = PieDataSet(pieEntries, "Status")
            pieDataSet.colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.green),  // Warna untuk Good
                ContextCompat.getColor(requireContext(), R.color.red)    // Warna untuk Bad
            )

            val pieData = PieData(pieDataSet)
            pieChart.data = pieData
            pieChart.invalidate()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isDateWithinRange(date: Date?): Boolean {
        val startDateParsed = startDate?.let { SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).parse(it) }
        val endDateParsed = endDate?.let { SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).parse(it) }

        // Memastikan bahwa jika tidak ada tanggal start atau end, akan menganggapnya valid
        return when {
            startDateParsed == null && endDateParsed == null -> true
            startDateParsed != null && endDateParsed == null -> !date!!.before(startDateParsed)
            startDateParsed == null && endDateParsed != null -> !date!!.after(endDateParsed)
            else -> !date!!.before(startDateParsed) && !date.after(endDateParsed)
        }
    }
}
