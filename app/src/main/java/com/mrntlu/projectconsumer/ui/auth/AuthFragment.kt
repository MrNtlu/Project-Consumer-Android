package com.mrntlu.projectconsumer.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
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
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.printLog
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.viewmodels.auth.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthFragment : BaseFragment<FragmentAuthBinding>() {

    private val viewModel: LoginViewModel by viewModels()

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
                        GoogleLoginBody(account.idToken!!, fcmToken)
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

        getFCMToken()
        setMenu()
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

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                fcmToken = "unknown_fcm_token"
                return@OnCompleteListener
            }

            fcmToken = task.result
        })
    }

    private fun setMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                menu.removeItem(R.id.settingsMenu)
            }
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}
            override fun onMenuItemSelected(menuItem: MenuItem) = true
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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

                    sharedViewModel.setAuthentication(true)
                    navController.popBackStack()
                }
            }
        }
    }

    override fun onDestroyView() {
        viewModel.loginResponse.removeObservers(viewLifecycleOwner)

        super.onDestroyView()
    }
}