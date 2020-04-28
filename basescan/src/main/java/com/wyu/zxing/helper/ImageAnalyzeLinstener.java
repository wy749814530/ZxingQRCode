package com.wyu.zxing.helper;

import android.graphics.Bitmap;

import com.google.zxing.Result;

/**
 * Created by Administrator on 2019/10/24 0024.
 */

public interface ImageAnalyzeLinstener {
    void onImageAnalyzeSuccess(String result, Bitmap barcode);

    void onImageAnalyzeFailed();
}
