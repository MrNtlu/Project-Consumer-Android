package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.ConsumeLaterAdapter
import com.mrntlu.projectconsumer.databinding.FragmentListBinding
import com.mrntlu.projectconsumer.interfaces.ConsumeLaterInteraction
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.main.userInteraction.ConsumeLaterResponse
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.Operation
import com.mrntlu.projectconsumer.utils.OperationEnum
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.isFailed
import com.mrntlu.projectconsumer.utils.isSuccessful
import com.mrntlu.projectconsumer.viewmodels.ConsumeLaterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConsumeLaterFragment : BaseFragment<FragmentListBinding>() {

    private val viewModel: ConsumeLaterViewModel by viewModels()

    private var consumeLaterAdapter: ConsumeLaterAdapter? = null
    private var searchQuery: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setMenu()
        setRecyclerView()
        setObservers()
    }

    override fun onStart() {
        super.onStart()

        if (!viewModel.isRestoringData && !viewModel.didOrientationChange) {
            consumeLaterAdapter?.setLoadingView()
            viewModel.getConsumeLater()
        }
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.removeItem(R.id.settingsMenu)
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.consume_later_menu, menu)

                val searchView = menu.findItem(R.id.searchMenu).actionView as SearchView
                searchView.setQuery(searchQuery, false)
                searchView.clearFocus()

                searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        context?.hideKeyboard(searchView)

                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        consumeLaterAdapter?.search(newText)

                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId) {
                    R.id.sortMenu -> {

                    }
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setObservers() {
        viewModel.consumeLaterList.observe(viewLifecycleOwner) { response ->
            if (response.isFailed()) {
                consumeLaterAdapter?.setErrorView(response.errorMessage!!)
            } else if (response.isLoading) {
                consumeLaterAdapter?.setLoadingView()
            } else if (response.isSuccessful()) {
                consumeLaterAdapter?.setData(response.data!!.toCollection(ArrayList()))

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
            val linearLayout = LinearLayoutManager(context)
            layoutManager = linearLayout

            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.HORIZONTAL))

            consumeLaterAdapter = ConsumeLaterAdapter(object: ConsumeLaterInteraction{
                override fun onDeletePressed(item: ConsumeLaterResponse, position: Int) {
                    val deleteConsumerLiveData = viewModel.deleteConsumeLater(IDBody(item.id))
                    deleteConsumerLiveData.observe(viewLifecycleOwner) { response ->
                        if (response is NetworkResponse.Success)
                            consumeLaterAdapter?.handleOperation(Operation(item, position, OperationEnum.Delete))

                        //TODO handle loading and failure
                        //use dialog
                    }
                }

                override fun onAddToListPressed(item: ConsumeLaterResponse, position: Int) {
                    TODO("Not yet implemented")
                }

                override fun onItemSelected(item: ConsumeLaterResponse, position: Int) {
                    when(Constants.ContentType.fromStringRequest(item.contentType)) {
                        Constants.ContentType.ANIME -> TODO()
                        Constants.ContentType.MOVIE -> TODO()
                        Constants.ContentType.TV -> TODO()
                        Constants.ContentType.GAME -> TODO()
                    }
                }

                override fun onErrorRefreshPressed() {
                    TODO("Not yet implemented")
                }

                override fun onCancelPressed() {
                    TODO("Not yet implemented")
                }

                override fun onExhaustButtonPressed() {
                    TODO("Not yet implemented")
                }

                override fun onDiscoverButtonPressed() {
                    navController.navigate(R.id.action_navigation_later_to_navigation_discover)
                }
            })
            adapter = consumeLaterAdapter
        }
    }

    override fun onDestroyView() {
        viewModel.consumeLaterList.removeObservers(viewLifecycleOwner)
        consumeLaterAdapter = null
        super.onDestroyView()
    }
}