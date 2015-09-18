package com.rengatartgital.photonandroidclient.ViewUtil;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Created by Tsai on 2015/9/16.
 */
public class ImageDecodeHelper {

    static public Bitmap decodeImageToSize(Resources res,int resId,int reqWidth,int reqHeight){
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bmp_ouput=null;
        try{
            bmp_ouput=BitmapFactory.decodeResource(res, resId, options);
        }catch(OutOfMemoryError e){
            System.gc();
            try{
                bmp_ouput=BitmapFactory.decodeResource(res, resId, options);
            }catch(OutOfMemoryError e2){
                Log.e("STLog","decode image error: "+e2.getMessage());
            }
        }

        try{
            bmp_ouput=Bitmap.createScaledBitmap(bmp_ouput, reqWidth, reqHeight, false);
        }catch(OutOfMemoryError e){
            Log.e("STLog","scale image error: "+e.getMessage());
        }

        return bmp_ouput;

    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
