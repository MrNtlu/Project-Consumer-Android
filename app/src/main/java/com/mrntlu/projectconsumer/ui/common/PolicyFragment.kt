package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.databinding.FragmentPolicyBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.Constants.PRIVACY_POLICY_URL
import com.mrntlu.projectconsumer.utils.Constants.TERMS_CONDITIONS_URL

class PolicyFragment : BaseFragment<FragmentPolicyBinding>() {

    private val args: PolicyFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = if (args.isPrivacy) PRIVACY_POLICY_URL else TERMS_CONDITIONS_URL
        if (URLUtil.isValidUrl(url))
            binding.webView.loadUrl(url)
    }

    override fun onResume() {
        binding.webView.onResume()
        super.onResume()
    }

    override fun onPause() {
        binding.webView.onPause()
        super.onPause()
    }

    override fun onDestroyView() {
        binding.webView.destroy()
        super.onDestroyView()
    }
}