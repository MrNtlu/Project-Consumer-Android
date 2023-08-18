package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentPolicyBinding
import com.mrntlu.projectconsumer.ui.BaseFragment

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

        val policy = if (args.isPrivacy) getString(R.string.privacy_policy) else getString(R.string.terms_conditions)
        binding.htmlText.apply {
            text = Html.fromHtml(policy, Html.FROM_HTML_MODE_COMPACT)
            isClickable = true
            movementMethod = LinkMovementMethod.getInstance()
        }
    }
}