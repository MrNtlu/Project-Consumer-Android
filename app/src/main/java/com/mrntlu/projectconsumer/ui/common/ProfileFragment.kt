package com.mrntlu.projectconsumer.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mrntlu.projectconsumer.databinding.FragmentProfileBinding
import com.mrntlu.projectconsumer.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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

    private fun setObservers() {

    }

    private fun setUI() {
        binding.apply {

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
        }
    }

    private fun setRecyclerView() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}