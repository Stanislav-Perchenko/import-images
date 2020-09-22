package com.alperez.importimages;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.alperez.importimages.model.ImageImportModel;
import com.alperez.importimages.util.BasePresenter;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 21:45.
 */
public class AttachAvatarPresenter extends BasePresenter<AttachAvatarView> {
    private static final int REQUEST_TAKE_AVATAR = 101;
    private static final int REQUEST_PICK_AVATAR = 102;
    private static final int REQUEST_PERMISSIONS_PICTURE_FROM_CAMERA = 103;
    private static final int REQUEST_PERMISSIONS_PICTURE_FROM_GALLERY = 104;

    private static final String ARG_BUNDLE_TAKE_INTENT = "take_intent";




    public AttachAvatarPresenter(AttachAvatarView attachAvatarView, boolean liveAttachment, int maxImagePixels, int maxImageSizeBytes) {
        super(attachAvatarView);
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    @Override
    public void initialize() {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    @Override
    public synchronized void release() {
        super.release();
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    public boolean onRequestPermissionsResult(int requestCode, int[] grantResults) {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return false;
    }

    /**
     * This method is to process calling children activities result.
     * It must be calles from the Activity's onActivityReenter
     * @param requestCode
     * @param resultCode
     * @param data
     * @return true if result was consumed (no need to call super Activity method)
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return false;
    }






    public void processImportedImageAndFinish(ImageImportModel img, @Nullable Rect scropRegion) {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    public void clearAndRecycle() {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    public Bundle getTakePictureIntent() {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        return null;
    }

    public void setTakePictureIntent(Bundle takePictureBundle) {
        //TODO Implement this !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }
}


