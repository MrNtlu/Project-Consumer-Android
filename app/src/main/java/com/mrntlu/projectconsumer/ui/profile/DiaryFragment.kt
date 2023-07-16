package com.mrntlu.projectconsumer.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.mrntlu.projectconsumer.databinding.FragmentDiaryBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.viewmodels.main.profile.DiaryViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint
class DiaryFragment : BaseFragment<FragmentDiaryBinding>() {

    private val viewModel: DiaryViewModel by viewModels()

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
    }

    private fun setUI() {
        binding.apply {
            viewModel.getUserLogs("2023-07-13", "2023-07-16")

            calendarView.isSelected = false
            printLog("${Date(calendarView.date)}")
        }
    }

    private fun setListeners() {
        binding.apply {

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

    private fun setDateRange() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}