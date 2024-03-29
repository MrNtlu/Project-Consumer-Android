package com.mrntlu.projectconsumer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.ContentAdapter
import com.mrntlu.projectconsumer.databinding.FragmentUserListBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.utils.RecyclerViewEnum
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.quickScrollToTop
import com.mrntlu.projectconsumer.utils.setGone
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

abstract class BaseDayOfWeekFragment<T: ContentModel>: BaseFragment<FragmentUserListBinding>() {

    protected var contentAdapter: ContentAdapter<T>? = null
    protected var gridCount = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    protected fun setToolbar() {
        binding.userListToolbar.apply {
            title = getString(R.string.airing_today)

            setNavigationOnClickListener { navController.popBackStack() }
        }
    }

    protected fun setUI() {
        //1 Sunday 7 Saturday
        //LocalDate.now().dayOfWeek.value
        //DayOfWeek.valueOf(dayOfWeek.name).value
        binding.userListTabLayout.tabLayout.apply {
            if (tabCount < DayOfWeek.values().size) {
                for (dayOfWeek in DayOfWeek.values()) {
                    addTab(
                        newTab().setText(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())),
                        dayOfWeek.value == if (LocalDate.now().dayOfWeek.value == 7) 1 else LocalDate.now().dayOfWeek.value
                    )
                }

                for (position in 0..tabCount.minus(1)) {
                    val layout = LayoutInflater.from(context).inflate(R.layout.layout_tab_title, null) as? LinearLayout

                    val tabIV = layout?.findViewById<ImageView>(R.id.tabIV)
                    val tabLayoutParams = layoutParams

                    tabLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    layoutParams = tabLayoutParams

                    tabIV?.setGone()

                    getTabAt(position)?.customView = layout
                }

                post {
                    val tabView = (getChildAt(0) as ViewGroup).getChildAt(selectedTabPosition)
                    val scrollToX = tabView.left - (width - tabView.width) / 2
                    scrollTo(scrollToX, 0)
                }
            }
        }
    }

    protected fun setRecyclerView(
        startFetch: () -> Unit,
        onItemSelected: (String) -> Unit,
    ) {
        binding.userListRV.apply {
            val rvLayoutManager = if(sharedViewModel.isAltLayout()) {
                val linearLayoutManager = LinearLayoutManager(this.context)

                val divider = MaterialDividerItemDecoration(this.context, LinearLayoutManager.VERTICAL)
                divider.apply {
                    dividerInsetStart = context.dpToPx(119f)
                    dividerInsetEnd = context.dpToPx(8f)
                    dividerThickness = context.dpToPx(1f)
                    isLastItemDecorated = false
                }
                addItemDecoration(divider)

                gridCount = 1
                linearLayoutManager
            } else {
                val gridLayoutManager = GridLayoutManager(this.context, gridCount)

                gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        val itemViewType = contentAdapter?.getItemViewType(position)
                        return if (
                            itemViewType == RecyclerViewEnum.View.value ||
                            itemViewType == RecyclerViewEnum.Loading.value
                        ) 1 else gridCount
                    }
                }
                gridLayoutManager
            }
            layoutManager = rvLayoutManager

            contentAdapter = ContentAdapter(
                gridCount = gridCount,
                isDarkTheme = !sharedViewModel.isLightTheme(),
                isAltLayout = sharedViewModel.isAltLayout(),
                radiusInPx = context.dpToPxFloat(8f),
                sizeMultiplier = if (sharedViewModel.isAltLayout()) 0.8f else 0.9f,
                interaction = object: Interaction<T> {
                    override fun onItemSelected(item: T, position: Int) {
                        onItemSelected(item.id)
                    }

                    override fun onErrorRefreshPressed() {
                        startFetch()
                    }

                    override fun onCancelPressed() {
                        navController.popBackStack()
                    }

                    override fun onExhaustButtonPressed() {
                        viewLifecycleOwner.lifecycleScope.launch {
                            quickScrollToTop()
                        }
                    }
                }
            )
            adapter = contentAdapter
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            sharedViewModel.windowSize.removeObservers(this)
        }
        contentAdapter = null
        super.onDestroyView()
    }
}