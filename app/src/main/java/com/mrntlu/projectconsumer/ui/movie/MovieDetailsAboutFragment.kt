package com.mrntlu.projectconsumer.ui.movie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.DetailsAdapter
import com.mrntlu.projectconsumer.adapters.GenreAdapter
import com.mrntlu.projectconsumer.adapters.RecommendationsAdapter
import com.mrntlu.projectconsumer.databinding.FragmentMovieDetailsAboutBinding
import com.mrntlu.projectconsumer.models.common.DetailsUI
import com.mrntlu.projectconsumer.models.main.movie.MovieDetails
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.Constants.BASE_IMDB_URL
import com.mrntlu.projectconsumer.utils.Constants.BASE_TMDB_URL
import com.mrntlu.projectconsumer.utils.Constants.ContentType
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.isNotEmptyOrBlank
import com.mrntlu.projectconsumer.utils.openInBrowser
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieDetailsAboutFragment(
    private val movieDetails: MovieDetails,
) : BaseFragment<FragmentMovieDetailsAboutBinding>() {

    companion object {
        private const val RECOMMENDATION_POSITION = "recommendation_position"
    }

    private var genreAdapter: GenreAdapter? = null
    private var companiesAdapter: DetailsAdapter? = null
    private var recommendationsAdapter: RecommendationsAdapter? = null

    private var recommendationPosition: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailsAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            recommendationPosition = it.getInt(RECOMMENDATION_POSITION)
        }

        setListeners()
        setUI()
        setRecyclerView()
    }

    private fun setListeners() {
        binding.apply {
            detailsDescriptionTV.setOnClickListener {
                detailsDescriptionTV.toggle()
            }

            imdbButton.setOnClickListener {
                movieDetails.imdbID?.let {
                    val url = "${BASE_IMDB_URL}$it"

                    context?.openInBrowser(url)
                }
            }

            tmdbButton.setOnClickListener {
                movieDetails.tmdbID.let {
                    val url = "${BASE_TMDB_URL}movie/$it"

                    context?.openInBrowser(url)
                }
            }
        }
    }

    private fun setUI() {
        movieDetails.apply {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val descriptionStr = if (!translations.isNullOrEmpty()) {
                    val translation =
                        translations.firstOrNull { it.lanCode == sharedViewModel.getLanguageCode() }?.description
                    if (translation?.isNotEmptyOrBlank() == true)
                        translation
                    else description
                } else description

                withContext(Dispatchers.Main) {
                    binding.detailsDescriptionTV.text = descriptionStr
                }
            }

            binding.imdbButton.setVisibilityByCondition(imdbID == null)
        }
    }

    private fun setRecyclerView() {
        if (movieDetails.genres.isNotEmpty()) {
            binding.detailsGenreRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout
//                val bulletDecoration = BulletItemDecoration(context)
//                addItemDecoration(bulletDecoration)

                genreAdapter = GenreAdapter(movieDetails.genres) {
                    val navWithAction = MovieDetailsFragmentDirections.actionMovieDetailsFragmentToDiscoverListFragment(
                        ContentType.MOVIE,
                        movieDetails.genres[it]
                    )
                    navController.navigate(navWithAction)
                }
                setHasFixedSize(true)
                adapter = genreAdapter
            }
        } else {
            binding.detailsGenreTV.setGone()
            binding.detailsGenreRV.setGone()
        }

        if (movieDetails.recommendations.isNotEmpty()) {
            binding.detailsRecommendationRV.apply {
                val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                layoutManager = linearLayout

                recommendationsAdapter = RecommendationsAdapter(movieDetails.recommendations) { position, recommendation ->
//                    isAppBarLifted = binding.detailsAppBarLayout.isLifted
                    recommendationPosition = position

                    val navWithAction =
                        MovieDetailsFragmentDirections.actionMovieDetailsFragmentSelf(
                            recommendation.tmdbID
                        )
                    navController.navigate(navWithAction)
                }
                adapter = recommendationsAdapter

                if (recommendationPosition != null)
                    scrollToPosition(recommendationPosition!!)
            }
        } else {
            binding.detailsRecommendationTV.setGone()
            binding.detailsRecommendationRV.setGone()
        }

        if (!movieDetails.productionCompanies.isNullOrEmpty()) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val productionAndCompanyUIList = movieDetails.productionCompanies.filter {
                    it.name.isNotEmptyOrBlank()
                }.map {
                    DetailsUI(
                        it.name,
                        it.logo ?: "",
                        it.originCountry
                    )
                }

                withContext(Dispatchers.Main) {
                    binding.detailsProductionRV.apply {
                        val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        layoutManager = linearLayout

                        setHasFixedSize(true)

                        companiesAdapter = DetailsAdapter(
                            R.drawable.ic_company_75,
                            binding.root.context.dpToPxFloat(12f),
                            productionAndCompanyUIList,
                        ) { transform(CenterInside()) }
                        adapter = companiesAdapter
                    }
                }
            }
        } else {
            binding.detailsProductionTV.setGone()
            binding.detailsProductionRV.setGone()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (recommendationPosition != null)
            outState.putInt(RECOMMENDATION_POSITION, recommendationPosition!!)
    }

    override fun onDestroyView() {
        recommendationsAdapter = null
        companiesAdapter = null
        genreAdapter = null
        super.onDestroyView()
    }
}