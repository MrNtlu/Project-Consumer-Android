package com.mrntlu.projectconsumer.ui

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.DetailsAdapter
import com.mrntlu.projectconsumer.models.common.DetailsUI
import com.mrntlu.projectconsumer.models.common.retrofit.MessageResponse
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.viewmodels.main.ConsumeLaterViewModel
import java.util.Locale

abstract class BaseDetailsFragment<T>: BaseFragment<T>() {

    protected val consumeLaterViewModel: ConsumeLaterViewModel by viewModels()

    private var isResponseFailed = false
    private lateinit var countryCode: String

    private var consumeLaterDeleteLiveData: LiveData<NetworkResponse<MessageResponse>>? = null

    private val countryList = Locale.getISOCountries().filter { it.length == 2 }.map {
        val locale = Locale("", it)
        Pair(locale.displayCountry, locale.country.uppercase())
    }.sortedBy {
        it.first
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = Color.TRANSPARENT
        countryCode = sharedViewModel.getCountryCode()
    }

    private fun createDetailsAdapter(
        recyclerView: RecyclerView, detailsList: List<DetailsUI>,
        placeHolderImage: Int = R.drawable.ic_person_75, cardCornerRadius: Float = 93F,
        transformImage: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable> = { centerCrop() },
        setAdapter: (DetailsAdapter) -> DetailsAdapter,
    ) {
        recyclerView.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout

            adapter = setAdapter(DetailsAdapter(placeHolderImage, cardCornerRadius, detailsList, transformImage))
        }
    }

    override fun onDestroyView() {
        activity?.let {
            it.window.statusBarColor = ContextCompat.getColor(it, if (sharedViewModel.isLightTheme()) R.color.darkWhite else R.color.androidBlack)
        }
        super.onDestroyView()
    }
}