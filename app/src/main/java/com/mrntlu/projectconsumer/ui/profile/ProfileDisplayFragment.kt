package com.mrntlu.projectconsumer.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentProfileDisplayBinding
import com.mrntlu.projectconsumer.ui.BaseProfileFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.hideKeyboard
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.showInfoDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileDisplayFragment : BaseProfileFragment<FragmentProfileDisplayBinding>() {

    private lateinit var dialog: LoadingDialog

    private val args: ProfileDisplayFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            dialog = LoadingDialog(it)
        }

        setToolbar()
    }

    private fun setToolbar() {
        binding.profileDisplayToolbar.apply {
            title = getString(R.string.profile)
            setOnMenuItemClickListener {
                hideKeyboard()

                when(it.itemId) {
                    R.id.shareMenu -> {
                        //TODO Share URL
                    }
                }

                true
            }
        }
    }

    override fun onStart() {
        super.onStart()

        //TODO Implement
//        if (viewModel.userInfoResponse.value != null) {
//            binding.loadingLayout.setVisible()
//            viewModel.getUserInfo()
//        }

        if (userInfo?.image != null)
            setImage(
                userInfo?.image ?: "",
                binding.profileDisplayIV,
                binding.profileDisplayPlaceHolderIV,
                binding.profileDisplayImageProgressBar,
            )
    }

    override fun onStop() {
        if (userInfo?.image != null)
            Glide.with(this).clear(binding.profileDisplayIV)
        super.onStop()
    }

    private fun setListeners() {
        binding.apply {

            legendInfoButton.setSafeOnClickListener {
                context?.showInfoDialog(getString(R.string.legend_content_info))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}