package twork.bollywood.videostatus;

import android.Manifest.permission;
import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.ads.MobileAds;

import java.io.ByteArrayOutputStream;

import twork.bollywood.videostatus.utils.Debugger;


/**
 * Created by admin on 5/28/2017.
 */

public class Globals extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {
    public static String TAG = "Globals";

    private ProgressDialog dialog;
    private static Globals mInstance;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        MultiDex.install(this);
        MobileAds.initialize(this, getString(R.string.admob_app_id));
        super.onCreate();
        mInstance = this;
    }

    public void dialogShow(Context context) {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Please wait.....");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void dialogShow(Context context, String percentage) {
        dialog = new ProgressDialog(context);
        dialog.setMessage(percentage);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void dialogDismiss() {
        dialog.dismiss();
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    /**
     * permission
     */
    public boolean hasPermission(String perm) {
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
        } else {
            return true;
        }
    }

    public boolean canAccessStorage() {
        return (hasPermission(permission.WRITE_EXTERNAL_STORAGE)) && (hasPermission(permission.READ_EXTERNAL_STORAGE));
    }


    /**
     * file
     */
    public static Uri getImageUri(Activity mActivity, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }

    public static String getPath(Activity mActivity, Uri uri) {
        try {
            final boolean isKitKat = VERSION.SDK_INT >= VERSION_CODES.KITKAT;
            if (isKitKat && DocumentsContract.isDocumentUri(mActivity, uri)) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                } else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(mActivity, contentUri, null, null);
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
                    final String[] selectionArgs = new String[]{split[1]};
                    return getDataColumn(mActivity, contentUri, selection, selectionArgs);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                if (isGooglePhotosUri(uri)) return uri.getLastPathSegment();
                return getDataColumn(mActivity, uri, null, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public static String getDataColumn(Activity mActivity, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = mActivity.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        return null;
    }

    /**
     * keyboard
     */
    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
            Debugger.debugE(TAG, "Exception : Try to close keyboard that is already not visible");
        }
    }

    public static void hideKeyboard(Activity activity, Button button) {
        try {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(button.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        } catch (Exception e) {
            e.printStackTrace();
            Debugger.debugE(TAG, "Exception : Try to close keyboard that is already not visible");
        }
    }


    /**
     * validation
     */
    public void toastValidateMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }


    /**
     * set font
     */
    public Typeface setTypeface(Activity activity, int fontType) {
        return Typeface.createFromAsset(getAssets(), activity.getString(fontType));
    }


    public static synchronized Globals getInstance() {
        return mInstance;
    }

}
