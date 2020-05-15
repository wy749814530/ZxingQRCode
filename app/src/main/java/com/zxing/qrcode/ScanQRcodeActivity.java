package com.zxing.qrcode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;

import com.wyu.zxing.activity.BaseScanActivity;
import com.wyu.zxing.linstener.ScanQRcodeLinstener;
import com.zxing.view.RuleAlertDialog;

/**
 * Created by Administrator on 2020/5/15 0015.
 */

public class ScanQRcodeActivity extends BaseScanActivity implements ScanQRcodeLinstener {
    private String TAG = ScanQRcodeActivity.class.getSimpleName();
    private boolean permissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置标题
        setTitle("二维码识别");
        //不显示右侧菜单
        setMenuVisibility(false);
        // 设置扫码结果回调
        setScanQRcodeLinstener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i("ScanActivity", "--- onResume ---");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !permissionDenied) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1000);
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2000);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(this.getClass().getSimpleName(), "requestCode == 1000 1");
            } else {
                boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]);
                if (showRequestPermission) {
                    permissionDenied = true;
                    Log.i(this.getClass().getSimpleName(), "requestCode == 1000 2");
                } else {
                    Log.i(this.getClass().getSimpleName(), "requestCode == 1000 3");
                    //被禁止且点了不再询问按钮
                    new RuleAlertDialog(this).builder().setCancelable(false).
                            setTitle(getString(R.string.add_wifi_tip)).
                            setMsg(getString(R.string.add_camera_per)).
                            setPositiveButton(getString(R.string.go_to_settings), v1 -> {
                                toPermissionSetting();
                                permissionDenied = false;
                            }).setNegativeButton(getString(R.string.label_cancel), v2 -> {
                    }).show();
                }

            }
        }

        if (requestCode == 2000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(this.getClass().getSimpleName(), "requestCode == 2000 1");
            } else {
                boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]);
                if (showRequestPermission) {
                    Log.i(this.getClass().getSimpleName(), "requestCode == 2000 2");
                    permissionDenied = true;
                } else {
                    Log.i(this.getClass().getSimpleName(), "requestCode == 2000 3");
                    //被禁止且点了不再询问按钮
                    new RuleAlertDialog(this).builder().setCancelable(false).
                            setTitle(getString(R.string.add_wifi_tip)).
                            setMsg(getString(R.string.add_camera_per)).
                            setPositiveButton(getString(R.string.go_to_settings), v1 -> {
                                toPermissionSetting();
                                permissionDenied = false;
                            }).setNegativeButton(getString(R.string.label_cancel), v2 -> {
                    }).show();
                }

            }
        }
    }

    /**
     * 跳转到权限设置界面
     */
    public void toPermissionSetting() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onQrAnalyzeFailed() {
        Log.i(TAG, "== 无法识别的二维码或条形码 ==");
    }

    @Override
    public void onClickMenuItem() {
        Log.i(TAG, "== onClickMenuItem ==");
    }

    @Override
    public void onQrAnalyzeSuccess(String result, Bitmap barcode) {
        Log.i(TAG, "== 识别的二维码或条形码成功 ==" + result);
    }
}
