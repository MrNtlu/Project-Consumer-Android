package com.mrntlu.projectconsumer.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.adapters.CalendarAdapter
import com.mrntlu.projectconsumer.adapters.DiaryAdapter
import com.mrntlu.projectconsumer.databinding.FragmentDiaryBinding
import com.mrntlu.projectconsumer.models.common.CalendarUI
import com.mrntlu.projectconsumer.models.common.LogsUI
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.getFirstDateOfTheWeek
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.smoothScrollToCenteredPosition
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
        setRecyclerView()
    }

    private fun setListeners() {
        binding.apply {
            errorLayoutInc.refreshButton.setOnClickListener {
                fetchRequest(forceFetch = true)
            }

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
            viewModel.getUserLogs(daysInWeekArray().first().toString(), daysInWeekArray().last().plusDays(1).toString())
    }

    private fun setObservers() {
        fetchRequest(forceFetch = false)

        val layoutParams = binding.calendarLayout.layoutParams as ViewGroup.MarginLayoutParams

        viewModel.totalYScroll.observe(viewLifecycleOwner) { totalYScroll ->
            if (totalYScroll in 0..900) {
                layoutParams.topMargin = (-totalYScroll / 1.5).toInt()
                binding.calendarLayout.layoutParams = layoutParams
            } else {
                layoutParams.topMargin = -600
                binding.calendarLayout.layoutParams = layoutParams
            }
        }

        viewModel.logsResponse.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkResponse.Failure -> {
                    if (::dialog.isInitialized)
                        dialog.dismissDialog()

                    binding.errorLayoutInc.apply {
                        cancelButton.setGone()

                        errorText.text = response.errorMessage
                    }
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

                    val logList = logs?.flatMap {
                        it.data
                    }?.sortedWith(compareBy {
                        it.createdAt
                    })

                    val logUIList  = arrayListOf<LogsUI>()
                    logList?.forEachIndexed { index, log ->
                        val isHeader = index == 0 ||
                            log.createdAt.convertToHumanReadableDateString() !=
                            logList[index.minus(1)].createdAt.convertToHumanReadableDateString()
                        if (isHeader)
                            logUIList.add(
                                LogsUI(
                                    log,
                                    true
                                )
                            )

                        logUIList.add(
                            LogsUI(
                                log,
                                false
                            )
                        )
                    }

                    updateFocusedDate()
                    calendarAdapter?.updateDays(calendarList)
                    diaryAdapter?.setData(logUIList)
                }
            }
        }
    }

    private fun setCalendar() {
        binding.calendarRV.apply {
            layoutManager = GridLayoutManager(this.context, 7)

            calendarAdapter = CalendarAdapter() { date ->
                val position = diaryAdapter?.getScrollPosition(date.convertToHumanReadableDateString()) ?: 0

                if (position > -1)
                    binding.logsRV.smoothScrollToCenteredPosition(position)
            }
            adapter = calendarAdapter
        }
    }

    private fun setRecyclerView() {
        binding.logsRV.apply {
            layoutManager = LinearLayoutManager(this.context)

            diaryAdapter = DiaryAdapter()
            adapter = diaryAdapter

            var isScrolling = false
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isScrolling = newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    if (isScrolling)
                        viewModel.setTotalYPosition(dy)
                }
            })
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

        viewModel.setTotalYPosition(-150)
        binding.logsRV.scrollToPosition(0)

        val headerDateText = "${focusedDate.month.toString().lowercase().replaceFirstChar { it.uppercase() }} ${focusedDate.year}"
        binding.calendarDateTV.text = headerDateText
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.setTotalYPosition(0)

            viewModel.logsResponse.removeObservers(this)
            viewModel.totalYScroll.removeObservers(this)
        }

        calendarAdapter = null
        diaryAdapter = null

        super.onDestroyView()
    }
}