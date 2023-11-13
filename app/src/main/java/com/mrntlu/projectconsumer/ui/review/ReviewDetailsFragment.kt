package com.mrntlu.projectconsumer.ui.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.adapters.LikeUserStackAdapter
import com.mrntlu.projectconsumer.adapters.decorations.OverlapDecoration
import com.mrntlu.projectconsumer.databinding.FragmentReviewDetailsBinding
import com.mrntlu.projectconsumer.models.auth.BasicUserInfo
import com.mrntlu.projectconsumer.models.common.retrofit.IDBody
import com.mrntlu.projectconsumer.models.main.review.Author
import com.mrntlu.projectconsumer.models.main.review.ReviewDetails
import com.mrntlu.projectconsumer.ui.BaseFragment
import com.mrntlu.projectconsumer.ui.dialog.LoadingDialog
import com.mrntlu.projectconsumer.utils.Constants.ContentType
import com.mrntlu.projectconsumer.utils.Constants.ProfileImageList
import com.mrntlu.projectconsumer.utils.NetworkResponse
import com.mrntlu.projectconsumer.utils.convertToHumanReadableDateString
import com.mrntlu.projectconsumer.utils.dpToPxFloat
import com.mrntlu.projectconsumer.utils.getColorFromAttr
import com.mrntlu.projectconsumer.utils.loadWithGlide
import com.mrntlu.projectconsumer.utils.sendHapticFeedback
import com.mrntlu.projectconsumer.utils.setGone
import com.mrntlu.projectconsumer.utils.setSafeOnClickListener
import com.mrntlu.projectconsumer.utils.setVisibilityByCondition
import com.mrntlu.projectconsumer.utils.setVisible
import com.mrntlu.projectconsumer.utils.showConfirmationDialog
import com.mrntlu.projectconsumer.utils.showErrorDialog
import com.mrntlu.projectconsumer.utils.showSuccessDialog
import com.mrntlu.projectconsumer.viewmodels.main.review.ReviewDetailsViewModel
import com.mrntlu.projectconsumer.viewmodels.shared.UserSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ReviewDetailsFragment : BaseFragment<FragmentReviewDetailsBinding>() {

    private val userSharedViewModel: UserSharedViewModel by activityViewModels()
    private val viewModel: ReviewDetailsViewModel by viewModels()
    private val args: ReviewDetailsFragmentArgs by navArgs()

    private var confirmDialog: AlertDialog? = null
    private lateinit var dialog: LoadingDialog
    private var userStackAdapter: LikeUserStackAdapter? = null

    private var reviewDetails: ReviewDetails? = null
    private var basicUserInfo: BasicUserInfo? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            dialog = LoadingDialog(it)
        }

        setObservers()
    }

    private fun setObservers() {
        if (!(viewModel.reviewDetails.hasObservers() || viewModel.reviewDetails.value is NetworkResponse.Success || viewModel.reviewDetails.value is NetworkResponse.Loading))
            viewModel.getReviewDetails(args.reviewId)

        viewModel.reviewDetails.observe(viewLifecycleOwner) { response ->
            binding.apply {
                if (response is NetworkResponse.Loading)
                    loadingLayout.setVisible()
                else if (response is NetworkResponse.Failure)
                    loadingLayout.setGone()

                errorLayout.setVisibilityByCondition(response !is NetworkResponse.Failure)
                when(response) {
                    is NetworkResponse.Failure -> {
                        errorLayoutInc.apply {
                            cancelButton.setGone()

                            errorText.text = response.errorMessage

                            setListeners()
                        }
                    }
                    is NetworkResponse.Success -> {
                        reviewDetails = response.data.data

                        viewModel.viewModelScope.launch {
                            setUI()
                            setToolbar()
                            setListeners()
                            setRecyclerView(reviewDetails!!.likes)
                            loadingLayout.setGone()
                        }
                    }
                    else -> {}
                }
            }
        }

        userSharedViewModel.userInfoResponse.observe(viewLifecycleOwner) { response ->
            if (response is NetworkResponse.Success)
                basicUserInfo = response.data.data
        }
    }

    private fun setToolbar() {
        binding.reviewDetailsToolbar.apply {
            title = reviewDetails?.content?.titleEn
            subtitle = getString(R.string.review)

            setNavigationOnClickListener { navController.popBackStack() }
        }
    }

    private suspend fun setUI() = withContext(Dispatchers.Default) {
        reviewDetails?.apply {
            val authorStr = author.username
            val timeStr = createdAt.convertToHumanReadableDateString(true) ?: createdAt
            val strokeColor = ContextCompat.getColor(
                binding.root.context,
                if (isAuthor) R.color.blue500 else android.R.color.transparent
            )
            withContext(Dispatchers.Main) {
                binding.authorImage.loadWithGlide(author.image, null, binding.authorProgressBar, 0.9f) {
                    transform(CenterCrop())
                }
                binding.authorTV.text = authorStr
                binding.timeTV.text = timeStr
                binding.reviewRateTV.text = star.toString()
                binding.reviewTV.text = review

                binding.authorCV.strokeColor = strokeColor
                binding.authorTV.setTextColor(
                    if (isAuthor)
                        ContextCompat.getColor(binding.root.context, R.color.blue500)
                    else
                        binding.root.context.getColorFromAttr(R.attr.mainTextColor)
                )
                binding.actionLayout.setVisibilityByCondition(!isAuthor)
                binding.premiumAnimation.setVisibilityByCondition(!author.isPremium)

                handleLikeButton()
                binding.likeButton.isEnabled = sharedViewModel.isLoggedIn() && !isAuthor
            }
        }

        if (reviewDetails != null) {
            binding.imageInclude.apply {
                val radiusInPx = root.context.dpToPxFloat(8f)
                val isRatioDifferent = reviewDetails!!.contentType == ContentType.GAME.request
                val sizeMultiplier = 0.7f
                val sizeMultipliedRadiusInPx = (radiusInPx * sizeMultiplier).toInt()

                withContext(Dispatchers.Main) {
                    previewCard.setGone()
                    previewShimmerLayout.setVisible()
                    previewShimmerCV.radius = radiusInPx
                    previewTV.setGone()

                    previewIV.scaleType = if (isRatioDifferent)
                        ImageView.ScaleType.CENTER_CROP
                    else
                        ImageView.ScaleType.FIT_XY

                    previewIV.loadWithGlide(reviewDetails!!.content.imageURL, previewCard, previewShimmerLayout, sizeMultiplier) {
                        if (isRatioDifferent)
                            transform(
                                CenterCrop(), RoundedCorners(sizeMultipliedRadiusInPx)
                            )
                        else
                            transform(RoundedCorners(sizeMultipliedRadiusInPx))
                    }

                    previewCard.radius = radiusInPx
                    previewIV.contentDescription = reviewDetails!!.content.titleEn
                }
            }
        }
    }

    private fun handleLikeButton() {
        reviewDetails?.apply {
            val likeImageSource = if (isLiked) R.drawable.ic_like else R.drawable.ic_like_outline
            binding.likeButton.setImageResource(likeImageSource)

            binding.popularityTV.text = popularity.toString()
        }
    }

    private fun setListeners() {
        binding.apply {
            likeButton.setSafeOnClickListener {
                root.sendHapticFeedback()

                val voteLiveData = viewModel.voteReview(IDBody(reviewDetails!!.id))

                voteLiveData.observe(viewLifecycleOwner) { response ->
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

                            reviewDetails?.isLiked = response.data.data.isLiked
                            reviewDetails?.popularity = response.data.data.popularity
                            handleLikeButton()

                            if (basicUserInfo != null && reviewDetails != null) {
                                viewModel.viewModelScope.launch {
                                    val likeList = reviewDetails!!.likes
                                    if (response.data.data.isLiked) {
                                        likeList.add(Author(
                                            basicUserInfo!!.image ?: ProfileImageList[0],
                                            basicUserInfo!!.username,
                                            basicUserInfo!!.email,
                                            "temp_id_holder",
                                            basicUserInfo!!.isPremium
                                        ))
                                    } else {
                                        likeList.removeIf {
                                            it.username == basicUserInfo!!.username &&
                                            it.email == basicUserInfo!!.email
                                        }
                                    }
                                    userStackAdapter?.handleOperation(likeList)
                                }
                            }
                        }
                    }
                }
            }

            errorLayoutInc.refreshButton.setSafeOnClickListener {
                viewModel.getReviewDetails(args.reviewId)
            }

            deleteButton.setSafeOnClickListener {
                root.sendHapticFeedback()

                if (confirmDialog != null && confirmDialog?.isShowing == true) {
                    confirmDialog?.dismiss()
                    confirmDialog = null
                }

                confirmDialog = context?.showConfirmationDialog(getString(R.string.do_you_want_to_delete)) {
                    val deleteReviewLiveData = viewModel.deleteReview(IDBody(reviewDetails!!.id))

                    deleteReviewLiveData.observe(viewLifecycleOwner) { response ->
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

                                navController.popBackStack()
                                context?.showSuccessDialog(getString(R.string.successfully) + " " + getString(R.string.deleted)) {}
                            }
                        }
                    }
                }
            }

            authorCV.setSafeOnClickListener {
                if (navController.currentDestination?.id == R.id.reviewDetailsFragment) {
                    val navWithAction = ReviewDetailsFragmentDirections.actionReviewDetailsFragmentToProfileDisplayFragment(
                        reviewDetails!!.author.username
                    )
                    navController.navigate(navWithAction)
                }
            }

            authorTV.setSafeOnClickListener {
                if (navController.currentDestination?.id == R.id.reviewDetailsFragment) {
                    val navWithAction = ReviewDetailsFragmentDirections.actionReviewDetailsFragmentToProfileDisplayFragment(
                        reviewDetails!!.author.username
                    )
                    navController.navigate(navWithAction)
                }
            }

            reviewDetailsToolbar.setSafeOnClickListener {
                if (navController.currentDestination?.id == R.id.reviewDetailsFragment) {
                    val contentId = reviewDetails!!.contentID

                    val navWithAction = when(ContentType.fromStringRequest(reviewDetails!!.contentType)) {
                        ContentType.ANIME -> ReviewDetailsFragmentDirections.actionReviewDetailsFragmentToAnimeDetailsFragment(contentId)
                        ContentType.MOVIE -> ReviewDetailsFragmentDirections.actionReviewDetailsFragmentToMovieDetailsFragment(contentId)
                        ContentType.TV -> ReviewDetailsFragmentDirections.actionReviewDetailsFragmentToTvDetailsFragment(contentId)
                        ContentType.GAME -> ReviewDetailsFragmentDirections.actionReviewDetailsFragmentToGameDetailsFragment(contentId)
                    }

                    navController.navigate(navWithAction)
                }
            }
        }
    }

    private fun setRecyclerView(authorList: List<Author>) {
        binding.likeUserRV.apply {
            val linearLayoutManager = object : LinearLayoutManager(context, HORIZONTAL, false) {
                override fun canScrollHorizontally(): Boolean {
                    return false
                }
            }
            addItemDecoration(OverlapDecoration())
            layoutManager = linearLayoutManager

            userStackAdapter = LikeUserStackAdapter(authorList.toCollection(ArrayList()))
            adapter = userStackAdapter
        }
    }

    override fun onDestroyView() {
        viewLifecycleOwner.apply {
            viewModel.reviewDetails.removeObservers(this)
            userSharedViewModel.userInfoResponse.removeObservers(this)
        }

        userStackAdapter = null
        confirmDialog?.dismiss()
        confirmDialog = null
        super.onDestroyView()
    }
}