package com.alperez.importimages

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.alperez.demo.importimages.R
import com.alperez.importimages.model.ImageAspectRatio
import com.alperez.importimages.util.newAlertDialogBuilder

/**
 * Created by stanislav.perchenko on 22.09.2020 at 20:50.
 */
class LoadImageRequester {
    private val activity1: Activity?
    private val fragment: Fragment?
    private val context: Context
    private val title: String

    constructor(activity: Activity, title: String) {
        activity1 = activity
        fragment = null
        context = activity
        this.title = title
    }

    constructor(activity: Activity, titleResId: Int) {
        activity1 = activity
        fragment = null
        context = activity
        title = activity.resources.getString(titleResId)
    }

    constructor(fragment: Fragment, title: String) {
        activity1 = null
        this.fragment = fragment
        context = fragment.context ?: throw IllegalStateException("Provided fragment has no Context")
        this.title = title
    }

    constructor(fragment: Fragment, titleResId: Int) {
        activity1 = null
        this.fragment = fragment
        context = fragment.context ?: throw IllegalStateException("Provided fragment has no Context")
        title = context.resources.getString(titleResId)
    }

    @JvmOverloads
    fun requestLoadImage(requestId: Int, aspect: ImageAspectRatio, maxImagePixels: Int, maxSizeBytes: Int = Int.MAX_VALUE) {
        val options = arrayOf(
            context.resources.getString(R.string.action_take_picture),
            context.resources.getString(R.string.action_pick_from_gallery)
        )
        newAlertDialogBuilder(context)
            .setTitle(title)
            .setAdapter(ArrayAdapter(context, android.R.layout.simple_list_item_1, options)) { dialogInterface: DialogInterface, index: Int ->
                when (index) {
                    0 -> startSelectImageAttachmentLive(
                        requestId,
                        aspect,
                        maxImagePixels,
                        maxSizeBytes
                    )
                    1 -> startSelectImageAttachmentGallery(
                        requestId,
                        aspect,
                        maxImagePixels,
                        maxSizeBytes
                    )
                }
                dialogInterface.dismiss()
            }.show()
    }

    private fun startSelectImageAttachmentLive(requestTypeNumber: Int, aspect: ImageAspectRatio, maxImagePixels: Int, maxImageBytes: Int) {
        val intent = Intent(context, ImportImageActivity::class.java)
        intent.putExtra(ImportImageActivity.ARG_ATTACHMENT_LIVE, true)
        intent.putExtra(ImportImageActivity.ARG_ASPECT_RATIO, aspect.toString())
        intent.putExtra(ImportImageActivity.ARG_MAX_PIXELS, maxImagePixels)
        intent.putExtra(ImportImageActivity.ARG_MAX_BYTES, maxImageBytes)
        activity1?.startActivityForResult(intent, requestTypeNumber)
            ?: fragment?.startActivityForResult(intent, requestTypeNumber)
    }

    private fun startSelectImageAttachmentGallery(requestTypeNumber: Int, aspect: ImageAspectRatio, maxImagePixels: Int, maxImageBytes: Int) {
        val intent = Intent(context, ImportImageActivity::class.java)
        intent.putExtra(ImportImageActivity.ARG_ATTACHMENT_LIVE, false)
        intent.putExtra(ImportImageActivity.ARG_ASPECT_RATIO, aspect.toString())
        intent.putExtra(ImportImageActivity.ARG_MAX_PIXELS, maxImagePixels)
        intent.putExtra(ImportImageActivity.ARG_MAX_BYTES, maxImageBytes)
        activity1?.startActivityForResult(intent, requestTypeNumber)
            ?: fragment?.startActivityForResult(intent, requestTypeNumber)
    }
}
