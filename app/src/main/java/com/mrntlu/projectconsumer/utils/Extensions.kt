package com.mrntlu.projectconsumer.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

const val DEFAULT_JUMP_THRESHOLD = 20
const val DEFAULT_SPEED_FACTOR = 1f
const val DATE_PATTERN = "yyyy-MM-dd"
const val DATE_READABLE_PATTERN = "dd.MM.yyyy"
const val DATE_ALT_READABLE_PATTERN = "dd MMM yy"

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun LocalDate.getFirstDateOfTheWeek(): LocalDate? {
    var currentDate = this

    val oneWeekAgo = currentDate.minusWeeks(1)
    while (currentDate.isAfter(oneWeekAgo)) {
        if (currentDate.dayOfWeek == DayOfWeek.MONDAY)
            return currentDate

        currentDate = currentDate.minusDays(1)
    }

    return null
}

fun Date.convertLongDateToAgoString(): String {
    val timeElapsed = Date().time - time
    val oneMin = 60000L
    val oneHour = 3600000L
    val oneDay = 86400000L
    val oneWeek = 604800000L
    val oneMonth = 30 * oneDay

    val unit: String
    val finalString = if (timeElapsed < oneMin) {
        var seconds = (timeElapsed / 1000).toDouble()
        seconds = seconds.roundToInt().toDouble()
        if (seconds < 30) {
            "Now"
        } else {
            unit = " secs ago"
            seconds.toInt().toString() + unit
        }
    } else if (timeElapsed < oneHour) {
        val minutes = (timeElapsed / oneMin).toDouble()
        handleAgoString(minutes, " min")
    } else if (timeElapsed < oneDay) {
        val hours = (timeElapsed / oneHour).toDouble()
        handleAgoString(hours, " hr")
    } else if (timeElapsed < oneWeek) {
        val days = (timeElapsed / oneDay).toDouble()
        handleAgoString(days, " day")
    } else if (timeElapsed < oneMonth) {
        val weeks = (timeElapsed / oneWeek).toDouble()
        handleAgoString(weeks, " week")
    } else {
        val months = (timeElapsed / oneMonth).toDouble()
        handleAgoString(months, " month")
    }
    return finalString
}

fun String.convertShortDateToAgoString(): String {
    val date = convertToFormattedDate()
    if (date != null) {
        val timeElapsed = Date().time - date.time
        val oneMin = 60000L
        val oneHour = 3600000L
        val oneDay = 86400000L
        val oneWeek = 604800000L
        val oneMonth = 30 * oneDay

        val unit: String
        val finalString = if (timeElapsed < oneMin) {
            var seconds = (timeElapsed / 1000).toDouble()
            seconds = seconds.roundToInt().toDouble()
            if (seconds < 30) {
                "Now"
            } else {
                unit = " secs ago"
                seconds.toInt().toString() + unit
            }
        } else if (timeElapsed < oneHour) {
            val minutes = (timeElapsed / oneMin).toDouble()
            handleAgoString(minutes, " min")
        } else if (timeElapsed < oneDay) {
            val hours = (timeElapsed / oneHour).toDouble()
            handleAgoString(hours, " hr")
        } else if (timeElapsed < oneWeek) {
            val days = (timeElapsed / oneDay).toDouble()
            handleAgoString(days, " day")
        } else if (timeElapsed < oneMonth) {
            val weeks = (timeElapsed / oneWeek).toDouble()
            handleAgoString(weeks, " week")
        } else {
            convertToHumanReadableDateString(isAlt = true)
        }

        return finalString ?: this
    } else
        return this
}

private fun handleAgoString(ago: Double, agoString: String): String {
    var agoStr = agoString
    val agoInt = ago.roundToInt()
    if (agoInt != 1) {
        agoStr = agoString + "s"
    }
    return "$agoInt$agoStr ago"
}

fun LocalDate.convertToHumanReadableDateString(): String {
    val formatter = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
    return formatter.format(Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant()))
}

fun Context.openInBrowser(url: String) {
    try {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(this, Uri.parse(url))
    } catch (_: Exception) {
        if (URLUtil.isValidUrl(url)) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val clip = ClipData.newUri(contentResolver, "URI", Uri.parse(url))
            clipboard.setPrimaryClip(clip)

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, getString(R.string.browser_not_available), Toast.LENGTH_SHORT).show()
        }
    }
}

fun Context.dpToPx(dp: Float): Int {
    val displayMetrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics).toInt()
}

fun Context.dpToPxFloat(dp: Float): Float {
    val displayMetrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
}

fun Context.getColorFromAttr(color: Int): Int {
    val typedValue = TypedValue()

    theme.resolveAttribute(color, typedValue, true)
    return ContextCompat.getColor(this, typedValue.resourceId)
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.showConfirmationDialog(message: String, onPositive: () -> Unit): AlertDialog {
    return MaterialAlertDialogBuilder(this, R.style.Theme_ProjectConsumer_InfoMaterialAlertDialog)
        .setTitle(getString(R.string.confirm))
        .setMessage(message)
        .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            onPositive()
            dialog.dismiss()
        }
        .setNegativeButton(getString(R.string.no_)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun Context.showNotificationInfoDialog(message: String, onPositive: () -> Unit, onNegative: () -> Unit): AlertDialog {
    return MaterialAlertDialogBuilder(this, R.style.Theme_ProjectConsumer_InfoMaterialAlertDialog)
        .setTitle(getString(R.string.info))
        .setMessage(message)
        .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            onPositive()
            dialog.dismiss()
        }
        .setNegativeButton(getString(R.string.no_thanks_)) { dialog, _ ->
            onNegative()
            dialog.dismiss()
        }
        .show()
}

fun Context.showSuccessDialog(message: String, onPositive: () -> Unit) {
    MaterialAlertDialogBuilder(this, R.style.Theme_ProjectConsumer_InfoMaterialAlertDialog)
        .setTitle(getString(R.string.success))
        .setMessage(message)
        .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            onPositive()
            dialog.dismiss()
        }
        .show()
}

fun Context.showInfoDialog(message: String) {
    MaterialAlertDialogBuilder(this, R.style.Theme_ProjectConsumer_InfoMaterialAlertDialog)
        .setTitle(getString(R.string.info))
        .setMessage(message)
        .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
            dialog.dismiss()
        }
        .show()
}

fun Context.showLoginRegisterDialog(isConsumeLater: Boolean, onPositive: () -> Unit) {
    MaterialAlertDialogBuilder(this)
        .setTitle(getString(R.string.error))
        .setMessage("Unauthorized access. You need to sign in to add this to your ${if (isConsumeLater) "watch later queue" else "list"}.")
        .setPositiveButton(getString(R.string.sign_in)) { dialog, _ ->
            onPositive()
            dialog.dismiss()
        }
        .setNegativeButton(R.string.dismiss) { dialog, _ ->
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

fun Context.setNotification(
    channelId: String,
    title: String?,
    body: String?,
    soundUri: Uri?,
    groupId: String?,
    pendingIntent: PendingIntent,
): NotificationCompat.Builder {
    val notification = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.ic_launcher_logo)
        .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_logo))
        .setColor(ContextCompat.getColor(applicationContext, R.color.materialBlack))
        .setContentTitle(title)
        .setContentText(body)
        .setAutoCancel(true)
        .setSound(soundUri)
        .setGroupSummary(false)

    if (groupId != null)
        notification.setGroup(groupId)

    notification.setContentIntent(pendingIntent)

    return notification
}

fun Context.setGroupNotification(
    channelId: String,
    groupId: String,
    groupSummary: Boolean,
    lineText: String,
    bigContentTitle: String,
    summaryText: String,
): Notification = NotificationCompat.Builder(this, channelId)
    .setSmallIcon(R.drawable.ic_launcher_logo)
    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_logo))
    .setColor(ContextCompat.getColor(applicationContext, R.color.materialBlack))
    .setStyle(
        NotificationCompat.InboxStyle()
            .addLine(lineText)
            .setBigContentTitle(bigContentTitle)
            .setSummaryText(summaryText)
    )
    .setGroup(groupId)
    .setGroupSummary(groupSummary)
    .setAutoCancel(true)
    .build()

fun String.capitalizeFirstLetter(): String {
    return this.replaceFirstChar { it.uppercase() }
}

fun String.convertToHumanReadableDateString(isAlt: Boolean = false): String? {
    val date = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).parse(this)
    return if(date != null) SimpleDateFormat(if (isAlt) DATE_ALT_READABLE_PATTERN else DATE_READABLE_PATTERN, Locale.getDefault()).format(date) else this
}

fun String.convertToDateString(): String? {
    val date = SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).parse(this)
    return if(date != null) SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(date) else this
}

fun String.convertToDate(): Date? {
    return SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).parse(this)
}

fun String.convertToFormattedTime(): String? {
    val date: Date? = try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        dateFormat.parse(this)
    } catch (_: Exception) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        dateFormat.parse(this)
    }
    return if(date != null) SimpleDateFormat("HH:mm z", Locale.getDefault()).format(date) else this
}

fun String.convertToFormattedDate(): Date? {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        dateFormat.parse(this)
    } catch (_: Exception) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        dateFormat.parse(this)
    }
}

fun String.isEmailValid(): Boolean {
    val emailRegex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
    return matches(emailRegex)
}

fun String.isEmptyOrBlank() = this.isEmpty() || this.isBlank()

fun String.isNotEmptyOrBlank(): Boolean {
    return this.trim().isNotEmpty() && this.trim().isNotBlank()
}

fun View.sendHapticFeedback() {
    performHapticFeedback(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            HapticFeedbackConstants.CONFIRM
        else
            HapticFeedbackConstants.KEYBOARD_TAP
    )
}

fun View.setVisibilityByConditionWithAnimation(shouldHide: Boolean) {
    if (shouldHide) {
        animate()
            .alpha(0f)
            .setDuration(250)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                }
            })
    } else {
        visibility = View.VISIBLE
        alpha = 0f
        animate()
            .alpha(1f)
            .setDuration(250)
            .setListener(null)
    }
}

fun View.setVisibilityByCondition(shouldHide: Boolean) {
    this.visibility = if (shouldHide) View.GONE else View.VISIBLE
}

fun View.setGone() {
    this.visibility = View.GONE
}

fun View.setVisible() {
    this.visibility = View.VISIBLE
}

fun Double.roundOffDecimal(): Double {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.CEILING

    val formattedStr = df.format(this)
    val numberFormat = NumberFormat.getInstance(Locale.getDefault())

    return numberFormat.parse(formattedStr)?.toDouble() ?: this
}

fun Double.roundSingleDecimal(): Double {
    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.CEILING

    val formattedStr = df.format(this)
    val numberFormat = NumberFormat.getInstance(Locale.getDefault())

    return numberFormat.parse(formattedStr)?.toDouble() ?: this
}

fun ImageView.loadWithGlide(imageUrl: String, placeHolder: View?, progressBar: View, sizeMultiplier: Float = 1f, transformImage: RequestBuilder<Drawable>.() -> RequestBuilder<Drawable>) =
    Glide.with(context).load(imageUrl).addListener(object: RequestListener<Drawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
            progressBar.setGone()
            placeHolder?.setVisible()
            return false
        }

        override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
            progressBar.setGone()
            placeHolder?.setGone()
            return false
        }
    }).sizeMultiplier(sizeMultiplier).transformImage().into(this)

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

//Credits: https://medium.com/holler-developers/paging-image-gallery-with-recyclerview-f059d035b7e7
fun RecyclerView.smoothScrollToCenteredPosition(position: Int) {
    val smoothScroller = object : LinearSmoothScroller(context) {

        override fun calculateDxToMakeVisible(view: View?,
                                              snapPref: Int): Int {
            val dxToStart = super.calculateDxToMakeVisible(view, SNAP_TO_START)
            val dxToEnd = super.calculateDxToMakeVisible(view, SNAP_TO_END)

            return (dxToStart + dxToEnd) / 2
        }
    }

    smoothScroller.targetPosition = position
    layoutManager?.startSmoothScroll(smoothScroller)
}