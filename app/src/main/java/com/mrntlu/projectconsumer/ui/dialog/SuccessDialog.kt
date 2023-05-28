package com.mrntlu.projectconsumer.ui.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.mrntlu.projectconsumer.R

class SuccessDialog(
    private val activity: Activity,
) {
    lateinit var dialog: AlertDialog

    fun showDialog(message: String, onClick: () -> Unit) {
        val builder = AlertDialog.Builder(activity, R.style.WrapContentDialog)
        val view = activity.layoutInflater.inflate(R.layout.layout_success_dialog, null)

        builder.apply {
            setView(view)
            setCancelable(false)
            dialog = create()
        }

        view.findViewById<TextView>(R.id.successTV).text = message
        view.findViewById<Button>(R.id.okButton).setOnClickListener {
            onClick()
            dialog.dismiss()
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
        if (::dialog.isInitialized)
            dialog.dismiss()
    }
}