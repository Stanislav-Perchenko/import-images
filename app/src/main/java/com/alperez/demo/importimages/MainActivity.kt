package com.alperez.demo.importimages

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.alperez.demo.importimages.widget.ImageImportActionView
import com.alperez.importimages.LoadImageRequester
import com.alperez.importimages.model.ImageAspectRatio
import com.alperez.importimages.model.ImageImportModel

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


    private fun submitSelectedDocumentImages() {
        //TODO Implement logic here !!!!!!!!!!!!!!
        Toast.makeText(this, "Submit selected images", Toast.LENGTH_LONG).show()
    }


}