package com.edwinkapkei.imageupload.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;

/**
 * Source: Courtesy
 */

public class AppHelper {
// --Commented out by Inspection START (4/24/2018 4:08 PM):
//    /**
//     * Turn drawable resource into byte array.
//     *
//     * @param context parent context
//     * @param id      drawable resource id
//     * @return byte array
//     */
//    public static byte[] getFileDataFromDrawable(Context context, int id) {
//        Drawable drawable = ContextCompat.getDrawable(context, id);
//        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
//        return byteArrayOutputStream.toByteArray();
//    }
// --Commented out by Inspection STOP (4/24/2018 4:08 PM)

    /**
     * Turn drawable into byte array.
     *
     * @param drawable data
     * @return byte array
     */
    public static byte[] getFileDataFromDrawable(Context context, Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
