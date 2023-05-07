package com.mrntlu.projectconsumer.ui.movie

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout.LayoutParams
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentMovieDetailsBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.roundSingleDecimal
import com.mrntlu.projectconsumer.utils.setGone
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
        activity?.window?.statusBarColor = Color.TRANSPARENT

        setUI()
    }

    private fun setUI() {
        Glide.with(requireContext()).load("https://image.tmdb.org/t/p/original/wxgD3fB5lQ2sGJLog0rvXW049Pf.jpg").addListener(object: RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                binding.detailsToolbarProgress.setGone()
                binding.detailsAppBarLayout.setExpanded(false)
                val collapsingLayoutParams: LayoutParams = binding.detailsCollapsingToolbar.layoutParams as LayoutParams
                collapsingLayoutParams.scrollFlags = -1
                return false
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                binding.detailsToolbarProgress.setGone()
                return false
            }
        }).into(binding.detailsToolbarIV)

        binding.detailsToolbarBackButton.setOnClickListener {
            navController.popBackStack()
        }

        args.movieArgs.apply {
            binding.detailsPosterIV.loadWithGlide(imageURL, null, binding.detailsPosterProgress) {
                centerCrop().transform(RoundedCorners(12))
            }

            //TODO Check translations and set
            binding.detailsTitleTV.text = title
            binding.detailsOriginalTV.text = titleOriginal

            binding.detailsRateTV.text = tmdbVote.roundSingleDecimal().toString()
            binding.detailsRateCountTV.text = " | $tmdbVoteCount"

            val genres = genres.joinToString(", ") { it.name }

            val releaseText = "$genres • ${if (releaseDate.isNotEmptyOrBlank()) releaseDate.take(4) else status}"

            binding.detailsReleaseTV.text = releaseText

            binding.detailsDescriptionTV.text = description
        }
    }

    override fun onDestroyView() {
        activity?.let {
            it.window.statusBarColor = ContextCompat.getColor(it, if (sharedViewModel.isLightTheme()) R.color.darkWhite else R.color.black)
        }

        super.onDestroyView()
    }
}