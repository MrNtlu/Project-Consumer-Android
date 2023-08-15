package com.mrntlu.projectconsumer.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.databinding.FragmentAuthBinding
import com.mrntlu.projectconsumer.models.auth.retrofit.GoogleLoginBody
import com.mrntlu.projectconsumer.models.auth.retrofit.LoginBody
import com.mrntlu.projectconsumer.service.TokenManager
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.isEmailValid
import com.mrntlu.projectconsumer.utils.isEmptyOrBlank
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.auth.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment : BaseFragment<FragmentAuthBinding>() {

    private val viewModel: LoginViewModel by viewModels()
    @Inject lateinit var tokenManager: TokenManager

    private var coroutineScope = CoroutineScope(Dispatchers.IO)
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var fcmToken: String
    private lateinit var dialog: LoadingDialog

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)

                if (account.idToken != null) {
                    viewModel.googleLogin(
                        GoogleLoginBody(account.idToken!!, Constants.ProfileImageList[(0..Constants.ProfileImageList.size).random()], fcmToken)
                    )
                }
            } catch (exception: ApiException) {
                context?.showErrorDialog(exception.message ?: exception.toString())
            }
        } else {
            context?.showErrorDialog("Failed to login! ${result.resultCode}")
            printLog("Failed $result")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            dialog = LoadingDialog(it)
        }

        if (!coroutineScope.isActive)
            coroutineScope = CoroutineScope(Dispatchers.IO)

        getFCMToken()
        setGoogleSignIn()
        setListeners()
        setObservers()
    }

    private fun setGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestIdToken(Constants.GOOGLE_CLIENT_ID)
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
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

    private fun setListeners() {
        binding.apply {
            googleSignInButton.setOnClickListener {
                val signInIntent: Intent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }

            signInButton.setOnClickListener {
                if (validate()) {
                    val body = LoginBody(
                        binding.mailET.text!!.toString(),
                        binding.passwordET.text!!.toString(),
                    )

                    viewModel.login(body)
                }
            }

            signUpButton.setOnClickListener {
                navController.navigate(R.id.action_authFragment_to_registerFragment)
            }

            forgotPasswordButton.setOnClickListener {
                activity?.let {
                    val bottomSheet = ForgotPasswordBottomSheet()
                    bottomSheet.show(it.supportFragmentManager, ForgotPasswordBottomSheet.TAG)
                }
            }
        }
    }

    private fun setObservers() {
        viewModel.loginResponse.observe(viewLifecycleOwner) { response ->
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

                    coroutineScope.launch {
                        tokenManager.saveToken(response.data.token)

                        withContext(Dispatchers.Main) {
                            sharedViewModel.setAuthentication(true)
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun validate(): Boolean {
        binding.apply {
            mailTextLayout.error = null
            passwordTextLayout.error = null

            val email = mailET.text?.toString()
            val password = passwordET.text?.toString()

            if (email?.isEmptyOrBlank() == true) {
                mailTextLayout.error = getString(R.string.please_enter_an_email)
                return false
            } else if (email?.isEmailValid() == false) {
                mailTextLayout.error = getString(R.string.invalid_email_address)
                return false
            } else if (password != null && password.length < 6) {
                passwordTextLayout.error = getString(R.string.password_too_short)
                return false
            }
        }

        return true
    }

    override fun onDestroyView() {
        viewModel.loginResponse.removeObservers(viewLifecycleOwner)
        coroutineScope.cancel()

        super.onDestroyView()
    }
}