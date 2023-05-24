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
import com.mrntlu.projectconsumer.databinding.FragmentAuthBinding
import com.mrntlu.projectconsumer.models.auth.retrofit.GoogleLoginBody
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.viewmodels.auth.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthFragment : BaseFragment<FragmentAuthBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient

    //TODO Add isLogged in and fail/loading dialog
    // Implement fcm token
    // https://firebase.google.com/docs/cloud-messaging/android/client
    // https://github.com/MrNtlu/Notification-Guide/tree/main/app/src/main/java/com/mrntlu/notificationguide
    // Implement token manager
    // https://github.com/MrNtlu/Token-Authentication/blob/master/app/src/main/java/com/mrntlu/tokenauthentication/utils/TokenManager.kt

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // handle the response in result.data
            printLog("Success ${result.data}")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)

                printLog("Account ${account.email}\n${account.idToken}\n${account.id}")
                if (account.idToken != null) {
                    viewModel.googleLogin(
                        GoogleLoginBody(account.idToken!!, "empty_token_key")
                    )
                }
            } catch (exception: ApiException) {
                printLog("Exception ${exception.status} ${exception.statusCode}")
            }
        } else {
            printLog("Failed $result")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUI()
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

    private fun setUI() {
        binding.apply {

        }
    }

    private fun setListeners() {
        binding.apply {
            googleSignInButton.setOnClickListener {
                val signInIntent: Intent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            }

            signInButton.setOnClickListener {
                //TODO Signin
            }

            signUpButton.setOnClickListener {
                //TODO Signup
                googleSignInClient.signOut()
            }

            forgotPasswordButton.setOnClickListener {
                //TODO ForgotPassword
            }
        }
    }

    private fun setObservers() {
        viewModel.loginResponse.observe(viewLifecycleOwner) { response ->
            printLog("Response $response")
        }
    }

    override fun onDestroyView() {
        viewModel.loginResponse.removeObservers(viewLifecycleOwner)

        super.onDestroyView()
    }
}