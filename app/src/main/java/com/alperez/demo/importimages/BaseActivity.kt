package com.alperez.demo.importimages

import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.alperez.demo.importimages.utils.ViewUtils

/**
 * Created by stanislav.perchenko on 22.09.2020 at 20:09.
 */
abstract class BaseActivity : AppCompatActivity() {
    protected abstract fun getToolbarResId(): Int?
    protected abstract fun getScreenTitle(): String?
    protected abstract fun getScreenSubTitle(): String?

    protected open fun setupToolbar(useUpButton: Boolean): Boolean {
        val resId = getToolbarResId()
        return if (resId != null) {
            val vTb = findViewById<Toolbar>(resId.toInt())
            vTb.setTitleTextColor(ViewUtils.getColorFromResourcesCompat(resources, R.color.text_white, null))
            setSupportActionBar(vTb)
            val ab: ActionBar = supportActionBar ?: throw RuntimeException()
            ab.title = getScreenTitle() ?: ""
            ab.subtitle = getScreenSubTitle()
            ab.setHomeButtonEnabled(useUpButton)
            ab.setDisplayHomeAsUpEnabled(useUpButton)
            true
        } else false
    }
}