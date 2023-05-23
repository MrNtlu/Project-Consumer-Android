package com.mrntlu.projectconsumer.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mrntlu.projectconsumer.databinding.FragmentAuthBinding
import com.mrntlu.projectconsumer.ui.BaseFragment

class AuthFragment : BaseFragment<FragmentAuthBinding>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}