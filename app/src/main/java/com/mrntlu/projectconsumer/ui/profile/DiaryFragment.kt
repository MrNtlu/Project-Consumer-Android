package com.mrntlu.projectconsumer.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.mrntlu.projectconsumer.adapters.CalendarAdapter
import com.mrntlu.projectconsumer.databinding.FragmentDiaryBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.utils.sundayForDate
import com.mrntlu.projectconsumer.viewmodels.main.profile.DiaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class DiaryFragment : BaseFragment<FragmentDiaryBinding>() {

    private val viewModel: DiaryViewModel by viewModels()

    private var focusedDate: LocalDate = LocalDate.now()
    private var calendarAdapter: CalendarAdapter? = null

    // Custom Calendar
    // https://tejas-soni.medium.com/horizontal-calendar-using-recylerview-android-f07f666f2da5
    // https://www.youtube.com/watch?v=knpSbtbPz3o&ab_channel=CodeWithCal
    // https://www.youtube.com/watch?v=yp0ZahAXbzo&ab_channel=AndroidCoding
    // https://medium.com/meetu-engineering/create-your-custom-calendar-view-10ff41f39bfe

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUI()
        setListeners()
        setObservers()
        setCalendar()
    }

    private fun setUI() {
        binding.apply {
//            viewModel.getUserLogs("2023-07-13", "2023-07-16")
            updateFocusedDate()
        }
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

    private fun setObservers() {
        viewModel.logsResponse.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkResponse.Failure -> {
                    printLog("Error ${response.errorMessage}")
                }
                NetworkResponse.Loading -> {

                }
                is NetworkResponse.Success -> {
                    printLog("${response.data}")
                }
            }
        }
    }

    private fun setCalendar() {
        binding.calendarRV.apply {
            layoutManager = GridLayoutManager(this.context, 7)

            calendarAdapter = CalendarAdapter(daysInWeekArray())
            adapter = calendarAdapter
        }
    }

    private fun daysInWeekArray(): ArrayList<LocalDate> {
        val dayList = arrayListOf<LocalDate>()

        var current = focusedDate.sundayForDate()
        val endDate = current?.plusWeeks(1)

        while (current?.isBefore(endDate) == true) {
            dayList.add(current)
            current = current.plusDays(1)
        }

        return dayList
    }

    private fun updateFocusedDate(newFocusedDate: LocalDate? = null) {
        if (newFocusedDate != null)
            focusedDate = newFocusedDate
        calendarAdapter?.updateDays(daysInWeekArray())

        val headerDateText = "${focusedDate.month} ${focusedDate.year}"
        binding.calendarDateTV.text = headerDateText
    }

    override fun onDestroyView() {
        //TODO Remove observers
        calendarAdapter = null

        super.onDestroyView()
    }
}