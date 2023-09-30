package com.mrntlu.projectconsumer.ui.movie

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrntlu.projectconsumer.adapters.StreamingAdapter
import com.mrntlu.projectconsumer.databinding.FragmentMovieDetailsStreamingBinding
import com.mrntlu.projectconsumer.models.common.Streaming
import com.mrntlu.projectconsumer.models.common.StreamingPlatform
import com.mrntlu.projectconsumer.models.main.movie.MovieDetails
import com.mrntlu.projectconsumer.ui.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MovieDetailsStreamingFragment(
    private val movieDetails: MovieDetails,
): BaseFragment<FragmentMovieDetailsStreamingBinding>() {

    private var streamingAdapter: StreamingAdapter? = null
    private var buyAdapter: StreamingAdapter? = null
    private var rentAdapter: StreamingAdapter? = null

    private lateinit var countryCode: String

    private val countryList = Locale.getISOCountries().filter { it.length == 2 }.map {
        val locale = Locale("", it)
        Pair(locale.displayCountry, locale.country.uppercase())
    }.sortedBy {
        it.first
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailsStreamingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countryCode = sharedViewModel.getCountryCode()

        setUI()
        setListeners()
    }

    private fun setUI() {
        setSpinner(binding.detailsStreamingCountrySpinner)

        if (!movieDetails.streaming.isNullOrEmpty()) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val streaming = movieDetails.streaming!!

                val streamingList = streaming.firstOrNull { it.countryCode == countryCode }?.streamingPlatforms
                val buyList = streaming.firstOrNull { it.countryCode == countryCode }?.buyOptions
                val rentList = streaming.firstOrNull { it.countryCode == countryCode }?.rentOptions

                withContext(Dispatchers.Main) {
                    createStreamingAdapter(
                        binding.detailsStreamingRV,
                        streamingList
                    ) {
                        streamingAdapter = it
                        it
                    }

                    createStreamingAdapter(
                        binding.detailsBuyRV,
                        buyList
                    ) {
                        buyAdapter = it
                        it
                    }

                    createStreamingAdapter(
                        binding.detailsRentRV,
                        rentList
                    ) {
                        rentAdapter = it
                        it
                    }
                }
            }
        }
    }

    private fun setListeners() {
        binding.detailsStreamingCountrySpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onCountrySpinnerSelected(
                    position, movieDetails.streaming,
                    streamingAdapter, buyAdapter, rentAdapter
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setSpinner(spinner: Spinner) {
        val spinnerAdapter = ArrayAdapter(spinner.context, R.layout.simple_spinner_item, countryList.map { it.first })
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        spinner.setSelection(
            countryList.indexOfFirst {
                it.second == countryCode
            }
        )
    }

    private fun onCountrySpinnerSelected(
        position: Int,
        streaming: List<Streaming>?,
        streamingAdapter: StreamingAdapter?,
        buyAdapter: StreamingAdapter?,
        rentAdapter: StreamingAdapter?,
    ) {
        countryCode = countryList[position].second
        val streamingList = streaming?.firstOrNull { it.countryCode == countryCode }

        streamingAdapter?.setNewList(streamingList?.streamingPlatforms ?: listOf())
        buyAdapter?.setNewList(streamingList?.buyOptions ?: listOf())
        rentAdapter?.setNewList(streamingList?.rentOptions ?: listOf())
    }

    private fun createStreamingAdapter(
        recyclerView: RecyclerView, streamingList: List<StreamingPlatform>?,
        setAdapter: (StreamingAdapter) -> StreamingAdapter,
    ) {
        recyclerView.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout

            setHasFixedSize(true)

            adapter = setAdapter(StreamingAdapter(streamingList ?: listOf()))
        }
    }

    override fun onDestroyView() {
        streamingAdapter = null
        buyAdapter = null
        rentAdapter = null

        super.onDestroyView()
    }
}