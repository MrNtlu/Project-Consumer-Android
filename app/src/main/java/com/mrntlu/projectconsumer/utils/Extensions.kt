package com.mrntlu.projectconsumer.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mrntlu.projectconsumer.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

const val DEFAULT_JUMP_THRESHOLD = 20
const val DEFAULT_SPEED_FACTOR = 1f

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.showInfoDialog(message: String, onPositive: () -> Unit) {
    MaterialAlertDialogBuilder(this, R.style.Theme_ProjectConsumer_InfoMaterialAlertDialog)
        .setTitle(getString(R.string.info))
        .setMessage(message)
        .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            onPositive()
            dialog.dismiss()
        }
        .setNegativeButton(getString(R.string.no_thanks_)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun Context.showErrorDialog(message: String) {
    MaterialAlertDialogBuilder(this)
        .setTitle(getString(R.string.error))
        .setMessage(message)
        .setPositiveButton(resources.getString(R.string.dismiss)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun String.convertToFormattedDate(): String? {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this)
    return if(date != null) SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date) else this
}

fun String.isEmailValid(): Boolean {
    val emailRegex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
    return matches(emailRegex)
}

fun String.isEmptyOrBlank() = this.isEmpty() || this.isBlank()

fun String.isNotEmptyOrBlank(): Boolean{
    return this.trim().isNotEmpty() && this.trim().isNotBlank()
}

fun View.setVisibilityByCondition(shouldHide: Boolean){
    this.visibility = if (shouldHide) View.GONE else View.VISIBLE
}

fun View.setGone(){
    this.visibility = View.GONE
}

fun View.setVisible(){
    this.visibility = View.VISIBLE
}

fun Double.roundOffDecimal(): Double {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this).toDouble()
}

fun Double.roundSingleDecimal(): Double {
    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this).toDouble()
}

fun ImageView.loadWithGlide(imageUrl: String, placeHolder: View?, progressBar: ProgressBar, transformImage: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable>) =
    Glide.with(context).load(imageUrl).addListener(object: RequestListener<Drawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            progressBar.setGone()
            placeHolder?.setVisible()
            return false
        }

        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            progressBar.setGone()
            placeHolder?.setGone()
            return false
        }
    }).transformImage().into(this)

fun<Response> ViewModel.networkResponseFlowCollector(
    request: Flow<NetworkResponse<Response>>,
    collect: (NetworkResponse<Response>) -> Unit,
) = viewModelScope.launch(Dispatchers.IO) {
    request.collect { response ->
        withContext(Dispatchers.Main) {
            collect(response)
        }
    }
}

suspend fun RecyclerView.quickScrollToTop(
    jumpThreshold: Int = DEFAULT_JUMP_THRESHOLD,
    speedFactor: Float = DEFAULT_SPEED_FACTOR
) {
    val layoutManager = layoutManager as LinearLayoutManager

    val smoothScroller = object : LinearSmoothScroller(context) {
        init {
            targetPosition = 0
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?) =
            super.calculateSpeedPerPixel(displayMetrics) / speedFactor
    }

    val jumpBeforeScroll = layoutManager.findFirstVisibleItemPosition() > jumpThreshold
    if (jumpBeforeScroll) {
        layoutManager.scrollToPositionWithOffset(jumpThreshold, 0)
        awaitFrame()
    }

    layoutManager.startSmoothScroll(smoothScroller)
}