package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.centerCrop
import com.google.android.material.tabs.TabLayout
import com.mrntlu.projectconsumer.databinding.FragmentProfileBinding
import com.mrntlu.projectconsumer.models.auth.UserInfo
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.viewmodels.main.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    private companion object {
        private const val KEY_VALUE = "content_type"
    }

    private val viewModel: ProfileViewModel by viewModels()

    private var isResponseFailed = false
    private var userInfo: UserInfo? = null
    private var contentType: Constants.ContentType = Constants.ContentType.MOVIE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            contentType = Constants.ContentType.fromStringValue(
                it.getString(KEY_VALUE, Constants.ContentType.MOVIE.value)
            )
        }

        setObservers()
    }

    private fun setObservers() {
        if (!(viewModel.userInfoResponse.hasObservers() || viewModel.userInfoResponse.value is NetworkResponse.Success || viewModel.userInfoResponse.value is NetworkResponse.Loading))
            viewModel.getUserInfo()

        viewModel.userInfoResponse.observe(viewLifecycleOwner) { response ->
            binding.apply {
                isResponseFailed = response is NetworkResponse.Failure
                loadingLayout.setVisibilityByCondition(response !is NetworkResponse.Loading)
                errorLayout.setVisibilityByCondition(response !is NetworkResponse.Failure)

                when(response) {
                    is NetworkResponse.Failure -> {
                        errorLayoutInc.apply {
                            errorText.text = response.errorMessage

                            setListeners()
                        }
                    }
                    is NetworkResponse.Success -> {
                        userInfo = response.data.data

                        setUI()
                        setListeners()
                        setRecyclerView()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun setUI() {
        userInfo?.let {
            binding.apply {
                profileIV.loadWithGlide(
                    it.image ?: "",
                    profilePlaceHolderIV,
                    profileImageProgressBar,
                ) {
                    centerCrop()
                }

                val username = "@${it.username}"
                profileUsernameTV.text = username

                movieStatTV.text = it.movieCount.toString()
                tvStatTV.text = it.tvCount.toString()
                gameStatTV.text = it.gameCount.toString()
                animeStatTV.text = it.animeCount.toString()

                profileContentTabLayout.apply {
                    for (tab in Constants.TabList) {
                        addTab(
                            profileContentTabLayout.newTab().setText(tab),
                            tab == contentType.value
                        )
                    }
                }
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            profileMyListButton.setOnClickListener {

            }

            profileDiaryButton.setOnClickListener {

            }

            legendInfoButton.setOnClickListener {

            }

            profileContentTabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when(tab?.position) {
                        0 -> {
                            contentType = Constants.ContentType.MOVIE
                        }
                        1 -> {
                            contentType = Constants.ContentType.TV
                        }
                        2 -> {
                            contentType = Constants.ContentType.ANIME
                        }
                        3 -> {
                            contentType = Constants.ContentType.GAME
                        }
                        else -> {}
                    }

                    //Change recyclerview
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun setRecyclerView() {

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(KEY_VALUE, contentType.value)
    }

    override fun onDestroyView() {
        viewModel.userInfoResponse.removeObservers(viewLifecycleOwner)

        super.onDestroyView()
    }
}