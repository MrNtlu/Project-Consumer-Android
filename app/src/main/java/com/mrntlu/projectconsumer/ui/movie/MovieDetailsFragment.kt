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
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout.LayoutParams
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentMovieDetailsBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.roundSingleDecimal
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisible
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
        setListeners()
    }

    private fun setUI() {
        //TODO if both images are null, hide.
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

        args.movieArgs.apply {
            //TODO Check translations and set
            binding.detailsTitleTV.text = title
            binding.detailsOriginalTV.text = titleOriginal

            binding.detailsInclude.apply {
                interactionRateTV.text = tmdbVote.roundSingleDecimal().toString()
                interactionsRateCountTV.text = " | $tmdbVoteCount"
            }

            val lengthStr = if (length > 10) {
                val hours = length / 60
                val minutes = length % 60
                String.format("%02dh %02dm • ", hours, minutes)
            } else null

            val releaseText = "${lengthStr ?: ""}${if (releaseDate.isNotEmptyOrBlank()) releaseDate.take(4) else status}"

            binding.detailsReleaseTV.text = releaseText

            binding.detailsDescriptionTV.text = description

            if (imdbID == null)
                binding.imdbButton.setGone()
            else binding.imdbButton.setVisible()
        }
    }

    private fun setListeners() {
        binding.apply {
            detailsToolbarBackButton.setOnClickListener {
                navController.popBackStack()
            }

            detailsDescriptionTV.setOnClickListener {
                detailsDescriptionTV.toggle()
            }

            detailsInclude.bookmarkInteractionButton.setOnClickListener {
                detailsInclude.bookmarkInteractionButton.setImageDrawable(ContextCompat.getDrawable(it.context, R.drawable.ic_bookmark_filled))
            }

            detailsInclude.watchLaterInteractionButton.setOnClickListener {
                detailsInclude.watchLaterInteractionButton.setImageDrawable(ContextCompat.getDrawable(it.context, R.drawable.ic_watchlist_filled))
            }

            imdbButton.setOnClickListener {
                //Open in web with id
            }

            tmdbButton.setOnClickListener {
                //Open in web with id
            }
        }
    }

    override fun onDestroyView() {
        activity?.let {
            it.window.statusBarColor = ContextCompat.getColor(it, if (sharedViewModel.isLightTheme()) R.color.darkWhite else R.color.androidBlack)
        }

        super.onDestroyView()
    }
}