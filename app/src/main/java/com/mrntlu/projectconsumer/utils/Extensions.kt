package com.mrntlu.projectconsumer.utils

import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

const val DEFAULT_JUMP_THRESHOLD = 20
const val DEFAULT_SPEED_FACTOR = 1f

fun String.convertToFormattedDate(): String? {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(this)
    return if(date != null) SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date) else this
}

fun String.isNotEmptyOrBlank(): Boolean{
    return this.trim().isNotEmpty() && this.trim().isNotBlank()
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

fun<T> networkResponseFlow(call: suspend () -> Response<T>): Flow<NetworkResponse<T>> = flow {
    emit(NetworkResponse.Loading)

    try {
        val response = call()

        if (response.isSuccessful) {
            response.body()?.let { data ->
                emit(NetworkResponse.Success(data))
            }
        } else {
            emit(NetworkResponse.Failure(errorMessage = response.message()))
        }
    } catch (e: Exception) {
        emit(NetworkResponse.Failure(e.message ?: e.toString()))
    }
}.flowOn(Dispatchers.IO)

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