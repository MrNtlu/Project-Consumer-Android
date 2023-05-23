package com.mrntlu.projectconsumer.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mrntlu.projectconsumer.viewmodels.shared.ActivitySharedViewModel

abstract class BaseFragment<T>: Fragment() {
    protected var _binding: T? = null
    protected val binding get() = _binding!!
    protected val sharedViewModel: ActivitySharedViewModel by activityViewModels()

    protected lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    // To prevent memory leak
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}