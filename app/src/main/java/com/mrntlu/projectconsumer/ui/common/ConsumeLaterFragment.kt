package com.mrntlu.projectconsumer.ui.common

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AbsListView
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.MainActivity
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.ConsumeLaterAdapter
import com.mrntlu.projectconsumer.databinding.FragmentListBinding
import com.mrntlu.projectconsumer.interfaces.ConsumeLaterInteraction
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.models.main.userInteraction.retrofit.MarkConsumeLaterBody
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.Orientation
import com.mrntlu.projectconsumer.utils.dpToPx
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.isFailed
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.utils.showConfirmationDialog
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.main.profile.ConsumeLaterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ConsumeLaterFragment: BaseFragment<FragmentListBinding>() {

    private val viewModel: ConsumeLaterViewModel by viewModels()

    private lateinit var dialog: LoadingDialog

    private var orientationEventListener: OrientationEventListener? = null

    private var consumeLaterAdapter: ConsumeLaterAdapter? = null
    private var confirmDialog: AlertDialog? = null
    private var scoreDialog: AlertDialog? = null
    private var sortPopupMenu: PopupMenu? = null
    private var searchView: SearchView? = null
    private var searchMenu: MenuItem? = null
    private var popupMenu: PopupMenu? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            dialog = LoadingDialog(it)
        }

        orientationEventListener = object : OrientationEventListener(view.context) {
            override fun onOrientationChanged(orientation: Int) {
                val defaultPortrait = 0
                val upsideDownPortrait = 180
                val rightLandscape = 90
                val leftLandscape = 270

                viewModel.viewModelScope.launch {
                    when {
                        isWithinOrientationRange(orientation, defaultPortrait) -> {
                            viewModel.setNewOrientation(Orientation.Portrait)
                        }
                        isWithinOrientationRange(orientation, leftLandscape) -> {
                            viewModel.setNewOrientation(Orientation.Landscape)
                        }
                        isWithinOrientationRange(orientation, upsideDownPortrait) -> {
                            viewModel.setNewOrientation(Orientation.PortraitReverse)
                        }
                        isWithinOrientationRange(orientation, rightLandscape) -> {
                            viewModel.setNewOrientation(Orientation.LandscapeReverse)
                        }
                    }
                }

                /*viewModel.setNewOrientation(
                    when (orientation) {
                        in 45..134 -> {
                            Orientation.Landscape
                        }
                        in 135..224 -> {
                            Orientation.Portrait
                        }
                        in 225..314 -> {
                            Orientation.LandscapeReverse
                        }
                        else -> {
                            Orientation.Portrait
                        }
                    }
                )*/
            }
        }
        orientationEventListener?.enable()

        setToolbar()
        setRecyclerView()
        setObservers()
        setListeners()
    }

    private suspend fun isWithinOrientationRange(
        currentOrientation: Int, targetOrientation: Int, epsilon: Int = 30
    ): Boolean = withContext(Dispatchers.Default){
        currentOrientation > targetOrientation - epsilon && currentOrientation < targetOrientation + epsilon
    }

    override fun onStart() {
        super.onStart()

        if (!viewModel.isRestoringData && !viewModel.didOrientationChange && viewModel.consumeLaterList.value != null) {
            consumeLaterAdapter?.setLoadingView()
            viewModel.getConsumeLater()
        }
    }

    private fun setToolbar() {
        binding.listToolbar.apply {
            title = getString(R.string.watch_later)
            setNavigationOnClickListener { navController.popBackStack() }

            inflateMenu(R.menu.search_toolbar_menu)

            searchMenu = menu.findItem(R.id.searchMenu)
            searchView = searchMenu?.actionView as SearchView

            try {
                val backgroundView = searchView?.findViewById(androidx.appcompat.R.id.search_plate) as? View
                backgroundView?.background = null
            } catch (_: Exception){}

            searchView?.queryHint = getString(R.string.search)
            searchView?.setQuery(viewModel.search, false)
            searchView?.clearFocus()

            searchView?.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchView?.let {
                        context?.hideKeyboard(it)
                    }

                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.search(newText)

                    return true
                }
            })

            val closeBtn = searchView?.findViewById<AppCompatImageView>(androidx.appcompat.R.id.search_close_btn)
            closeBtn?.setOnClickListener {
                if (viewModel.search == null)
                    resetSearchView()
                else {
                    viewModel.setSearch(null)
                    searchView?.setQuery(null, false)

                    hideKeyboard()
                }
            }

            setOnMenuItemClickListener { menuItem ->
                hideKeyboard()

                when(menuItem.itemId) {
                    R.id.sortMenu -> {
                        if (consumeLaterAdapter?.isLoading == false) {
                            if (sortPopupMenu == null) {
                                val menuItemView = requireActivity().findViewById<View>(R.id.sortMenu)
                                sortPopupMenu = PopupMenu(requireContext(), menuItemView)
                                sortPopupMenu!!.menuInflater.inflate(R.menu.sort_extra_menu, sortPopupMenu!!.menu)
                                sortPopupMenu!!.setForceShowIcon(true)
                            }

                            sortPopupMenu?.let {
                                val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
                                val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

                                for (i in 0..it.menu.size.minus(1)) {
                                    val popupMenuItem = it.menu[i]
                                    val sortType = Constants.SortConsumeLaterRequests[i]

                                    popupMenuItem.iconTintList = ContextCompat.getColorStateList(
                                        requireContext(),
                                        if(viewModel.sort == sortType.request) selectedColor else unselectedColor
                                    )
                                    popupMenuItem.title = sortType.name
                                }

                                it.setOnMenuItemClickListener { item ->
                                    val newSortType = when(item.itemId) {
                                        R.id.firstSortMenu -> {
                                            setPopupMenuItemVisibility(it, 0)

                                            Constants.SortConsumeLaterRequests[0].request
                                        }
                                        R.id.secondSortMenu -> {
                                            setPopupMenuItemVisibility(it, 1)

                                            Constants.SortConsumeLaterRequests[1].request
                                        }
                                        R.id.thirdSortMenu -> {
                                            setPopupMenuItemVisibility(it, 2)

                                            Constants.SortConsumeLaterRequests[2].request
                                        }
                                        R.id.forthSortMenu -> {
                                            setPopupMenuItemVisibility(it, 3)

                                            Constants.SortConsumeLaterRequests[3].request
                                        }
                                        else -> { Constants.SortConsumeLaterRequests[0].request }
                                    }

                                    item.isChecked = true

                                    if (newSortType != viewModel.sort) {
                                        resetSearchView()
                                        viewModel.setSort(newSortType)
                                        viewModel.getConsumeLater()
                                    }

                                    true
                                }

                                it.show()
                            }
                        }
                    }
                    R.id.filterMenu -> {
                        if (consumeLaterAdapter?.isLoading == false) {
                            if (popupMenu == null) {
                                val menuItemView = requireActivity().findViewById<View>(R.id.filterMenu)
                                popupMenu = PopupMenu(requireContext(), menuItemView)
                                popupMenu!!.menuInflater.inflate(R.menu.filter_consume_later_menu, popupMenu!!.menu)
                                popupMenu!!.setForceShowIcon(true)
                            }

                            popupMenu?.let {
                                val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
                                val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

                                for (i in 0..it.menu.size.minus(1)) {
                                    val popupMenuItem = it.menu[i]
                                    val contentType = Constants.ContentType.values()[i]

                                    popupMenuItem.iconTintList = ContextCompat.getColorStateList(
                                        requireContext(),
                                        if(viewModel.filter == contentType.request) selectedColor else unselectedColor
                                    )
                                    popupMenuItem.title = contentType.value
                                }

                                it.setOnMenuItemClickListener { item ->
                                    val newFilterType = when (item.itemId) {
                                        R.id.firstFilterMenu -> {
                                            setPopupMenuItemVisibility(it, 0)

                                            Constants.ContentType.values()[0]
                                        }
                                        R.id.secondFilterMenu -> {
                                            setPopupMenuItemVisibility(it, 1)

                                            Constants.ContentType.values()[1]
                                        }
                                        R.id.thirdFilterMenu -> {
                                            setPopupMenuItemVisibility(it, 2)

                                            Constants.ContentType.values()[2]
                                        }
                                        R.id.forthFilterMenu -> {
                                            setPopupMenuItemVisibility(it, 3)

                                            Constants.ContentType.values()[3]
                                        }
                                        else -> { Constants.ContentType.values()[0] }
                                    }

                                    item.isChecked = true

                                    resetSearchView()
                                    viewModel.setFilter(
                                        if (newFilterType.request != viewModel.filter)
                                            newFilterType.request
                                        else null
                                    )
                                    viewModel.getConsumeLater()

                                    true
                                }

                                it.show()
                            }
                        }
                    }
                }
                true
            }
        }
    }

    private fun setPopupMenuItemVisibility(popupMenu: PopupMenu, selectedIndex: Int) {
        val selectedColor = if (sharedViewModel.isLightTheme()) R.color.materialBlack else R.color.white
        val unselectedColor = if (sharedViewModel.isLightTheme()) R.color.white else R.color.materialBlack

        for(i in 0..popupMenu.menu.size.minus(1)) {
            popupMenu.menu[i].iconTintList = ContextCompat.getColorStateList(requireContext(), if(i == selectedIndex) selectedColor else unselectedColor)
        }
    }

    private fun setObservers() {
        viewModel.consumeLaterList.observe(viewLifecycleOwner) { response ->
            if (response.isFailed()) {
                consumeLaterAdapter?.setErrorView(response.errorMessage!!)
            } else if (response.isLoading) {
                consumeLaterAdapter?.setLoadingView()
            } else if (response.isSuccessful()) {
                viewModel.viewModelScope.launch {
                    consumeLaterAdapter?.setData(response.data!!.toCollection(ArrayList()))
                }

                if (viewModel.isRestoringData || viewModel.didOrientationChange) {
                    binding.listRV.scrollToPosition(viewModel.scrollPosition - 1)

                    if (viewModel.isRestoringData) {
                        viewModel.isRestoringData = false
                    } else {
                        viewModel.didOrientationChange = false
                    }
                }
            }
        }
    }

    private fun setRecyclerView() {
        binding.listRV.apply {
            setPadding(0, 0, 0, context.dpToPx(8F))
            clipToPadding = false

            val linearLayout = LinearLayoutManager(context)

//            val divider = MaterialDividerItemDecoration(this.context, LinearLayoutManager.VERTICAL)
//            divider.apply {
//                dividerInsetStart = context.dpToPx(100f)
//                dividerInsetEnd = context.dpToPx(8f)
//                dividerThickness = context.dpToPx(1f)
//                isLastItemDecorated = false
//            }
//            addItemDecoration(divider)

            layoutManager = linearLayout

            consumeLaterAdapter = ConsumeLaterAdapter(binding.root.context.dpToPxFloat(6f), object: ConsumeLaterInteraction {
                override fun onDeletePressed(item: ConsumeLaterResponse, position: Int) {
                    confirmDialog = context?.showConfirmationDialog(getString(R.string.do_you_want_to_delete)) {
                        val deleteConsumerLiveData = viewModel.deleteConsumeLater(IDBody(item.id))

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

                                    viewModel.viewModelScope.launch {
                                        consumeLaterAdapter?.handleOperation(Operation(item, position, OperationEnum.Delete))
                                    }
                                }
                            }
                        }
                    }
                }

                @SuppressLint("InflateParams")
                override fun onAddToListPressed(item: ConsumeLaterResponse, position: Int) {
                    activity?.let {
                        val builder = AlertDialog.Builder(it, R.style.WrapContentDialog)

                        builder.apply {
                            val dialogView = it.layoutInflater.inflate(R.layout.layout_score_dialog, null)
                            setView(dialogView)
                            setCancelable(true)
                            setPositiveButton(getString(R.string.save)) { sDialog, _ ->
                                val moveConsumerLiveData = viewModel.moveConsumeLaterAsUserList(MarkConsumeLaterBody(
                                    id = item.id,
                                    score = dialogView.findViewById<AutoCompleteTextView>(R.id.scoreSelectionACTV).text.toString().toIntOrNull()
                                ))

                                moveConsumerLiveData.observe(viewLifecycleOwner) { response ->
                                    when(response) {
                                        is NetworkResponse.Failure -> {
                                            if (::dialog.isInitialized)
                                                dialog.dismissDialog()

                                            context.showErrorDialog(response.errorMessage)
                                        }
                                        NetworkResponse.Loading -> {
                                            if (::dialog.isInitialized)
                                                dialog.showLoadingDialog()
                                        }
                                        is NetworkResponse.Success -> {
                                            if (::dialog.isInitialized)
                                                dialog.dismissDialog()

                                            viewModel.viewModelScope.launch {
                                                consumeLaterAdapter?.handleOperation(Operation(item, position, OperationEnum.Delete))
                                            }
                                        }
                                    }
                                }

                                sDialog.dismiss()
                            }
                            setNegativeButton(getString(R.string.cancel)) { sDialog, _ ->
                                sDialog.dismiss()
                            }
                            scoreDialog = create()
                        }

                        scoreDialog?.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.custom_popup_background))
                        val layoutParams = WindowManager.LayoutParams().apply {
                            copyFrom(scoreDialog?.window?.attributes)
                            width = WindowManager.LayoutParams.WRAP_CONTENT
                            height = WindowManager.LayoutParams.WRAP_CONTENT
                        }
                        scoreDialog?.window?.attributes = layoutParams
                        scoreDialog?.show()
                    }
                }

                override fun onItemSelected(item: ConsumeLaterResponse, position: Int) {
                    confirmDialog?.dismiss()
                    scoreDialog?.dismiss()

                    if (navController.currentDestination?.id == R.id.navigation_later) {
                        when(Constants.ContentType.fromStringRequest(item.contentType)) {
                            Constants.ContentType.ANIME -> {
                                val navWithAction = ConsumeLaterFragmentDirections.actionNavigationLaterToAnimeDetailsFragment(item.contentID)
                                navController.navigate(navWithAction)
                            }
                            Constants.ContentType.MOVIE -> {
                                val navWithAction = ConsumeLaterFragmentDirections.actionNavigationLaterToMovieDetailsFragment(item.contentID)
                                navController.navigate(navWithAction)
                            }
                            Constants.ContentType.TV -> {
                                val navWithAction = ConsumeLaterFragmentDirections.actionNavigationLaterToTvDetailsFragment(item.contentID)
                                navController.navigate(navWithAction)
                            }
                            Constants.ContentType.GAME -> {
                                val navWithAction = ConsumeLaterFragmentDirections.actionNavigationLaterToGameDetailsFragment(item.contentID)
                                navController.navigate(navWithAction)
                            }
                        }
                    }
                }

                override fun onErrorRefreshPressed() {
                    viewModel.getConsumeLater()
                }

                override fun onCancelPressed() {}

                override fun onExhaustButtonPressed() {}

                override fun onDiscoverButtonPressed() {
                    navController.popBackStack()
                    (activity as? MainActivity)?.navigateToHome()
                }
            })
            adapter = consumeLaterAdapter

            var isScrolling = false
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isScrolling = newState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val lastVisibleItemPosition = linearLayout.findLastVisibleItemPosition()

                    if (isScrolling) {
                        val centerScrollPosition = (linearLayout.findLastCompletelyVisibleItemPosition() + linearLayout.findFirstCompletelyVisibleItemPosition()) / 2
                        viewModel.setScrollPosition(centerScrollPosition)
                    }

                    val isScrollingUp = dy <= -90
                    val isScrollingDown = dy >= 10
                    val isThresholdPassed = lastVisibleItemPosition > Constants.PAGINATION_LIMIT

                    if (isThresholdPassed && isScrollingUp)
                        binding.topFAB.show()
                    else if (!isThresholdPassed || isScrollingDown)
                        binding.topFAB.hide()
                }
            })
        }
    }

    private fun setListeners() {
        binding.topFAB.setOnClickListener {
            binding.listRV.scrollToPosition(0)
        }
    }

    private fun resetSearchView() {
        viewModel.setSearch(null)
        searchView?.setQuery(null, false)
        searchView?.isIconified = true
        searchView?.clearFocus()

        searchView?.onActionViewCollapsed()
        searchMenu?.collapseActionView()

        hideKeyboard()
    }

    override fun onDestroyView() {
        viewModel.consumeLaterList.removeObservers(viewLifecycleOwner)

        orientationEventListener?.disable()
        orientationEventListener = null

        scoreDialog?.dismiss()
        confirmDialog?.dismiss()
        confirmDialog = null
        scoreDialog = null
        searchView = null
        searchMenu = null
        popupMenu = null
        sortPopupMenu = null
        consumeLaterAdapter = null
        super.onDestroyView()
    }
}