package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.databinding.FragmentMovieDetailsBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel

class MovieDetailsFragment : BaseFragment<FragmentMovieDetailsBinding>() {

    private val sharedViewModel: ActivitySharedViewModel by activityViewModels()
    private val args: MovieDetailsFragmentArgs by navArgs()

    //TODO Pass movie information and make new request
    //while fetching show passed info and update after fetch
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detailsTV.text = args.movieArgs.title
    }

    override fun onDestroyView() {

        super.onDestroyView()
    }
}