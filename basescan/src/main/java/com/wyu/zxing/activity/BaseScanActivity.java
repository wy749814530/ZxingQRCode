package com.wyu.zxing.activity;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.wyu.zxing.R;
import com.wyu.zxing.camera.CameraManager;
import com.wyu.zxing.decoding.CaptureActivityHandler;
import com.wyu.zxing.decoding.InactivityTimer;
import com.wyu.zxing.enumc.ScanFrequency;
import com.wyu.zxing.linstener.ImageAnalyzeLinstener;
import com.wyu.zxing.linstener.ScanQRcodeLinstener;
import com.wyu.zxing.linstener.ScanResultLinstener;
import com.wyu.zxing.utils.ImageUtil;
import com.wyu.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by Administrator on 2019/10/24 0024.
 */

public class BaseScanActivity extends AppCompatActivity implements SurfaceHolder.Callback, ScanResultLinstener, View.OnClickListener {
    private ScanQRcodeLinstener mLinstener;
    private final int REQUEST_IMAGE = 112;
    private CaptureActivityHandler handler;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private boolean cameraIsShow = false;
    private SurfaceView previewView;
    private ViewfinderView viewfinderView;
    private ImageView ivFinder;
    private ImageView ivBack;
    private ImageView ivFlashlight;
    private ImageView ivPhotoAlbum;
    private TextView tvTitle;
    private TextView tvRight;
    private TextView tvDescription;
    private ScanFrequency mSpeed = ScanFrequency.MEDIUM_SPEED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 设置为无标题格式
        setContentView(R.layout.activity_base_scan);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        Log.i("BaseScanActivity", "--- onCreate ---");

        initView();
        ZXingLibrary.initDisplayOpinion(this);
        CameraManager.init(this);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    private void initView() {

        tvTitle = findViewById(R.id.tv_title);
        tvRight = findViewById(R.id.tv_right);
        tvDescription = findViewById(R.id.tv_description);

        previewView = findViewById(R.id.preview_view);
        viewfinderView = findViewById(R.id.viewfinder_view);
        ivFinder = findViewById(R.id.iv_finder);
        ivBack = findViewById(R.id.iv_back);
        ivFlashlight = findViewById(R.id.iv_flashlight);
        ivPhotoAlbum = findViewById(R.id.iv_photo_album);
        Log.i("ScanActivity", "--- onCreate ---");

        surfaceHolder = previewView.getHolder();


        ivBack.setOnClickListener(this);
        ivFlashlight.setOnClickListener(this);
        ivPhotoAlbum.setOnClickListener(this);
        tvRight.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("BaseScanActivity", "--- onResume ---" + hasSurface);

        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;
        cameraIsShow = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("BaseScanActivity", "--- onPause ---");
        cameraIsShow = false;
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("BaseScanActivity", "--- onDestroy ---");
        inactivityTimer.shutdown();
        mLinstener = null;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    @Override
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();

        if (result == null || TextUtils.isEmpty(result.getText())) {
            if (mLinstener != null) {
                mLinstener.onQrAnalyzeFailed();
            }
        } else {
            if (mLinstener != null) {
                mLinstener.onQrAnalyzeSuccess(result.getText(), barcode);
            }
        }

        if (handler != null) {
            int speed = 1000;
            if (mSpeed == ScanFrequency.HIGHT_SPEED) {
                speed = 1000;
            } else if (mSpeed == ScanFrequency.MEDIUM_SPEED) {
                speed = 2000;
            } else if (mSpeed == ScanFrequency.LOW_SPEED) {
                speed = 2500;
            }
            handler.postDelayed(() -> {
                restartScanCode();
            }, speed);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            camera = CameraManager.get().getCamera();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet, viewfinderView);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
        if (camera != null) {
            if (camera != null && CameraManager.get().isPreviewing()) {
                if (!CameraManager.get().isUseOneShotPreviewCallback()) {
                    camera.setPreviewCallback(null);
                }
                camera.stopPreview();
                CameraManager.get().getPreviewCallback().setHandler(null, 0);
                CameraManager.get().getAutoFocusCallback().setHandler(null, 0);
                CameraManager.get().setPreviewing(false);
            }
        }
    }

    protected void scanLocalPictures() {
        Intent innerIntent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            innerIntent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            innerIntent.setAction(Intent.ACTION_PICK);
        }
        innerIntent.setType("image/*");
        Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
        startActivityForResult(wrapperIntent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * 选择系统图片并解析
         */
        if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    CodeUtils.analyzeBitmap(ImageUtil.getImageAbsolutePath(this, uri), new ImageAnalyzeLinstener() {
                        @Override
                        public void onImageAnalyzeSuccess(Result result, Bitmap barcode) {
                            handleDecode(result, barcode);
                        }

                        @Override
                        public void onImageAnalyzeFailed() {
                            if (mLinstener != null) {
                                mLinstener.onQrAnalyzeFailed();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //震动
    private void playBeepSoundAndVibrate() {
        Vibrator vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(200);
    }

    /**
     * 设置title
     *
     * @param title
     */
    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    /**
     * 设置是否显示右侧菜单栏
     *
     * @param visibility
     */
    public void setMenuVisibility(boolean visibility) {
        tvRight.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    /**
     * 设置右侧Menu
     *
     * @param text
     */
    public void setMenuText(String text) {
        tvRight.setText(text);
    }

    /**
     * 设置右侧menu图片
     *
     * @param imageRes
     */
    public void setMenuImage(int imageRes) {
        tvRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, imageRes, 0);
    }

    /**
     * 设置底部描述
     *
     * @param text
     */
    public void setDescriptionText(String text) {
        tvDescription.setText(text);
    }

    /**
     * 重启开启扫码
     */
    public void restartScanCode() {
        if (cameraIsShow) {
            if (handler != null) {
                handler.quitSynchronously();
                handler = null;
            }
            if (surfaceHolder != null) {
                initCamera(surfaceHolder);
            }
        }
    }

    /**
     * 设置扫码监听
     *
     * @param linstener
     */
    public void setScanQRcodeLinstener(ScanQRcodeLinstener linstener) {
        mLinstener = linstener;
    }

    /**
     * 设置扫码频率
     *
     * @param speed
     */
    public void setScanFrequency(ScanFrequency speed) {
        mSpeed = speed;
    }

    public boolean isOpen = false;

    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.iv_back) {
            finish();
        } else if (i == R.id.iv_flashlight) {
            if (!isOpen) {
                CodeUtils.isLightEnable(true);
                isOpen = true;
                ivFlashlight.setImageResource(R.mipmap.add_scan_btn_colse);
            } else {
                CodeUtils.isLightEnable(false);
                isOpen = false;
                ivFlashlight.setImageResource(R.mipmap.add_scan_btn_opne);
            }
        } else if (i == R.id.iv_photo_album) {
            scanLocalPictures();
        } else if (i == R.id.tv_right) {
            if (mLinstener != null) {
                mLinstener.onClickMenuItem();
            }
        }
    }
}
