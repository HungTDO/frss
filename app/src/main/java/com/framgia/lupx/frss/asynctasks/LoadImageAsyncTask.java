package com.framgia.lupx.frss.asynctasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.framgia.lupx.frss.cache.BitmapLruCache;
import com.framgia.lupx.frss.BuildConfig;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * Created by FRAMGIA\pham.xuan.lu on 27/07/2015.
 */
public class LoadImageAsyncTask extends AsyncTask<String, Void, Void> {

    private static final int IMAGE_DESIRED_WIDTH = 150;
    private static final int IMAGE_DESIRED_HEIGHT = 150;
    private WeakReference<ImageView> imgRef;
    private Context context;
    private String url;
    private Bitmap bitmap;

    public LoadImageAsyncTask(Context context, ImageView imgV) {
        this.context = context;
        this.imgRef = new WeakReference<ImageView>(imgV);
    }

    private int inSampleSize(BitmapFactory.Options options, int W, int H) {
        final int rW = options.outWidth;
        final int rH = options.outHeight;
        int sampleSize = 1;
        if (rH > H || rW > W) {
            final int h = rH / 2;
            final int w = rW / 2;
            while ((h / sampleSize) > H && (w / sampleSize) > W) {
                sampleSize *= 2;
            }
        }
        return sampleSize;
    }

    @Override
    protected Void doInBackground(String... params) {
        url = params[0];
        if (BitmapLruCache.getInstance().get(url) != null) {
            bitmap = BitmapLruCache.getInstance().get(url);
            return null;
        }
        if (isCancelled()) {
            return null;
        }
        try {
            URL url = new URL(this.url);
            InputStream in = url.openStream();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, options);
            options.inSampleSize = inSampleSize(options, IMAGE_DESIRED_WIDTH, IMAGE_DESIRED_HEIGHT);
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeStream(url.openStream(), null, options);
            if (BuildConfig.DEBUG) {
                Log.v("BITMAP SIZE", "(W,H)=(" + bitmap.getWidth() + "," + bitmap.getHeight() + ")");
            }
            BitmapLruCache.getInstance().put(this.url, bitmap);
        } catch (IOException ex) {

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (isCancelled()) {
            return;
        }
        if (imgRef.get() != null) {
            if (bitmap != null) {
                imgRef.get().setImageBitmap(bitmap);
            }
        }
    }

}