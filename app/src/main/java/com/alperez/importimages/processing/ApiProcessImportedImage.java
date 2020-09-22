package com.alperez.importimages.processing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.alperez.common.executor.AbstractExecutable;
import com.alperez.importimages.model.ImageImportModel;
import com.alperez.importimages.util.AttachmentUtils;
import com.alperez.importimages.util.FileUtils;
import com.alperez.importimages.util.HashCalculator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 22:47.
 */
public class ApiProcessImportedImage extends AbstractExecutable<ImageImportModel> {

    private final Context context;
    private final ImageImportModel origImportModel;
    private final int maxPixelNumber;
    private final int maxSizeBytes;
    private final Rect cropRegion;

    private ApiProcessImportedImage(Context context, ImageImportModel origImportModel, int maxPixelNumber, int maxSizeBytes, Rect cropRegion) {
        this.context = context;
        this.origImportModel = origImportModel;
        this.maxPixelNumber = (maxPixelNumber > 0) ? maxPixelNumber : Integer.MAX_VALUE;
        this.maxSizeBytes = (maxSizeBytes > 0) ? maxSizeBytes : Integer.MAX_VALUE;
        this.cropRegion = cropRegion;
    }

    public static Builder newBuilder(Context c, ImageImportModel imgModel) {
        return new Builder(c, imgModel);
    }

    @Override
    public void executeSynchronously() throws Exception {
        BitmapFactory.Options opts = AttachmentUtils.decodeBitmapForOptions(origImportModel.getLocalFile());
        final int origW = opts.outWidth;
        final int origH = opts.outHeight;
        final int imageRotation = AttachmentUtils.getImageRotation(origImportModel.getLocalFile());

        if (cropRegion != null) {
            if (cropRegion.left < 0) {
                cropRegion.right -= cropRegion.left;
                cropRegion.left = 0;
                if (cropRegion.right > origW) cropRegion.right = origW;
            }
            if (cropRegion.top < 0) {
                cropRegion.bottom -= cropRegion.top;
                cropRegion.top = 0;
                if (cropRegion.bottom > origH) cropRegion.right = origH;
            }
            if (cropRegion.right > origW) {
                cropRegion.left -= (cropRegion.right - origW);
                cropRegion.right = origW;
                if (cropRegion.left < 0) cropRegion.left = 0;
            }
            if (cropRegion.bottom > origH) {
                cropRegion.top -= (cropRegion.bottom - origH);
                cropRegion.bottom = origH;
                if (cropRegion.top < 0) cropRegion.top = 0;
            }
        }

        //--- Decode bitmap from file (safe) ---
        Bitmap originalBmp = AttachmentUtils.decodeBitmapMaxRezSafely(origImportModel.getLocalFile());

        //--- Check if decoded image was downscaled and correct crop region ---
        if ((cropRegion != null) && ((origW != originalBmp.getWidth()) || (origH != originalBmp.getHeight()))) {
            float preDscl = Math.min(((float) originalBmp.getWidth()/origW), ((float) originalBmp.getHeight() / origH));
            cropRegion.left = Math.round(cropRegion.left * preDscl);
            cropRegion.top = Math.round(cropRegion.top * preDscl);
            cropRegion.right = Math.round(cropRegion.right * preDscl);
            cropRegion.bottom = Math.round(cropRegion.bottom * preDscl);
        }

        //---  Define processing matrix if necessary ---
        Matrix dsMatrix = null;
        double dstPixels = (cropRegion == null) ? (origW * origH) : (cropRegion.width() * cropRegion.height());
        if (dstPixels/maxPixelNumber > 1.03) {
            if (dsMatrix == null) dsMatrix = new Matrix();

            float scale = (float) Math.sqrt( 1.0 * maxPixelNumber/dstPixels );
            dsMatrix.preScale(scale, scale);
        }
        if (imageRotation != 0) {
            if (dsMatrix == null) dsMatrix = new Matrix();
            dsMatrix.postRotate(imageRotation, 0, 0);
        }

        Bitmap processedImage = null;
        try {
            //---  Crop and process (downscale and/or rotate) image  ----
            processedImage = Bitmap.createBitmap(originalBmp, cropRegion.left, cropRegion.top, cropRegion.width(), cropRegion.height(), dsMatrix, (dsMatrix != null));
            if (processedImage != originalBmp) originalBmp.recycle();

            //----  Compress image to byte array in memory  ----
            byte[] compressedBytes = compressBitmapPNG(processedImage);
            String nameExtension = ".png";

            //----  Check maximum bytes size and re-compress if necessary  ----
            int quality = 100;
            while(compressedBytes.length > maxSizeBytes && (quality >= 0)) {
                compressedBytes = compressBitmapJPEG(processedImage, quality);
                nameExtension = ".jpeg";
                quality -= 5;
            }
            if (compressedBytes.length > maxSizeBytes) {
                throw new Exception("compressed size limit unreachable");
            }

            //----  Get local file name by calculating hash  ----
            String hashName = calculateHash(compressedBytes) + nameExtension;

            //----  Save compressed data to file  ----
            String destFilePath = saveCompressedData(hashName, compressedBytes);

            //----  Decode save file back for checking and get MIME type  ---
            String mimeType = AttachmentUtils.decodeImageFileForMimeType(destFilePath);

            //----  Instantiate result model and return  ----
            setResult(new ImageImportModel.Builder()
                    .setLocalFile(destFilePath)
                    .setSize(compressedBytes.length)
                    .setMimeType(mimeType)
                    .setRelativeHashName(hashName)
                    .setWidth(processedImage.getWidth())
                    .setHeight(processedImage.getHeight())
                    .setCreationTimestamp(System.currentTimeMillis())
                    .build());

        } finally {
            if (processedImage != null && !processedImage.isRecycled()) processedImage.recycle();
            if (originalBmp != null && !originalBmp.isRecycled()) originalBmp.recycle();
        }
    }


    private byte[] compressBitmapPNG(Bitmap bmp) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(bmp.getByteCount() / 2)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 0, bos);
            return bos.toByteArray();
        }
    }

    private byte[] compressBitmapJPEG(Bitmap bmp, int quality) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(bmp.getByteCount() / 2)) {
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            return bos.toByteArray();
        }
    }

    private String calculateHash(byte[] data) throws IOException, NoSuchAlgorithmException {
        try (InputStream is = new ByteArrayInputStream(data)) {
            return HashCalculator.createForSHA256().process(is).getBase64Hash();
        }
    }

    /**
     * Saves array of bytes to file and returns the absolute path of the created files
     * @param relativeFileName
     * @param data
     * @return
     */
    private String saveCompressedData(String relativeFileName, byte[] data) throws IOException {
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(data);
            File fDest = new File(FileUtils.getFinalCacheDirectory(context, AttachmentUtils.DIRECTORY_ATTACHMENTS), relativeFileName);
            if (fDest.exists()) fDest.delete();
            FileUtils.streamToFile(fDest, is);
            return fDest.getAbsolutePath();
        } finally {
            if (is != null) is.close();
        }
    }


    /**********************************************************************************************/
    /***************************  Builder implementation  *****************************************/
    /**********************************************************************************************/
    public static class Builder {
        Context context;
        ImageImportModel imgModel;
        int maxPixelNumber;
        int maxSizeBytes;
        Rect cropRegion;
        private Builder(Context c, ImageImportModel imgModel) {
            assert (c != null);
            assert (imgModel != null);
            context = c;
            this.imgModel = imgModel;
        }

        public Builder setCropRegion(Rect cropRegion) {
            this.cropRegion = cropRegion;
            return this;
        }

        public Builder setMaxPixelNumber(int maxPixelNumber) {
            this.maxPixelNumber = maxPixelNumber;
            return this;
        }

        public Builder setMaxSizeBytes(int maxSizeBytes) {
            this.maxSizeBytes = maxSizeBytes;
            return this;
        }

        public ApiProcessImportedImage build() {
            return new ApiProcessImportedImage(context, imgModel, maxPixelNumber, maxSizeBytes, cropRegion);
        }
    }
}
