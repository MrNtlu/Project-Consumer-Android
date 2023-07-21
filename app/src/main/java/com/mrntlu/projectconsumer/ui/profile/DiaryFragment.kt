package com.mrntlu.projectconsumer.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.mrntlu.projectconsumer.adapters.CalendarAdapter
import com.mrntlu.projectconsumer.adapters.DiaryAdapter
import com.mrntlu.projectconsumer.databinding.FragmentDiaryBinding
import com.mrntlu.projectconsumer.models.common.CalendarUI
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.getFirstDateOfTheWeek
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.viewmodels.main.profile.DiaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class DiaryFragment : BaseFragment<FragmentDiaryBinding>() {

    private val viewModel: DiaryViewModel by viewModels()

    private var focusedDate: LocalDate = LocalDate.now()
    private var calendarAdapter: CalendarAdapter? = null
    private var diaryAdapter: DiaryAdapter? = null

    private lateinit var dialog: LoadingDialog

    //TODO Replace LocalDate with CalendarUI in CalendarAdapter.kt
    // Create adapter for diary with title headers
    // Use diffutil to update count nodes

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            dialog = LoadingDialog(it)
        }

        setListeners()
        setObservers()
        setCalendar()
    }

    private fun setListeners() {
        binding.apply {
            calendarNextButton.setOnClickListener {
                updateFocusedDate(focusedDate.plusWeeks(1))
            }

            calendarPrevButton.setOnClickListener {
                updateFocusedDate(focusedDate.minusWeeks(1))
            }
        }
    }

    private fun fetchRequest(forceFetch: Boolean = true) {
        if (!(viewModel.logsResponse.hasObservers() || viewModel.logsResponse.value is NetworkResponse.Success) || forceFetch)
            viewModel.getUserLogs(daysInWeekArray().first().toString(), daysInWeekArray().last().toString())
    }

    private fun setObservers() {
        fetchRequest(forceFetch = false)

        viewModel.logsResponse.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkResponse.Failure -> {
                    if (::dialog.isInitialized)
                        dialog.dismissDialog()

                    //TODO Fail screen
                    printLog("Error ${response.errorMessage}")
                }
                NetworkResponse.Loading -> {
                    if (::dialog.isInitialized)
                        dialog.showLoadingDialog()
                }
                is NetworkResponse.Success -> {
                    if (::dialog.isInitialized)
                        dialog.dismissDialog()

                    val logs = response.data.data
                    val dates = daysInWeekArray()

                    val calendarList = dates.map {
                        val index = logs?.indexOfFirst { logsByDate ->
                            it == LocalDate.parse(logsByDate.date)
                        } ?: -1

                        CalendarUI(
                            it,
                            if (index != -1)
                                logs!![index].count
                            else 0
                        )
                    }.toCollection(ArrayList())

                    updateFocusedDate()
                    calendarAdapter?.updateDays(calendarList)
                }
            }
        }
    }

    private fun setCalendar() {
        binding.calendarRV.apply {
            layoutManager = GridLayoutManager(this.context, 7)

            calendarAdapter = CalendarAdapter()
            adapter = calendarAdapter
        }
    }

    private fun daysInWeekArray(): ArrayList<LocalDate> {
        val dayList = arrayListOf<LocalDate>()

        var current = focusedDate.getFirstDateOfTheWeek()
        val endDate = current?.plusWeeks(1)

        while (current?.isBefore(endDate) == true) {
            dayList.add(current)
            current = current.plusDays(1)
        }

        return dayList
    }

    private fun updateFocusedDate(newFocusedDate: LocalDate? = null) {
        if (newFocusedDate != null) {
            focusedDate = newFocusedDate
            fetchRequest()
        }

        val headerDateText = "${focusedDate.month} ${focusedDate.year}"
        binding.calendarDateTV.text = headerDateText
    }

    override fun onDestroyView() {
        //TODO Remove observers
        calendarAdapter = null
        diaryAdapter = null

        super.onDestroyView()
    }
}