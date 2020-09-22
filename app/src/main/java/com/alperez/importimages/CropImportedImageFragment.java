package com.alperez.importimages;

import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.alperez.demo.importimages.R;
import com.alperez.demo.importimages.databinding.FragmentCropAvatarImageBinding;
import com.alperez.importimages.model.ImageAspectRatio;
import com.alperez.importimages.model.ImageImportModel;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 21:33.
 */
public class CropImportedImageFragment extends Fragment {
    public CropImportedImageFragment() {
    }

    public interface OnCropImportImageListener {
        void onTryAgain();
        void onCancel();
        void onImageCropped(ImageImportModel img, Rect selectedRegion);
    }

    private static final String ARG_IMPORT_IMAGE = "file";
    private static final String ARG_CROP_ASPECT= "aspect";

    public static CropImportedImageFragment newInstance(ImageImportModel img, ImageAspectRatio aspect) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_IMPORT_IMAGE, img);
        args.putString(ARG_CROP_ASPECT, aspect.toString());
        CropImportedImageFragment f = new CropImportedImageFragment();
        f.setArguments(args);
        return f;
    }

    private OnCropImportImageListener mListener;

    private ImageImportModel argImage;
    private FragmentCropAvatarImageBinding binding;
    private boolean firstTimeCreated = true;

    @Override
    public void onAttach(Context context) {
        if (context instanceof OnCropImportImageListener) {
            mListener = (OnCropImportImageListener) context;
            super.onAttach(context);
        } else {
            throw new IllegalStateException("A host activity must implement the OnCropCoverImageListener interface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (binding == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_crop_avatar_image, container, false);
        } else {
            container.removeView(binding.getRoot());
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (firstTimeCreated) {
            firstTimeCreated = false;

            argImage = getArguments().getParcelable(ARG_IMPORT_IMAGE);
            final String txtAspect = getArguments().getString(ARG_CROP_ASPECT);
            ImageAspectRatio aspect = TextUtils.isEmpty(txtAspect) ? ImageAspectRatio.ASPECT_ANY : ImageAspectRatio.valueOf(txtAspect);

            binding.croppedAvatar.setAspectRatio(aspect.getWidth(), aspect.getHeight());
            binding.croppedAvatar.setCropShape((aspect == ImageAspectRatio.ASPECT_ANY) || (aspect.getWidth() != aspect.getHeight()) ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL);
            binding.croppedAvatar.setFixedAspectRatio(aspect.isFixedAspectRatio());
            binding.croppedAvatar.setMultiTouchEnabled(false);
            binding.croppedAvatar.setScaleType(CropImageView.ScaleType.FIT_CENTER);
            binding.croppedAvatar.setShowProgressBar(true);
            binding.croppedAvatar.setImageUriAsync(Uri.fromFile(new File(argImage.getLocalFile())));

            binding.setClickerDone(v -> {
                binding.croppedAvatar.getCropRect();
                mListener.onImageCropped(argImage, binding.croppedAvatar.getCropRect());
            });
            binding.setClickerCancel(v -> mListener.onCancel());
            binding.setClickerRedo(v -> mListener.onTryAgain());
        }
    }

    public void setNewImage(@Nullable ImageImportModel img) {
        argImage = img;
        if (binding != null) {
            binding.croppedAvatar.setShowProgressBar(true);
            if (img != null) {
                binding.croppedAvatar.setImageUriAsync(Uri.fromFile(new File(img.getLocalFile())));
            } else {
                binding.croppedAvatar.clearImage();
            }
        }
    }
}

