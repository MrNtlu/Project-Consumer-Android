package com.mrntlu.projectconsumer.ui

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.mrntlu.projectconsumer.R

class LoadingDialog(
    private val activity: Activity
) {

    lateinit var dialog: AlertDialog

    fun showLoadingDialog() {
        val builder = AlertDialog.Builder(activity, R.style.WrapContentDialog)

        builder.apply {
            setView(activity.layoutInflater.inflate(R.layout.layout_loading_dialog, null))
            setCancelable(false)
            dialog = create()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams().apply {
            copyFrom(dialog.window?.attributes)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        dialog.window?.attributes = layoutParams
        dialog.show()
    }

    fun dismissDialog() {
        dialog.dismiss()
    }
}