package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.mrntlu.projectconsumer.databinding.FragmentMovieBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.NetworkListResponse
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.viewmodels.movie.MovieViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieFragment : BaseFragment<FragmentMovieBinding>() {

    private val viewModel: MovieViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
    }

    private fun setObservers() {
        viewModel.upcomingMovies.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkListResponse.Failure -> {
                    printLog("Error ${response.errorMessage}")
                }
                is NetworkListResponse.Loading -> {
                    printLog("Loading")
                }
                is NetworkListResponse.Success -> {
                    printLog("Success ${response.data}\n" +
                            "IsPaginationData: ${response.isPaginationData}\n" +
                            "IsPaginationExhausted: ${response.isPaginationExhausted}")
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}