package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.databinding.FragmentMovieSearchBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.printLog

class MovieSearchFragment : BaseFragment<FragmentMovieSearchBinding>() {

    private val args: MovieSearchFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        printLog(args.searchQuery)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}