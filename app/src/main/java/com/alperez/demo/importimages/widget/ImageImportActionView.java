package com.alperez.demo.importimages.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.alperez.demo.importimages.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.alperez.importimages.model.ImageImportModel;

import java.io.File;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 20:18.
 */
public class ImageImportActionView extends LinearLayout {

    public interface OnSelectImageListener {
        void onSelectImage();
    }

    public interface OnImageRemoveListener {
        void onImageRemoved();
    }


    private View vIconSelectImage, vIconImageSelected;

    private String mActionTitle;
    private ImageImportModel mImgModel;


    private ViewGroup vGroupTextImage;
    private TextView vTxtActionTitle;
    private ImageView vImage;


    private View vActionSelect;
    private View vActionRemove;


    private OnSelectImageListener onSelectImageListener;
    private OnImageRemoveListener onImageRemoveListener;

    public ImageImportActionView(Context context) {
        super(context);
        init(context);
    }

    public ImageImportActionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        extractCustomAttributes(context, attrs);
        init(context);
    }

    public ImageImportActionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        extractCustomAttributes(context, attrs);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ImageImportActionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        extractCustomAttributes(context, attrs);
        init(context);
    }

    private void extractCustomAttributes(Context context, @NonNull AttributeSet attrs) {
        TypedArray a = context.getResources().obtainAttributes(attrs, R.styleable.ImageImportActionView);
        mActionTitle = a.getString(R.styleable.ImageImportActionView_actionTitle);
        if (mActionTitle == null) mActionTitle = "";
        a.recycle();
    }

    private void init(Context c) {
        setOrientation(HORIZONTAL);
        setBackgroundResource(R.drawable.bg_image_import_action);
        setClickable(false);
        LayoutInflater.from(c).inflate(R.layout.layout_image_import_action, this, true);

        vIconSelectImage = findViewById(R.id.ic_select_image);
        vIconImageSelected = findViewById(R.id.ic_image_selected);
        vGroupTextImage = findViewById(R.id.group_text_image);
        vTxtActionTitle = vGroupTextImage.findViewById(R.id.txt_action_title);
        vImage = vGroupTextImage.findViewById(R.id.img_selection);
        vImage.setVisibility(View.INVISIBLE);
        (vActionSelect = findViewById(R.id.action_select)).setOnClickListener(v -> {
            if (onSelectImageListener != null) onSelectImageListener.onSelectImage();
        });
        (vActionRemove = findViewById(R.id.action_remove)).setOnClickListener(v -> {
            setSelectedImage(null);
            if (onImageRemoveListener != null)  onImageRemoveListener.onImageRemoved();
        });

        vTxtActionTitle.setText(mActionTitle);

        setSelectedImage(null);
    }

    public void setActionTitle(String actionTitle) {
        this.mActionTitle = (actionTitle == null) ? "" : actionTitle;
        if (vTxtActionTitle != null) vTxtActionTitle.setText(mActionTitle);
    }

    public void setOnSelectImageListener(OnSelectImageListener onSelectImageListener) {
        this.onSelectImageListener = onSelectImageListener;
    }

    public void setOnImageRemoveListener(OnImageRemoveListener onImageRemoveListener) {
        this.onImageRemoveListener = onImageRemoveListener;
    }

    public void setSelectedImage(@Nullable ImageImportModel imgModel) {
        if (imgModel == null) {
            mImgModel = null;
            vImage.setImageDrawable(null);
            vImage.setVisibility(View.INVISIBLE);
            vTxtActionTitle.setVisibility(View.VISIBLE);
            vIconSelectImage.setVisibility(View.VISIBLE);
            vIconImageSelected.setVisibility(View.INVISIBLE);
            vActionSelect.setVisibility(View.VISIBLE);
            vActionRemove.setVisibility(View.GONE);
        } else if (getMeasuredWidth()==0 || getMeasuredHeight()==0 || vImage.getMeasuredHeight() == 0) {
            throw new IllegalStateException("This View has not been measured yet");
        } else {
            mImgModel = imgModel;
            vImage.setVisibility(View.VISIBLE);
            vTxtActionTitle.setVisibility(View.INVISIBLE);
            vIconSelectImage.setVisibility(View.INVISIBLE);
            vIconImageSelected.setVisibility(View.VISIBLE);
            vActionSelect.setVisibility(View.GONE);
            vActionRemove.setVisibility(View.VISIBLE);

            final float origImgW = imgModel.getWidth();
            final float origImgH = imgModel.getHeight();

            final float imgContainerW = vGroupTextImage.getMeasuredWidth();
            final float imgContainerH = vGroupTextImage.getMeasuredHeight();

            final boolean needDownscale;
            float resolvedImgW, resolvedImgH;
            final float kX = origImgW / imgContainerW;
            final float kY = origImgH / imgContainerH;
            if ((kY > 1) && (kY > kX)) {
                needDownscale = true;
                resolvedImgH = imgContainerH;
                resolvedImgW = resolvedImgH * origImgW / origImgH;
            } else if ((kX > 1) && (kX > kY)) {
                needDownscale = true;
                resolvedImgW = imgContainerW;
                resolvedImgH = resolvedImgW * origImgH / origImgW;
            } else {
                needDownscale = false;
                resolvedImgW = origImgW;
                resolvedImgH = origImgH;
            }

            RequestCreator rcr = Picasso.get().load(new File(imgModel.getLocalFile()));
            if (needDownscale) rcr.resize(Math.round(resolvedImgW), Math.round(resolvedImgH));
            rcr.into(vImage);
        }
    }
}

