
package com.mrntlu.projectconsumer.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.ProfileImageAdapter
import com.mrntlu.projectconsumer.databinding.FragmentRegisterBinding
import com.mrntlu.projectconsumer.models.auth.retrofit.RegisterBody
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.LoadingDialog
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.isEmailValid
import com.mrntlu.projectconsumer.utils.isEmptyOrBlank
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.auth.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : BaseFragment<FragmentRegisterBinding>() {

    private val viewModel: RegisterViewModel by viewModels()

    private var profileImageAdapter: ProfileImageAdapter? = null

    private lateinit var fcmToken: String
    private lateinit var dialog: LoadingDialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            dialog = LoadingDialog(it)
        }

        getFCMToken()
        setRecyclerView()
        setListeners()
        setObservers()
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                fcmToken = "unknown_fcm_token"
                return@OnCompleteListener
            }

            fcmToken = task.result
        })
    }

    private fun setRecyclerView() {
        binding.profileImageRV.apply {
            val linearLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = linearLayout
            profileImageAdapter = ProfileImageAdapter()
            adapter = profileImageAdapter
        }
    }

    private fun setListeners() {
        binding.apply {
            registerButton.setOnClickListener {
                if (validate()) {
                    val body = RegisterBody(
                        mailET.text!!.toString(),
                        fcmToken,
                        usernameET.text!!.toString(),
                        passwordET.text!!.toString(),
                        profileImageAdapter?.getSelectedImage(),
                    )

                    viewModel.register(body)
                }
            }

            signInButton.setOnClickListener {
                navController.popBackStack()
            }
        }
    }

    private fun setObservers() {
        viewModel.registerResponse.observe(viewLifecycleOwner) { response ->
            when(response) {
                is NetworkResponse.Failure -> {
                    if (::dialog.isInitialized)
                        dialog.dismissDialog()

                    context?.showErrorDialog(response.errorMessage)
                }
                NetworkResponse.Loading -> {
                    if (::dialog.isInitialized)
                        dialog.showLoadingDialog()
                }
                is NetworkResponse.Success -> {
                    if (::dialog.isInitialized)
                        dialog.dismissDialog()

                    //TODO Success dialog, on click popBack
                }
            }
        }
    }

    private fun validate(): Boolean {
        binding.apply {
            mailTextLayout.error = null
            usernameTextLayout.error = null
            passwordTextLayout.error = null

            val email = mailET.text?.toString()
            val username = usernameET.text?.toString()
            val password = passwordET.text?.toString()
            val passwordAgain = rePasswordET.text?.toString()

            //TODO Extract string
            if (email?.isEmptyOrBlank() == true) {
                mailTextLayout.error = getString(R.string.please_enter_an_email)
                return false
            } else if (email?.isEmailValid() == false) {
                mailTextLayout.error = getString(R.string.invalid_email_address)
                return false
            } else if (username?.isEmptyOrBlank() == true) {
                usernameTextLayout.error = getString(R.string.please_fill_the_form)
                return false
            } else if (password != null && password.length < 6) {
                passwordTextLayout.error = getString(R.string.password_too_short)
                return false
            } else if (password != null && passwordAgain != null && password != passwordAgain) {
                passwordTextLayout.error = getString(R.string.passwords_dont_match)
                return false
            }
        }

        return true
    }

    override fun onDestroyView() {
        viewModel.registerResponse.removeObservers(viewLifecycleOwner)
        profileImageAdapter = null

        super.onDestroyView()
    }
}