package com.alperez.demo.importimages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.alperez.demo.importimages.widget.ImageImportActionView
import com.alperez.importimages.ImportImageActivity
import com.alperez.importimages.LoadImageRequester
import com.alperez.importimages.model.ImageAspectRatio
import com.alperez.importimages.model.ImageImportModel
import com.squareup.picasso.Picasso

/**
 * Created by stanislav.perchenko on 22.09.2020 at 19:49.
 */
class MainActivity : BaseActivity() {

    companion object {
        const val REQUEST_LOAD_IMAGE_ID_CARD_FRONT = 101
        const val REQUEST_LOAD_IMAGE_ID_CARD_BACK = 102
    }

    private lateinit var vIdCardFrontSideSelector: ImageImportActionView
    private lateinit var vIdCardBackSideSelector: ImageImportActionView
    private lateinit var vActionSubmitIdCardImages: View


    //----  Selected documents for scanning  ----
    private var mImageIdCardFront: ImageImportModel? = null
    private var mImageIdCardBack: ImageImportModel? = null

    private lateinit var loadImageRequester: LoadImageRequester


    override fun getToolbarResId(): Int? = R.id.toolbar

    override fun getScreenTitle(): String = resources.getString(R.string.app_name)

    override fun getScreenSubTitle(): String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbar(false)

        findViewById<ImageImportActionView>(R.id.id_card_front_side_selector).also {vIdCardFrontSideSelector = it}.setOnSelectImageListener {
            loadImageRequester.requestLoadImage(REQUEST_LOAD_IMAGE_ID_CARD_FRONT, ImageAspectRatio.ASPECT_ANY, Integer.MAX_VALUE, Integer.MAX_VALUE)
        }
        vIdCardFrontSideSelector.setOnImageRemoveListener {
            mImageIdCardFront = null
            vActionSubmitIdCardImages.isEnabled = false
        }

        findViewById<ImageImportActionView>(R.id.id_card_back_side_selector).also {vIdCardBackSideSelector = it}.setOnSelectImageListener {
            loadImageRequester.requestLoadImage(REQUEST_LOAD_IMAGE_ID_CARD_BACK, ImageAspectRatio.ASPECT_ANY, Integer.MAX_VALUE, Integer.MAX_VALUE)
        }
        vIdCardBackSideSelector.setOnImageRemoveListener {
            mImageIdCardBack = null
            vActionSubmitIdCardImages.isEnabled = false
        }

        findViewById<View>(R.id.action_submit).also {
            vActionSubmitIdCardImages = it
            vActionSubmitIdCardImages.isEnabled = false
        }.setOnClickListener() { _ -> submitSelectedDocumentImages() }

        loadImageRequester = LoadImageRequester(this, resources.getString(R.string.dialog_title_import_document_image))
    }


    /***********************************************************************************************
     * Receiving selected images
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((resultCode != Activity.RESULT_CANCELED) && (data?.hasExtra(ImportImageActivity.RESULT_COVERIMAGE) == true)) {
            val model = data.getParcelableExtra<ImageImportModel>(ImportImageActivity.RESULT_COVERIMAGE)
            when (requestCode) {
                REQUEST_LOAD_IMAGE_ID_CARD_FRONT -> {
                    mImageIdCardFront = model
                    vIdCardFrontSideSelector.setSelectedImage(model)
                    vActionSubmitIdCardImages.isEnabled = (mImageIdCardBack != null)
                }
                REQUEST_LOAD_IMAGE_ID_CARD_BACK -> {
                    mImageIdCardBack = model
                    vIdCardBackSideSelector.setSelectedImage(model)
                    vActionSubmitIdCardImages.isEnabled = (mImageIdCardFront != null)
                }
                else -> throw RuntimeException("WTF! This should not happen by design!")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }



    private fun submitSelectedDocumentImages() {
        //TODO Implement logic here !!!!!!!!!!!!!!
        Toast.makeText(this, "Submit selected images", Toast.LENGTH_LONG).show()
    }


}