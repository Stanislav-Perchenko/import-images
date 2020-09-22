package com.alperez.demo.importimages

import android.os.Bundle

/**
 * Created by stanislav.perchenko on 22.09.2020 at 19:49.
 */
class MainActivity : BaseActivity() {

    override fun getToolbarResId(): Int? = R.id.toolbar

    override fun getScreenTitle(): String = resources.getString(R.string.app_name)

    override fun getScreenSubTitle(): String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar(false)
    }


}