package com.mrntlu.projectconsumer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrntlu.projectconsumer.adapters.PreviewAdapter
import com.mrntlu.projectconsumer.databinding.FragmentPreviewBinding
import com.mrntlu.projectconsumer.interfaces.ContentModel
import com.mrntlu.projectconsumer.interfaces.Interaction
import com.mrntlu.projectconsumer.models.common.retrofit.DataPaginationResponse
import com.mrntlu.projectconsumer.models.main.movie.Movie
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.viewmodels.shared.ViewPagerSharedViewModel

abstract class BasePreviewFragment<T: ContentModel>: BaseFragment<FragmentPreviewBinding>() {

    private val viewPagerSharedViewModel: ViewPagerSharedViewModel by activityViewModels()

    protected var upcomingAdapter: PreviewAdapter<T>? = null
    protected var popularAdapter: PreviewAdapter<T>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    protected fun setScrollListener() {
        binding.previewScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            viewPagerSharedViewModel.setScrollYPosition(scrollY)
        }
    }

    protected fun setRecyclerView(
        firstOnItemSelected: (String) -> Unit,
        firstOnRefreshPressed: () -> Unit,
        secondOnItemSelected: (String) -> Unit,
        secondOnRefreshPressed: () -> Unit,
    ) {
        binding.upcomingPreviewRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            upcomingAdapter = PreviewAdapter(object: Interaction<T> {
                override fun onItemSelected(item: T, position: Int) {
                    firstOnItemSelected(item.id)
                }

                override fun onCancelPressed() {
                    navController.popBackStack()
                }

                override fun onErrorRefreshPressed() {
                    firstOnRefreshPressed()
                }

                override fun onExhaustButtonPressed() {}
            }, isDarkTheme = !sharedViewModel.isLightTheme())
            adapter = upcomingAdapter
        }

        binding.popularPreviewRV.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,false)
            popularAdapter = PreviewAdapter(object: Interaction<T> {
                override fun onItemSelected(item: T, position: Int) {
                    secondOnItemSelected(item.id)
                }

                override fun onCancelPressed() {
                    navController.popBackStack()
                }

                override fun onErrorRefreshPressed() {
                    secondOnRefreshPressed()
                }

                override fun onExhaustButtonPressed() {}
            }, isDarkTheme = !sharedViewModel.isLightTheme())
            adapter = popularAdapter
        }
    }

    protected fun handleObserver(response: NetworkResponse<DataPaginationResponse<T>>, adapter: PreviewAdapter<T>?) {
        when(response) {
            is NetworkResponse.Failure -> adapter?.setErrorView(response.errorMessage)
            NetworkResponse.Loading -> adapter?.setLoadingView()
            is NetworkResponse.Success -> {
                adapter?.setData(response.data.data)
            }
        }
    }

    override fun onDestroyView() {
        sharedViewModel.networkStatus.removeObservers(this)
        binding.previewScrollView.setOnScrollChangeListener(null)
        upcomingAdapter = null
        popularAdapter = null
        super.onDestroyView()
    }
}