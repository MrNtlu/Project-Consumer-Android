package com.mrntlu.projectconsumer.ui.discover

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.adapters.ViewBindingAdapter.setPadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.SuggestionsAdapter
import com.mrntlu.projectconsumer.databinding.FragmentAiSuggestionsBinding
import com.mrntlu.projectconsumer.interfaces.AISuggestionsInteraction
import com.mrntlu.projectconsumer.models.common.AISuggestion
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.ConsumeLaterBody
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.common.ConsumeLaterFragmentDirections
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.convertToFormattedDate
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.showConfirmationDialog
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.common.AISuggestionsConsumeLaterViewModel
import com.mrntlu.projectconsumer.viewmodels.main.common.AISuggestionsViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.UserSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class AISuggestionsFragment : BaseFragment<FragmentAiSuggestionsBinding>() {

    private val viewModel: AISuggestionsViewModel by viewModels()
    private val consumeLaterViewModel: AISuggestionsConsumeLaterViewModel by viewModels()
    private val userSharedViewModel: UserSharedViewModel by activityViewModels()

    private lateinit var dialog: LoadingDialog

    private var suggestionsAdapter: SuggestionsAdapter? = null
    private var confirmDialog: AlertDialog? = null
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiSuggestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            dialog = LoadingDialog(it)
        }


        setToolbar()
        setRecyclerView()
        setObservers()
    }

    private fun setToolbar() {
        binding.suggestionsToolbar.apply {
            setNavigationOnClickListener { navController.popBackStack() }
            binding.suggestionsToolbar.title = getString(R.string._suggestions)
        }
    }

    private fun setCountDownTimer(createdAt: String) {
        val startDate = createdAt.convertToFormattedDate()
        val currentDate = Calendar.getInstance().time
        val deadlineDayRange = if (userSharedViewModel.userInfo?.isPremium == true) 7 else 30

        if (startDate != null) {
            val calendar = Calendar.getInstance()
            calendar.time = startDate
            calendar.add(Calendar.DATE, deadlineDayRange)
            val deadlineDate = Date(calendar.timeInMillis)

            val difference = deadlineDate.time - currentDate.time

            countDownTimer = object: CountDownTimer(difference, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val days = millisUntilFinished / (24 * 60 * 60 * 1000)
                    val hours = (millisUntilFinished % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)
                    val minutes = ((millisUntilFinished % (24 * 60 * 60 * 1000)) % (60 * 60 * 1000)) / (60 * 1000)
                    val seconds = (((millisUntilFinished % (24 * 60 * 60 * 1000)) % (60 * 60 * 1000)) % (60 * 1000)) / 1000

                    val countDownText = "⌛ $days d $hours h $minutes m $seconds s"
                    binding.countDownTV.text = countDownText
                }

                override fun onFinish() {
                    binding.countDownTV.text = getString(R.string.ready_to_suggest)
                }
            }
            countDownTimer?.start()
        }
    }

    private fun setRecyclerView() {
        binding.suggestionsRV.apply {
            setPadding(0, 0, 0, context.dpToPx(8F))
            clipToPadding = false

            val linearLayout = LinearLayoutManager(context)
            layoutManager = linearLayout

            suggestionsAdapter = SuggestionsAdapter(!sharedViewModel.isLightTheme(), object: AISuggestionsInteraction {
                override fun onAddToListPressed(item: AISuggestion, position: Int) {
                    if (item.consumeLater != null) {
                        confirmDialog = context?.showConfirmationDialog(getString(R.string.do_you_want_to_delete)) {
                            val deleteConsumerLiveData = consumeLaterViewModel.deleteConsumeLater(IDBody(item.consumeLater!!.id))

                            deleteConsumerLiveData.observe(viewLifecycleOwner) { response ->
                                when(response) {
                                    is NetworkResponse.Failure -> {
                                        if (::dialog.isInitialized)
                                            dialog.dismissDialog()

                                        context?.showErrorDialog(response.errorMessage)
                                    }
                                    NetworkResponse.Loading -> {
                                        if (::dialog.isInitialized)
                                            dialog.showLoadingDialog()
                                    }
                                    is NetworkResponse.Success -> {
                                        if (::dialog.isInitialized)
                                            dialog.dismissDialog()

                                        suggestionsAdapter?.handleOperation(Operation(null, position, OperationEnum.Update))
                                    }
                                }
                            }
                        }
                    } else {
                        val createConsumerLiveData = consumeLaterViewModel.createConsumeLater(
                            ConsumeLaterBody(item.contentId, item.contentExternalID, item.contentExternalIntID, item.contentType, null)
                        )

                        createConsumerLiveData.observe(viewLifecycleOwner) { response ->
                            when(response) {
                                is NetworkResponse.Failure -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    context?.showErrorDialog(response.errorMessage)
                                }
                                NetworkResponse.Loading -> {
                                    if (::dialog.isInitialized)
                                        dialog.showLoadingDialog()
                                }
                                is NetworkResponse.Success -> {
                                    if (::dialog.isInitialized)
                                        dialog.dismissDialog()

                                    suggestionsAdapter?.handleOperation(Operation(response.data.data, position, OperationEnum.Update))
                                }
                            }
                        }
                    }
                }

                override fun onItemSelected(item: AISuggestion, position: Int) {
                    confirmDialog?.dismiss()

                    if (navController.currentDestination?.id == R.id.AISuggestionsFragment) {
                        when(Constants.ContentType.fromStringRequest(item.contentType)) {
                            Constants.ContentType.ANIME -> {
                                val navWithAction = AISuggestionsFragmentDirections.actionAISuggestionsFragmentToAnimeDetailsFragment(item.contentId)
                                navController.navigate(navWithAction)
                            }
                            Constants.ContentType.MOVIE -> {
                                val navWithAction = AISuggestionsFragmentDirections.actionAISuggestionsFragmentToMovieDetailsFragment(item.contentId)
                                navController.navigate(navWithAction)
                            }
                            Constants.ContentType.TV -> {
                                val navWithAction = AISuggestionsFragmentDirections.actionAISuggestionsFragmentToTvDetailsFragment(item.contentId)
                                navController.navigate(navWithAction)
                            }
                            Constants.ContentType.GAME -> {
                                val navWithAction = AISuggestionsFragmentDirections.actionAISuggestionsFragmentToGameDetailsFragment(item.contentId)
                                navController.navigate(navWithAction)
                            }
                        }
                    }
                }

                override fun onErrorRefreshPressed() {
                    viewModel.getAISuggestions()
                }

                override fun onCancelPressed() {
                    navController.popBackStack()
                }

                override fun onExhaustButtonPressed() {}
            })
            adapter = suggestionsAdapter
        }
    }

    private fun setObservers() {
        viewModel.aiSuggestionsResponse.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkResponse.Failure -> suggestionsAdapter?.setErrorView(response.errorMessage)
                NetworkResponse.Loading -> suggestionsAdapter?.setLoadingView()
                is NetworkResponse.Success -> {
                    setCountDownTimer(response.data.data.createdAt)
                    suggestionsAdapter?.setData(response.data.data.suggestions.toCollection(ArrayList()))
                }
            }
        }


    }

    override fun onDestroyView() {
        viewModel.aiSuggestionsResponse.removeObservers(viewLifecycleOwner)

        suggestionsAdapter = null

        countDownTimer?.cancel()
        countDownTimer = null
        confirmDialog?.dismiss()
        confirmDialog = null
        super.onDestroyView()
    }
}