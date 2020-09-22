package com.alperez.importimages.processing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import com.alperez.common.executor.AbstractExecutable;
import com.alperez.importimages.model.ImageImportModel;
import com.alperez.importimages.util.AttachmentUtils;
import com.alperez.importimages.util.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * Created by stanislav.perchenko on 22.09.2020 at 22:46.
 */
public class ApiImportImageSimple extends AbstractExecutable<ImageImportModel> {

    private final Context context;
    private final Uri uri;
    private final boolean isPutInFinalAttachmentDirectory;

    public ApiImportImageSimple(Context context, Uri uri, boolean isPutInFinalAttachmentDirectory) {
        this.context = context;
        this.uri = uri;
        this.isPutInFinalAttachmentDirectory = isPutInFinalAttachmentDirectory;
    }

    @Override
    public void executeSynchronously() throws Exception {
        File cacheDir = FileUtils.getFinalCacheDirectory(context, isPutInFinalAttachmentDirectory ? AttachmentUtils.DIRECTORY_ATTACHMENTS : AttachmentUtils.ATTACHMENT_WORK_DIRECTORY);
        File dstFile = FileUtils.getNewRandomFile(cacheDir);

        if (dstFile == null) {
            throw new Exception("Error create cache file for importing content");
        }

        try (InputStream is = getContentInputStream(); OutputStream os = new BufferedOutputStream(new FileOutputStream(dstFile))) {

            MessageDigest digester = MessageDigest.getInstance("SHA-256");
            byte[] transBuff = new byte[4096];
            int size = 0;
            int nBytes;
            while ((nBytes = is.read(transBuff)) > 0) {
                digester.update(transBuff, 0, nBytes);
                os.write(transBuff, 0, nBytes);
                size += nBytes;
            }
            os.flush();

            BitmapFactory.Options opts = new BitmapFactory.Options();
            Bitmap bmp = BitmapFactory.decodeFile(dstFile.getAbsolutePath(), opts);
            bmp.recycle();
            if ((bmp == null) || TextUtils.isEmpty(opts.outMimeType)) {
                throw new Exception("Importing resource is probably not an image");
            }

            String hashBase64 = Base64.encodeToString(digester.digest(), Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
            String relativeHashName = hashBase64 + "." + opts.outMimeType.substring(opts.outMimeType.lastIndexOf('/') + 1);

            setResult(new ImageImportModel.Builder()
                    .setLocalFile(dstFile.getAbsolutePath())
                    .setSize(size)
                    .setMimeType(opts.outMimeType)
                    .setRelativeHashName(relativeHashName)
                    .setWidth(opts.outWidth)
                    .setHeight(opts.outHeight)
                    .setCreationTimestamp(System.currentTimeMillis())
                    .build());
        }
    }

    private InputStream getContentInputStream() throws FileNotFoundException {
        return (uri.toString().startsWith("content:"))
                ? new BufferedInputStream(context.getContentResolver().openInputStream(uri))
                : new BufferedInputStream(new FileInputStream(uri.getPath()));
    }
}
