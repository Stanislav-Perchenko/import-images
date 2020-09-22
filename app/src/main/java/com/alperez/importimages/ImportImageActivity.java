package com.alperez.importimages;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.alperez.demo.importimages.R;
import com.alperez.importimages.model.ImageAspectRatio;
import com.alperez.importimages.model.ImageImportModel;

import java.util.Objects;


/**
 * Created by stanislav.perchenko on 22.09.2020 at 21:26.
 */
public class ImportImageActivity extends AppCompatActivity implements AttachAvatarView, CropImportedImageFragment.OnCropImportImageListener {

    public static final String ARG_ATTACHMENT_LIVE = "attachment_live";
    public static final String ARG_ASPECT_RATIO = "crop_shape";
    public static final String ARG_MAX_PIXELS = "max_pixels";
    public static final String ARG_MAX_BYTES = "max_bytes";

    public static final String RESULT_COVERIMAGE = "coverimage";

    private static final String ARG_TAKE_PICTURE_BUNDLE = "take_picture";

    private ImageAspectRatio argAspect;

    private AttachAvatarPresenter presenter;
    private CropImportedImageFragment cropFragment;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideBars();
        setContentView(R.layout.activity_attach_avatar);
        argAspect = ImageAspectRatio.valueOf(Objects.requireNonNull(getIntent().getExtras()).getString(ARG_ASPECT_RATIO, "none"));

        presenter = new AttachAvatarPresenter(this, getAttachmentLiveArgument(), getIntent().getIntExtra(ARG_MAX_PIXELS, Integer.MAX_VALUE), getIntent().getIntExtra(ARG_MAX_BYTES, Integer.MAX_VALUE));
        if (savedInstanceState == null) {
            presenter.initialize();
        } else {
            Bundle bundle = savedInstanceState.getBundle(ARG_TAKE_PICTURE_BUNDLE);
            if (bundle != null) {
                presenter.setTakePictureIntent(bundle);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideBars();
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void hideBars() {
        int uiOptions = getWindow().getDecorView().getSystemUiVisibility();

        int newUiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_IMMERSIVE;
        }

        getWindow().getDecorView().setSystemUiVisibility(uiOptions | newUiOptions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean consumed = presenter.onRequestPermissionsResult(requestCode, grantResults);
        if (!consumed) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean consumed = presenter.onActivityResult(requestCode, resultCode, data);
        if (!consumed) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Bundle takePicture = presenter.getTakePictureIntent();
        if (takePicture != null) {
            outState.putBundle(ARG_TAKE_PICTURE_BUNDLE, takePicture);
        }
        super.onSaveInstanceState(outState);
    }

    /**************************  View interface implementation  ***********************************/

    @Override
    public void close() {
        getWindow().getDecorView().postDelayed(() -> finish(), 300);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void requestPermissionsForPresenter(@NonNull String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    @Override
    public void showProgressWithMessage(String message) {
        if (this.isFinishing()) return;

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    @Override
    public void dismissProgressWithMessage() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressWithMessage();
        presenter.release();
    }

    @Override
    public void onAttachmentImported(ImageImportModel coverImage) {
        if (cropFragment == null) {
            cropFragment = CropImportedImageFragment.newInstance(coverImage, argAspect);
            getSupportFragmentManager().beginTransaction().add(R.id.attach_avatar_content, cropFragment, "crop_fragment").commit();
        } else {
            cropFragment.setNewImage(coverImage);
        }
    }

    @Override
    public void finishWithResult(ImageImportModel cover) {
        Intent data = new Intent().putExtra(RESULT_COVERIMAGE, (Parcelable) cover);
        setResult(RESULT_OK, data);
        finish();
    }

    /***********************  Cropping fragment interface implementation  *************************/
    @Override
    public void onTryAgain() {
        cropFragment.setNewImage(null);
        presenter.clearAndRecycle();
        presenter.initialize();
    }

    @Override
    public void onCancel() {
        presenter.clearAndRecycle();
        finish();
    }

    @Override
    public void onImageCropped(ImageImportModel img, Rect selectedRegion) {
        presenter.processImportedImageAndFinish(img, selectedRegion);
    }

    private boolean getAttachmentLiveArgument() {
        try {
            return Objects.requireNonNull(getIntent().getExtras()).getBoolean(ARG_ATTACHMENT_LIVE, true);
        } catch (NullPointerException e) {
            return true;
        }
    }

}

