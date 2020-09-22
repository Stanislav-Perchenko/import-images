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


import com.alperez.common.error.AppError;
import com.alperez.common.executor.BackgroundExecutor;
import com.alperez.common.executor.model.Callback;
import com.alperez.common.executor.model.Disposable;
import com.alperez.demo.importimages.R;
import com.alperez.demo.importimages.utils.Toaster;
import com.alperez.importimages.model.ImageImportModel;
import com.alperez.importimages.processing.ApiImportImageSimple;
import com.alperez.importimages.processing.ApiProcessImportedImage;
import com.alperez.importimages.util.AttachmentUtils;
import com.alperez.importimages.util.BasePresenter;
import com.alperez.importimages.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 21:45.
 */
public class AttachAvatarPresenter extends BasePresenter<AttachAvatarView> {
    private static final int REQUEST_TAKE_AVATAR = 101;
    private static final int REQUEST_PICK_AVATAR = 102;
    private static final int REQUEST_PERMISSIONS_PICTURE_FROM_CAMERA = 103;
    private static final int REQUEST_PERMISSIONS_PICTURE_FROM_GALLERY = 104;

    private static final String ARG_BUNDLE_TAKE_INTENT = "take_intent";

    private boolean liveAttachment;
    private final int maxImagePixels;
    private final int maxImageSizeBytes;

    private BackgroundExecutor executorRemote;

    private ImageImportModel mOriginalImportedImage;


    public AttachAvatarPresenter(AttachAvatarView attachAvatarView, boolean liveAttachment, int maxImagePixels, int maxImageSizeBytes) {
        super(attachAvatarView);
        this.liveAttachment = liveAttachment;
        this.maxImagePixels = maxImagePixels;
        this.maxImageSizeBytes = maxImageSizeBytes;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (executorRemote == null) {
            executorRemote = new BackgroundExecutor(Looper.getMainLooper());
        }

        boolean started = (liveAttachment) ? tryStartCameraForImage() :  tryStartGallery();
        if (!started) {
            final String errText =  getView().getContext().getString((liveAttachment) ? R.string.err_camera_not_stated : R.string.err_gallery_access);
            AppError err = new AppError() {
                @Override public String getInternalMessage() { return null; }
                @Override public String getUserMessage() { return errText; }
                @Override public Exception getException() { return null; }
            };
            Toaster.toastErrorLong(getView().getContext(), R.string.err_import_image, err);
            getView().close();
        }
    }

    @Override
    public synchronized void release() {
        super.release();
        if (executorRemote != null) {
            executorRemote.release();
            executorRemote = null;
        }
    }

    public boolean onRequestPermissionsResult(int requestCode, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_PICTURE_FROM_CAMERA:
                if(reTryStartCameraWithPermissions) {
                    reTryStartCameraWithPermissions = false;
                    for(int result : grantResults) {
                        if(result != PackageManager.PERMISSION_GRANTED) {
                            AttachAvatarView v = getView();
                            if (!isReleased() && (v != null)) {
                                Toaster.toastToUser(v.getContext(), R.string.err_permissions_not_granted);
                                v.close();
                            }
                            return true;
                        }
                    }
                    tryStartCameraForImage();
                }
                return true;
            case REQUEST_PERMISSIONS_PICTURE_FROM_GALLERY:
                if (reTryStartGalleryWithPermissions) {
                    reTryStartGalleryWithPermissions = false;
                    for (int result : grantResults) {
                        if(result != PackageManager.PERMISSION_GRANTED) {
                            AttachAvatarView v = getView();
                            if (!isReleased() && (v != null)) {
                                Toaster.toastToUser(v.getContext(), R.string.err_permissions_not_granted);
                                v.close();
                            }
                            return true;
                        }
                    }
                    tryStartGallery();
                }
                return true;
            default:
                return false;
        }
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
        Uri[] selectedItems;
        switch (requestCode) {
            case REQUEST_TAKE_AVATAR:
                selectedItems = AttachmentUtils.getUrisFromResult(takePictureIntent);
                if (resultCode == Activity.RESULT_OK && selectedItems.length > 0) {
                    importImageAsync(selectedItems[0]);
                } else if (!isReleased()) {
                    AttachAvatarView v = getView();
                    Toaster.toastToUser(v.getContext(), R.string.err_image_not_taken);
                    v.close();
                }
                return true;
            case REQUEST_PICK_AVATAR:
                selectedItems = AttachmentUtils.getUrisFromResult(data);
                if (resultCode == Activity.RESULT_OK && selectedItems.length > 0) {
                    importImageAsync(selectedItems[0]);
                } else if (!isReleased()) {
                    AttachAvatarView v = getView();
                    Toaster.toastToUser(v.getContext(), R.string.err_image_not_picked);
                    v.close();
                }
                return true;
        }
        return false;
    }

    private void importImageAsync(Uri src) {
        new ApiImportImageSimple(getView().getContext(), src, false).on(executorRemote).executeAsync(new Callback<ImageImportModel>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                registerApiDisposable(d);
                getView().showProgressWithMessage(getView().getContext().getString(R.string.progress_import));
            }

            @Override
            public void onSuccess(@Nullable ImageImportModel result) {
                AttachAvatarView v = getView();
                if (!isReleased() && v != null) {
                    v.onAttachmentImported(mOriginalImportedImage = result);
                } else {
                    clearAndRecycle();
                }
            }

            @Override
            public void onError(@NonNull AppError reason) {
                AttachAvatarView v = getView();
                if (!isReleased() && v != null) {
                    Toaster.toastErrorLong(v.getContext(), R.string.err_import_image, reason);
                    v.close();
                } else {
                    clearAndRecycle();
                }
            }

            @Override
            public void onComplete(@NonNull Disposable d) {
                unregisterApiDisposable(d);
                AttachAvatarView v = getView();
                if (!isReleased() && v != null) v.dismissProgressWithMessage();
            }
        });
    }

    /**
     * This field is to be used in the onActivityResult()
     * As the camera activity does not return Uri.
     */
    private Intent takePictureIntent;

    private boolean reTryStartCameraWithPermissions;

    private boolean tryStartCameraForImage() {
        Context ctx = getView().getContext();
        reTryStartCameraWithPermissions = false;
        List<String> notGrantedPermissions = new ArrayList<>(3);
        if(ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            notGrantedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(ActivityCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            notGrantedPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(ActivityCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            notGrantedPermissions.add(Manifest.permission.CAMERA);
        }
        if(notGrantedPermissions.size() > 0) {
            reTryStartCameraWithPermissions = true;
            getView().requestPermissionsForPresenter(notGrantedPermissions.toArray(new String[notGrantedPermissions.size()]), REQUEST_PERMISSIONS_PICTURE_FROM_CAMERA);
            return true;
        }


        takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(ctx.getPackageManager()) != null) {
            try {
                FileUtils.createImageFileForCamera(getView().getContext(), (MediaScannerConnection.OnScanCompletedListener) (s, uri) -> {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    getView().getActivity().startActivityForResult(takePictureIntent, REQUEST_TAKE_AVATAR);
                });
                return true;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }


    private boolean reTryStartGalleryWithPermissions;

    private boolean tryStartGallery() {
        Context ctx = getView().getContext();
        reTryStartGalleryWithPermissions = false;
        List<String> notGrantedPermissions = new ArrayList<>(2);
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            notGrantedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            notGrantedPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (notGrantedPermissions.size() > 0) {
            reTryStartGalleryWithPermissions = true;
            getView().requestPermissionsForPresenter(notGrantedPermissions.toArray(new String[notGrantedPermissions.size()]), REQUEST_PERMISSIONS_PICTURE_FROM_GALLERY);
            return true;
        }

        Intent intent = AttachmentUtils.getIntentPickImagesFromGallery(getView().getContext(), false);
        if (intent != null) {
            ((Activity) getView().getContext()).startActivityForResult(Intent.createChooser(intent, "Select pictures"), REQUEST_PICK_AVATAR);
            return true;
        }
        return false;
    }




    public void processImportedImageAndFinish(ImageImportModel img, @Nullable Rect scropRegion) {
        ApiProcessImportedImage.newBuilder(getView().getContext(), img)
                .setCropRegion(scropRegion)
                .setMaxPixelNumber(maxImagePixels)
                .setMaxSizeBytes(maxImageSizeBytes)
                .build().on(executorRemote).executeAsync(new Callback<ImageImportModel>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                registerApiDisposable(d);
                getView().showProgressWithMessage(getView().getContext().getString(R.string.progress_processing));
            }

            @Override
            public void onSuccess(@Nullable ImageImportModel result) {
                clearAndRecycle();
                AttachAvatarView v = getView();
                if (!isReleased() && v != null) {
                    v.finishWithResult(result);
                }
            }

            @Override
            public void onError(@NonNull AppError reason) {
                clearAndRecycle();
                AttachAvatarView v = getView();
                if (!isReleased() && v != null) {
                    Toaster.toastErrorLong(v.getContext(), R.string.err_process_image, reason);
                    v.close();
                }
            }

            @Override
            public void onComplete(@NonNull Disposable d) {
                unregisterApiDisposable(d);
                AttachAvatarView v = getView();
                if (!isReleased() && v != null) v.dismissProgressWithMessage();
            }
        });
    }

    public void clearAndRecycle() {
        reTryStartCameraWithPermissions = false;

        if (mOriginalImportedImage != null) {
            (new File(mOriginalImportedImage.getLocalFile())).delete();
            mOriginalImportedImage = null;
        }
    }

    public Bundle getTakePictureIntent() {
        if (takePictureIntent != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(ARG_BUNDLE_TAKE_INTENT, takePictureIntent);
            return bundle;
        } else {
            return null;
        }
    }

    public void setTakePictureIntent(Bundle takePictureBundle) {
        takePictureIntent = takePictureBundle.getParcelable(ARG_BUNDLE_TAKE_INTENT);
    }
}




