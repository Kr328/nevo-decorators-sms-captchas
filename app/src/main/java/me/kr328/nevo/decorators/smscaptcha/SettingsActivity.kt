package me.kr328.nevo.decorators.smscaptcha

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import me.kr328.nevo.decorators.smscaptcha.utils.PackageUtils.hasPackageInstalled
import kotlin.concurrent.thread

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWindowOreo()

        thread {
            detectNevolution()
        }
    }

    private fun setupWindowOreo() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val decorView = window.decorView
        decorView.systemUiVisibility =
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        window.navigationBarColor =
                resources.getColor(R.color.colorPrimary, theme)
    }

    private fun detectNevolution() {
        if (!hasPackageInstalled(this, Constants.NEVOLUTION_PACKAGE_NAME)) {
            runOnUiThread {
                AlertDialog.Builder(this)
                        .setTitle(R.string.missing_nevolution_dialog_title)
                        .setMessage(R.string.missing_nevolution_dialog_message)
                        .setOnDismissListener { finish() }
                        .setPositiveButton(R.string.missing_nevolution_dialog_button) { _: DialogInterface?, _: Int ->
                            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + Constants.NEVOLUTION_PACKAGE_NAME)))
                        }
                        .show()
            }
        } else {
            runOnUiThread {
                setContentView(R.layout.main_layout)
            }
        }
    }
}