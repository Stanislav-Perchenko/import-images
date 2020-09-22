package com.alperez.importimages;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.alperez.importimages.model.ImageImportModel;
import com.alperez.importimages.util.ContextProvidingView;

/**
 * Created by stanislav.perchenko on 10.09.2020 at 12:01.
 */
public interface AttachAvatarView extends ContextProvidingView {
    Activity getActivity();

    void requestPermissionsForPresenter(@NonNull String[] permissions, int requestCode);

    void showProgressWithMessage(String message);
    void dismissProgressWithMessage();

    void onAttachmentImported(ImageImportModel result);
    void finishWithResult(ImageImportModel cover);
}