package com.alperez.importimages.util

import android.app.AlertDialog
import android.content.Context
import android.view.ContextThemeWrapper
import com.alperez.demo.importimages.R

/**
 * Created by stanislav.perchenko on 22.09.2020 at 20:58.
 */

public fun newAlertDialogBuilder(c: Context): AlertDialog.Builder =
    AlertDialog.Builder(ContextThemeWrapper(c, R.style.AllDialogs))