package com.mrntlu.projectconsumer.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.LayoutUserListBottomSheetBinding
import com.mrntlu.projectconsumer.interfaces.BottomSheetOperation
import com.mrntlu.projectconsumer.interfaces.BottomSheetState
import com.mrntlu.projectconsumer.interfaces.UserListContentModel
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.getColorFromAttr
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserListBottomSheet(
    private val userListContentModel: UserListContentModel,
    private val contentType: Constants.ContentType,
    private var bottomSheetState: BottomSheetState,
): BottomSheetDialogFragment() {
    companion object {
        const val TAG = "UserListBottomSheet"
    }

    private var bottomSheetOperation = if (bottomSheetState == BottomSheetState.EDIT) BottomSheetOperation.UPDATE else BottomSheetOperation.DELETE

    private var _binding: LayoutUserListBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutUserListBottomSheetBinding.inflate(inflater, container, false)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUI()
        setListeners()

        //TODO Reimplement view and set it for details too
        // get contenttype

        //TODO On edit pressed, incrementing the episode and season should be very easy and 1 tap.
    }

    private fun setUI() {
        binding.apply {
            binding.layoutViewInc.root.setVisibilityByCondition(bottomSheetState != BottomSheetState.VIEW)
            binding.layoutEditInc.root.setVisibilityByCondition(bottomSheetState != BottomSheetState.EDIT)

            if (bottomSheetState == BottomSheetState.VIEW) {
                bottomSheetOperation = BottomSheetOperation.DELETE

            } else {
                bottomSheetOperation = BottomSheetOperation.UPDATE

                binding.layoutEditInc.apply {
                    setTabLayout(toggleTabLayout)

                    toggleTabLayout.getTabAt(
                        when (userListContentModel.contentStatus) {
                            Constants.UserListStatus[0].request -> 0
                            Constants.UserListStatus[1].request -> 1
                            else -> 2
                        }
                    )?.select()

                    userListContentModel.apply {
                        setACTVSelection(score?.plus(1) ?: 0)

                        timesFinishedTextInputET.setText(timesFinished.toString())
                        watchedSeasonTextInputET.setText(watchedSeasons.toString())
                        watchedEpisodeTextInputET.setText(mainAttribute.toString())
                        watchedSeasonTextLayout.suffixText = if (totalSeasons != null) "/$totalSeasons" else null
                        watchedEpisodeTextLayout.suffixText = if (totalEpisodes != null) "/$totalEpisodes" else null
                    }

                    setSelectedTabColors(
                        binding.root.context,
                        toggleTabLayout.getTabAt(toggleTabLayout.selectedTabPosition),
                        toggleTabLayout,
                    )
                }
            }

            binding.saveButton.text = getString(if (bottomSheetState == BottomSheetState.EDIT) R.string.save else R.string.update)
            binding.cancelButton.text = getString(R.string.cancel)
        }
    }

    private fun setTabLayout(tabLayout: TabLayout) {
        tabLayout.apply {
            for (tab in Constants.UserListStatus) {
                val customTab = newTab().setCustomView(R.layout.cell_status_tab_layout)
                addTab(customTab)
                customTab.customView?.findViewById<TextView>(R.id.tabTV)?.text = tab.name
            }
        }
    }

    private fun setACTVSelection(position: Int) {
        binding.layoutEditInc.scoreSelectionACTV.apply {
            setText(adapter.getItem(position).toString(), false)
            dismissDropDown()
        }
    }

    private fun setListeners() {
        binding.apply {
            layoutEditInc.apply {
                watchedSeasonTextLayout.setEndIconOnClickListener {

                }

                watchedEpisodeTextLayout.setEndIconOnClickListener {

                }
            }
        }
    }

    private fun setSelectedTabColors(context: Context, tab: TabLayout.Tab?, tabLayout: TabLayout) {
        val tv = tab?.customView?.findViewById<TextView>(R.id.tabTV)

        val attrColor = when(tv?.text) {
            Constants.UserListStatus[0].name -> R.attr.statusActiveColor
            Constants.UserListStatus[1].name -> R.attr.statusFinishedColor
            else -> R.attr.statusDroppedColor
        }

        val color = context.getColorFromAttr(attrColor)
        tv?.setTextColor(color)
        tabLayout.setSelectedTabIndicatorColor(color)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}