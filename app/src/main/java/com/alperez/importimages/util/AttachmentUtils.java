package com.alperez.importimages.util;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by stanislav.perchenko on 11.09.2020 at 16:22.
 */
public final class AttachmentUtils {

    public static final String ATTACHMENT_WORK_DIRECTORY = "attachment_work";
    public static final String DIRECTORY_ATTACHMENTS = "attachments";
    public static final String DIRECTORY_CREATED_MEDIA_CONTENT = "media";


    private AttachmentUtils() { }

    public static BitmapFactory.Options decodeBitmapForOptions(String filePath) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);
        return opts;
    }



    public static Bitmap scaleBitmapPreservingAspect(Bitmap inBmp, float scale) {
        Matrix mtrx = new Matrix();
        mtrx.postScale(scale, scale);
        return Bitmap.createBitmap(inBmp, 0, 0, inBmp.getWidth(), inBmp.getHeight(), mtrx, false);
    }

    public static int getImageRotation(String imageFilePath) throws IOException {
        String orientation = (new ExifInterface(imageFilePath)).getAttribute(ExifInterface.TAG_ORIENTATION);
        switch (orientation) {
            case "6":
                return 90;
            case "8":
                return 270;
            case "3":
                return 180;
            default:
                return 0;
        }
    }

    /**
     * Fixes rotation of an input bitmap. In any case the new file is returned even if the rotation value is 0.
     * @param bitmap The bitmap which orientation must be corrected.
     * @param imageFile The original image file of the Bitmap which is used to define image orientation
     * @return
     */
    public static Bitmap getBitmapInCorrectOrientation(Bitmap bitmap, String imageFile) {
        Bitmap result = null;
        try {
            String orientation = (new ExifInterface(imageFile)).getAttribute(ExifInterface.TAG_ORIENTATION);

            if (orientation.equalsIgnoreCase("6")) {
                result = rotateBitmap(bitmap, 90);
            } else if (orientation.equalsIgnoreCase("8")) {
                result = rotateBitmap(bitmap, 270);
            } else if (orientation.equalsIgnoreCase("3")) {
                result = rotateBitmap(bitmap, 180);
            } else {
                result = rotateBitmap(bitmap, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == 0 || h == 0) {
            return null;
        }
        Matrix mtx = new Matrix();
        mtx.setRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }





    /**
     * This helper methon must be used to create intent to start gallery for picking an image
     * @param context
     * @param pickMultiple
     * @return
     */
    public static Intent getIntentPickImagesFromGallery(Context context, boolean pickMultiple) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI).putExtra(Intent.EXTRA_ALLOW_MULTIPLE, pickMultiple);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            intent = new Intent(Intent.ACTION_GET_CONTENT).putExtra(Intent.EXTRA_ALLOW_MULTIPLE, pickMultiple);
            intent.setType("image/*");
            if (intent.resolveActivity(context.getPackageManager()) == null) {
                intent = null;
            }
        }
        return intent;
    }

    /**
     * This helper method must be used to create intent to start gallery for picking a video
     * @param context
     * @return
     */
    public static Intent getIntentPickVideoFromGallery(Context context) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            intent = null;
        }
        return intent;
    }

    /**
     * This helper method must be used to create intent to start gallery for picking an audio
     * @param context
     * @return
     */
    public static Intent getIntentPickAudioFromGallery(Context context) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            intent = null;
        }
        return intent;
    }


    /**
     * This method retrieves all Uris returned in an Intent via onActivityResult()
     * Used to pick images from gallery
     * @param result
     * @return
     */
    @SuppressLint({"NewApi"})
    public static Uri[] getUrisFromResult(@Nullable Intent result) {
        List<Uri> uriList = new ArrayList<>();
        Uri[] uriArray;
        try {
            if (result != null) {

                if ((Build.VERSION.SDK_INT >= 16) && result.getClipData() != null) {
                    ClipData cData = result.getClipData();
                    for (int i=0; i<cData.getItemCount(); i++) {
                        Uri u = cData.getItemAt(i).getUri();
                        if (u != null) {
                            uriList.add(u);
                        }
                    }
                } else if (result.getData() != null) {
                    uriList.add(result.getData());
                } else if(result.getExtras() != null){
                    Uri u = result.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
                    if (u != null) uriList.add(u);
                }
            }
        } finally {
            uriArray = new Uri[uriList.size()];
            if (uriArray.length > 0) uriList.toArray(uriArray);
        }
        return uriArray;
    }


    public static String decodeImageFileForMimeType(String path) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        if (options.outWidth==0 || options.outHeight==0 || options.outMimeType == null) {
            throw new IOException("Not an image");
        }
        return options.outMimeType;
    }


    public static Bitmap decodeBitmapMaxRezSafely(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        do {
            try {
                return BitmapFactory.decodeFile(path, options);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                options.inSampleSize *= 2;
            }
        } while (options.inSampleSize < 17);
        return null;
    }


    public static Bitmap decodeBitmapMaxRezSafely(InputStream is) {
        is.mark(0);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        do {
            try {
                return BitmapFactory.decodeStream(is, null, options);
            } catch (OutOfMemoryError e) {
                options.inSampleSize *= 2;
                try {
                    is.reset();
                } catch (IOException e1) {
                    //This happens if the <is> is ton an ByteArrayInputStream.
                    return null;
                }
            }
        } while (options.inSampleSize < 17);
        return null;
    }



    /**********************************************************************************************/
    /***************************  Video-specific helpers  *****************************************/
    /**********************************************************************************************/
    public static int[] getCoverFrameOptionTimingsForVideo(File videoFile) {
        MediaMetadataRetriever mediaRetriever = new MediaMetadataRetriever();
        mediaRetriever.setDataSource(videoFile.getAbsolutePath());
        int durationMillis = Integer.parseInt(mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

        int[] prams = getFrameNumberAndDtForVideoDuration(durationMillis);
        final int dt = prams[1];
        final int nFrames = prams[0];

        int[] result = new int[nFrames];
        int ti = (durationMillis - nFrames * dt) / 2;

        for (int i=0; i<nFrames; i++, ti += dt) {
            result[i] = ti;
        }
        return result;
    }

    private static final int MIN_FRAME_DELTA_MILLIS = 500;
    private static final int NORM_FRAME_DELTA_MILLIS = 1000;
    private static final int MIN_FRAME_NUMBER = 5;
    private static final int MAX_FRAME_NUMBER = 14;

    private static int[] getFrameNumberAndDtForVideoDuration(int tMillis) {
        int dt = NORM_FRAME_DELTA_MILLIS;
        int nFrames = tMillis/dt;
        if (nFrames > MAX_FRAME_NUMBER) {
            dt = tMillis / (nFrames = MAX_FRAME_NUMBER);
        } else if (nFrames < MIN_FRAME_NUMBER) {
            dt = tMillis / (nFrames = MIN_FRAME_NUMBER);
            if (dt < MIN_FRAME_DELTA_MILLIS) {
                nFrames = tMillis / (dt = MIN_FRAME_DELTA_MILLIS);
            }
        }
        return new int[]{nFrames, dt};
    }
}

