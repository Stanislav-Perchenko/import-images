package com.alperez.importimages.util;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by stanislav.perchenko on 11.09.2020 at 16:20.
 */
public final class FileUtils {

    private FileUtils() { }

    /**
     * @param ctx
     * @param path relative path against cache directory.
     * @return
     */
    public static File getFinalCacheDirectory(Context ctx, String path) throws IOException {
        File dir = getDiskCacheDir(ctx, path);
        return createDirIfNeeded(dir);
    }

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context    The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use
        // external cache dir
        // otherwise use internal cache dir
        boolean useExternalMemory = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !isExternalStorageRemovable();
        File cacheFolder;
        if (useExternalMemory) {
            cacheFolder = getExternalCacheDir(context);
        } else {
            try {
                cacheFolder = context.getCacheDir();
            } catch (Exception e) {
                cacheFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // Lastly, try this as a last ditch effort
            }
        }

        return TextUtils.isEmpty(uniqueName) ? cacheFolder : new File(cacheFolder.getPath() + File.separator + uniqueName);
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @TargetApi(8)
    public static File getExternalCacheDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     * otherwise.
     */
    @TargetApi(9)
    public static boolean isExternalStorageRemovable() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD || Environment.isExternalStorageRemovable();
    }

    /**
     * Creates a directory and puts a .nomedia file in it
     *
     * @param dir
     * @return new dir
     * @throws IOException
     */
    private static File createDirIfNeeded(File dir) throws IOException {
        if ((dir != null) && !dir.exists()) {
            if (!dir.mkdirs() && !dir.isDirectory()) {
                throw new IOException("error create directory");
            }
            File noMediaFile = new File(dir, ".nomedia");
            noMediaFile.createNewFile();
        }
        return dir;
    }


    public static File createImageFileForCamera(Context context, @NonNull MediaScannerConnection.OnScanCompletedListener l) throws IOException {
        File dir = FileUtils.getFinalCacheDirectory(context, AttachmentUtils.DIRECTORY_CREATED_MEDIA_CONTENT);
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";

        File imgFile = new File(dir, imageFileName);
        imgFile.createNewFile();
        MediaScannerConnection.scanFile(context, new String[] { imgFile.getAbsolutePath() }, null, l);

        return imgFile;
    }

    public static void streamToFile(File dstFile, InputStream src) throws IOException {
        OutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(dstFile));
            byte data[] = new byte[1024];
            int count;

            while ((count = src.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
        } finally {
            if (output != null) output.close();
        }
    }

    /**
     * Returns File with UUID-type name which does not exist in the provided directory
     * @param directory
     * @return
     */
    public static @Nullable
    File getNewRandomFile(@Nullable File directory) {
        if ((directory == null) || !directory.isDirectory()) {
            return null;
        }

        File f;
        do {
            f = new File(directory, UUID.randomUUID().toString());
        } while (f != null && f.exists());
        return f;
    }

    public static String getPath(final Context context, final Uri uri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {

            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);

            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);

            } else if (isGoogleDriveFile(uri)) {
                return "google_drive";
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {

            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            return getDataColumn(context, uri, null, null);

        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isGoogleDriveFile(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }
}

