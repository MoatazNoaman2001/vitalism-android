package com.example.livenativerppg.models.emrVitualize.ui

import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Build
import android.util.Log
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.livenativerppg.R
import com.example.livenativerppg.commons.rppgDateFormat
import com.example.livenativerppg.component.base.BaseFragment
import com.example.livenativerppg.component.natives.BPRPPGResult
import com.example.livenativerppg.component.natives.RPPGResult
import com.example.livenativerppg.databinding.FragmentEmrVisualizationBinding
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.shrikanthravi.collapsiblecalendarview.data.Day
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.IntStream
import java.util.stream.Stream
import javax.inject.Inject
import javax.inject.Named
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.streams.toList


private const val TAG = "EmrVisualization"

@AndroidEntryPoint
class EmrVisualizationFragment :
    BaseFragment<FragmentEmrVisualizationBinding>(R.layout.fragment_emr_visualization) {
    lateinit var controller: NavController
    lateinit var toolbar: MaterialToolbar


    @Inject
    @Named("emr")
    lateinit var emrHr: DatabaseReference
    @Inject
    @Named("BP_rppg")
    lateinit var emrBP: DatabaseReference

    override fun onInitialized() {
        super.onInitialized()
        controller = Navigation.findNavController(requireView())

        binding.MeasureChart.apply {
            setTouchEnabled(true)
            setPinchZoom(true)
            description.isEnabled = true
            val description = Description().apply {
                text = "Measures"
                textSize = 15f
            }
        }

        val ll1 = LimitLine(30f, "Title").apply {
            lineColor = resources.getColor(R.color.gray_400)
            lineWidth = 4f
            enableDashedLine(10f, 10f, 10f)
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
            textSize = 10f
        }
        val ll2 = LimitLine(35f, "").apply {
            lineWidth = 4f
            enableDashedLine(10f, 10f, 0f)
        }

        binding.MeasureChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                Log.d(TAG, "onValueSelected: value : ${e?.data.toString()}")
            }

            override fun onNothingSelected() {

            }
        })

        binding.calendarView.setCalendarListener(object : CollapsibleCalendar.CalendarListener {
            override fun onClickListener() {
            }

            override fun onDataUpdate() {
            }

            override fun onDayChanged() {
                Log.d(TAG, "onDayChanged: ${binding.calendarView.selectedDay}")
            }

            override fun onDaySelect() {
                val day = binding.calendarView.selectedDay
                val dateString = if (Build.VERSION.SDK_INT >= 26) {
                    val date = LocalDate.of(day?.year!!, day.month + 1, day.day)
                    date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy EEE"))
                } else {
                    val date = Calendar.getInstance().set(day?.year!!, day.month + 1, day.day)
                    rppgDateFormat.format(date)
                }
                Log.d(TAG, "onDaySelect: path: ${emrHr.child(dateString).path}")

                CoroutineScope(Dispatchers.IO).launch {
                    val barData = CallDaysSelcted(day)

                    MainScope().launch {
                        if (barData.dataSets.size >= 2) {
                            binding.BarChart.data = barData
                            val date = LocalDate.of(day.year, day.month + 1, day.day)
                            val daysName = Stream.iterate(date) { it.plusDays(1) }
                                .limit(7)
                                .map { it.format(DateTimeFormatter.ofPattern("EEE")) }
                                .toList()

                            binding.BarChart.xAxis.apply {
                                valueFormatter = IndexAxisValueFormatter(daysName)
                                setCenterAxisLabels(true)
                                position = XAxis.XAxisPosition.BOTTOM
                                granularity = 1f
                                isGranularityEnabled = true

                                val barSpace = 0.08f
                                val groupSpace = 0.44f
                                axisMinimum = 0f
                                axisMaximum = barData.getGroupWidth(groupSpace, barSpace) * 7
                            }

                            binding.BarChart.isDragEnabled = true
                            binding.BarChart.setVisibleXRangeMaximum(3f)
                            binding.BarChart.groupBars(0f, 0.08f, 0.44f)
                            binding.BarChart.axisLeft.axisMinimum = 0f
                            binding.BarChart.invalidate()
                        }

                    }

                    val children = emrHr.child(dateString).get().await().children
                    val allData =
                        children.map { Pair(getMeasurePair(it), getResultPairHR(it)) }.toList()

                    val measuresTime = allData.map { it.first }.toList()
                    if (measuresTime.isEmpty()) {
                        binding.MeasureChart.clear()
                        Log.d(TAG, "onDaySelect: no measures")
                        return@launch
                    } else {
                        Log.d(TAG, "onDaySelect: measures: ${measuresTime.size}")
                    }

                    binding.MeasureChart.xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        enableGridDashedLine(2f, 1f, 0f)
                        axisMaximum = measuresTime.size.toFloat()
                        axisMinimum = 0f
                        setLabelCount(measuresTime.size + 1, true)
//                        isGranularityEnabled = true
                        labelRotationAngle = 315f
                        setCenterAxisLabels(true)
                        valueFormatter =
                            ClaimXAxisValueError(measuresTime.map { it.first }.toList())

                        removeAllLimitLines()
                        addLimitLine(ll1)
                        addLimitLine(ll2)
                    }

                    binding.MeasureChart.axisLeft.apply {
                        valueFormatter = ClaimYAxisValueError()
                        axisMinimum = 0f
                        axisMaximum = 180f
                    }
                    setDataForMeasure(measuresTime)

                    val ResultTime = allData.map { it.second }
                        .toList().apply {
                            Log.d(TAG, "onDaySelect: $this")
                            if (this.isEmpty()) {
                                binding.ResultChart.clear()
                                Log.d(TAG, "onDaySelect: no Result")
                                return@launch
                            } else {
                                Log.d(TAG, "onDaySelect: Result: $size")

                                Log.d(
                                    TAG, "onDaySelect time sorted: ${
                                        asSequence().map { it.second }
                                            .flatten().map { it.time }.sorted()
                                            .map { it.toString() }.toList()
                                    }"
                                )
                            }
                        }
                    binding.ResultChart.xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        enableGridDashedLine(2f, 1f, 0f)
                        axisMaximum =
                            60f
                        axisMinimum =
                            0f

                        setLabelCount(60, true)
                        isGranularityEnabled = true
                        labelRotationAngle = 315f
                        setCenterAxisLabels(true)
                        valueFormatter =
                            ClaimXAxisValueErrorForResultChart(
                                IntStream.rangeClosed(0, 60).toList()

//                                ResultTime.asSequence().map { it.second }
//                                    .flatten().sortedBy { it.time }.map { it.time.toString() }
//                                    .toList()
                            )
                        removeAllLimitLines()
                        addLimitLine(ll1)
                        addLimitLine(ll2)
                    }

                    binding.ResultChart.axisLeft.apply {
                        valueFormatter = ClaimYAxisValueError()
                        axisMinimum = 0f
                        axisMaximum = 180f
                    }


                    setDataForResult(ResultTime)
                }
                Log.d(TAG, "onDaySelect: $dateString")
            }

            override fun onItemClick(v: View) {
            }

            override fun onMonthChange() {
            }

            override fun onWeekChange(position: Int) {
            }

        })
    }

    suspend fun CallDaysSelcted(day: Day): BarData {
        val date = LocalDate.of(day.year, day.month + 1, day.day)
        val resultHr = kotlin.collections.ArrayList<List<Pair<String, ArrayList<RPPGResult>>>>()
        val resultBP = kotlin.collections.ArrayList<List<Pair<String, ArrayList<BPRPPGResult>>>>()
        val weakValues = Stream.iterate(date) { it.plusDays(1) }
            .limit(7)
        val job1 = CoroutineScope(Dispatchers.IO).launch {
            for (weakValue in weakValues) {
                resultHr.add(emrHr.child(weakValue.format(DateTimeFormatter.ofPattern("dd-MM-yyyy EEE")))
                    .get().await().children.map {
                        getResultPairHR(it)
                    }
                )
                resultBP.add(emrBP.child(weakValue.format(DateTimeFormatter.ofPattern("dd-MM-yyyy EEE")))
                    .get().await().children.map {
                        getResultPairBP(it)
                    }
                )
            }
        }
        job1.join()
        Log.d(TAG, "CallDaysSelected: ${resultHr.map { it.map { it.second.map { it.mean } } }}")

        val daystoreadstomean =
            resultHr.map { it.map { it.second.map { it.mean } } }
        val daystoreadstotime =
            resultHr.map { it.map { it.second.map { it.time } } }


        val daysStored = BarDataSet(
            resultHr.map { it.map { it.second } }.flatten().mapIndexed { index, rppgResult ->
                return@mapIndexed BarEntry(
                    index.toFloat(),
                    rppgResult.map { it.mean }.average().toFloat()
                )
            }.toList() , "hr barData"
        ).apply {
            color = Color.rgb(
                Random().nextInt() % 255 + 1,
                Random().nextInt() % 255 + 1,
                Random().nextInt() % 255 + 1
            )
        }

        val daysStoredBP = BarDataSet(
            resultBP.map { it.map { it.second } }.flatten().mapIndexed { index, rppgResult ->
                return@mapIndexed BarEntry(
                    index.toFloat(),
                    rppgResult.map { it.sp }.average().toFloat()
                )
            }.toList() , "BP sp"
        ).apply {
            color = Color.rgb(
                Random().nextInt() % 255 + 1,
                Random().nextInt() % 255 + 1,
                Random().nextInt() % 255 + 1
            )
        }
        val daysStoredBP2 = BarDataSet(
            resultBP.map { it.map { it.second } }.flatten().mapIndexed { index, rppgResult ->
                return@mapIndexed BarEntry(
                    index.toFloat(),
                    rppgResult.map { it.dp }.average().toFloat()
                )
            }.toList() , "BP db"
        ).apply {
            color = Color.rgb(
                Random().nextInt() % 255 + 1,
                Random().nextInt() % 255 + 1,
                Random().nextInt() % 255 + 1
            )
        }


        return BarData(daysStored, daysStoredBP , daysStoredBP2)
    }


    private fun getMeasurePair(it: DataSnapshot?): Pair<String, Float> {
        val rppg_list: List<RPPGResult> =
            it?.children?.map { it.getValue(RPPGResult::class.java)!! }?.toList()!!
        Log.d(TAG, "getMeasurePair: it : ${it.value} , rppg_list: $rppg_list")
        val avg = ceil(rppg_list.sumOf { it.mean } / rppg_list.size)
        return Pair(it.key!!, avg.toFloat())
    }


    private fun getResultPairHR(it: DataSnapshot?): Pair<String, ArrayList<RPPGResult>> {
        val rppg_list: ArrayList<RPPGResult> =
            it?.children?.map { it.getValue(RPPGResult::class.java)!! }
                ?.toCollection(kotlin.collections.ArrayList())!!
        return Pair(it.key!!, rppg_list)
    }

    private fun getResultPairBP(it: DataSnapshot?): Pair<String, ArrayList<BPRPPGResult>> {
        val rppg_list: ArrayList<BPRPPGResult> =
            it?.children?.map { it.getValue(BPRPPGResult::class.java)!! }
                ?.toCollection(kotlin.collections.ArrayList())!!
        return Pair(it.key!!, rppg_list)
    }

    private fun setDataForMeasure(measuresTime: List<Pair<String, Float>>) {
        val data = ArrayList<Entry>()
        for (s in measuresTime) {
            data.add(Entry(measuresTime.indexOf(s).toFloat(), s.second))
        }

        val set1: LineDataSet;
        if (binding.MeasureChart.data != null &&
            binding.MeasureChart.data.dataSetCount > 0
        ) {
            set1 = binding.MeasureChart.data.getDataSetByIndex(0) as LineDataSet
            set1.values = data;
            binding.MeasureChart.data.notifyDataChanged();
            binding.MeasureChart.notifyDataSetChanged();
        } else {
            set1 = LineDataSet(data, "Total Measures");
            set1.setDrawCircles(true);
            set1.enableDashedLine(10f, 0f, 0f);
            set1.enableDashedHighlightLine(10f, 0f, 0f);
            set1.color = resources.getColor(R.color.blue_A200);
            set1.setCircleColor(resources.getColor(R.color.blue_A200));
            set1.lineWidth = 2f;//line size
            set1.circleRadius = 5f;
            set1.setDrawCircleHole(true);
            set1.valueTextSize = 10f;
            set1.setDrawFilled(true);
            set1.formLineWidth = 5f;
            set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f);
            set1.formSize = 5f;

            if (Build.VERSION.SDK_INT >= 18) {
//                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.blue_bg);
//                set1.setFillDrawable(drawable);
                set1.fillColor = Color.WHITE;

            } else {
                set1.fillColor = Color.WHITE;
            }
            set1.setDrawValues(true);
            val dataSets = ArrayList<ILineDataSet>();
            dataSets.add(set1);
            val data = LineData(dataSets);

            binding.MeasureChart.data = data
            binding.MeasureChart.invalidate()
            binding.MeasureChart.notifyDataSetChanged()
        }
    }

    private fun setDataForResult(resultTime: List<Pair<String, java.util.ArrayList<RPPGResult>>>) {
        val data = kotlin.collections.ArrayList<ArrayList<Entry>>()
        for (s in resultTime) {
            val simpleList = kotlin.collections.ArrayList<Entry>()
            for ((count, rppgResult) in s.second.withIndex()) {
                simpleList.add(
                    Entry(
                        count.toFloat(),
                        rppgResult.mean.toFloat()
                    )
                )
            }
            data.add(simpleList)
        }

        val setList = kotlin.collections.ArrayList<LineDataSet>();
        if (binding.ResultChart.data != null &&
            binding.ResultChart.data.dataSetCount > 0
        ) {
            for (i in 0..setList.size) {
                val set1: LineDataSet = binding.ResultChart.data.getDataSetByIndex(i) as LineDataSet
                set1.values = data[i];
                binding.ResultChart.data.notifyDataChanged();
                binding.ResultChart.notifyDataSetChanged();
            }
        } else {
            for (datum in data) {
                val set1 = LineDataSet(datum, "result measure").apply {
                    setDrawCircles(true)
                    enableDashedLine(10f, 0f, 0f)
                    enableDashedHighlightLine(10f, 0f, 0f)
                    val color = Color.rgb(
                        Random().nextInt() % 255 + 1,
                        Random().nextInt() % 255 + 1,
                        Random().nextInt() % 255 + 1
                    )
                    this.color = color
                    setCircleColor(color)
                    lineWidth = 2f;//line size
                    circleRadius = 5f;
                    setDrawCircleHole(true);
                    valueTextSize = 10f;
                    setDrawFilled(true);
                    formLineWidth = 5f;
                    formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f);
                    formSize = 5f;
                    fillColor = Color.WHITE;
                }
                setList.add(set1)
            }


            binding.ResultChart.data = LineData(kotlin.collections.ArrayList<ILineDataSet>().apply {
                for (lineDataSet in setList) {
                    add(lineDataSet)
                }
            })
            binding.ResultChart.invalidate()
            binding.ResultChart.notifyDataSetChanged()


        }


    }

    override fun setUpClicks() {
        binding.toolbar.setNavigationOnClickListener {
            controller.popBackStack()
        }
    }

    inner class ClaimXAxisValueError(val data: List<String>) : ValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {

            val position = value.roundToInt()
            if (position < data.size)
                return data[position]
            return "un known"
        }

    }

    inner class ClaimXAxisValueErrorForResultChart(val data: List<Int>) : ValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            if (value.roundToInt() <= data.size)
                return data[value.roundToInt()].toString()
            else return value.toString()
        }

    }

    inner class ClaimYAxisValueError() : ValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return value.toString() + "Bpm"
        }

    }
}


//
//        val myCalenderViewManger = object : CalendarViewManager {
//            override fun bindDataToCalendarView(
//                holder: SingleRowCalendarAdapter.CalendarViewHolder,
//                date: Date,
//                position: Int,
//                isSelected: Boolean,
//            ) {
//                if (isSelected) {
//                    holder.itemView.findViewById<AppCompatTextView>(R.id.textViewSelected).text =
//                        calenderDateFormat.format(date)
//                } else {
//                    holder.itemView.findViewById<AppCompatTextView>(R.id.textViewUnselected).text =
//                        calenderDateFormat.format(date)
//                }
//            }
//
//            override fun setCalendarViewResourceId(
//                position: Int,
//                date: Date,
//                isSelected: Boolean,
//            ): Int {
//                return if (isSelected) {
//                    R.layout.emr_visual_celander_selected
//                } else {
//                    R.layout.emr_visual_celander_not_selected
//                }
//            }
//        }
//
//        val mySelectionManager = object : CalendarSelectionManager {
//            override fun canBeItemSelected(position: Int, date: Date): Boolean {
//                return true
//            }
//        }
//
//        val myCalendarChangesObserver = object : CalendarChangesObserver {
//            override fun whenWeekMonthYearChanged(
//                weekNumber: String,
//                monthNumber: String,
//                monthName: String,
//                year: String,
//                date: Date,
//            ) {
//                super.whenWeekMonthYearChanged(weekNumber, monthNumber, monthName, year, date)
//            }
//
//            override fun whenSelectionChanged(isSelected: Boolean, position: Int, date: Date) {
//                super.whenSelectionChanged(isSelected, position, date)
//            }
//
//            override fun whenCalendarScrolled(dx: Int, dy: Int) {
//                super.whenCalendarScrolled(dx, dy)
//            }
//
//            override fun whenSelectionRestored() {
//                super.whenSelectionRestored()
//            }
//
//            override fun whenSelectionRefreshed() {
//                super.whenSelectionRefreshed()
//            }
//        }
//        val singleRowCalendar = binding.calendar.apply {
//            calendarViewManager = myCalenderViewManger
//            calendarChangesObserver = myCalendarChangesObserver
//            calendarSelectionManager = mySelectionManager
//            pastDaysCount = 30
//            includeCurrentDate = true
//            init()
//        }
