package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mrntlu.projectconsumer.databinding.FragmentProfileBinding
import com.mrntlu.projectconsumer.ui.BaseFragment

class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}