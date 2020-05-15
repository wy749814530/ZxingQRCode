# ZxingQRCode
基于ZXing3.0的二维码与条形码扫码封装

#Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:
```java
  allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
  }
```

#Step 2. Add the dependency

```java
  dependencies {
	 implementation 'com.github.wy749814530:ZxingQRCode:3.0.5'
  }
```
  
#Examples
```java
public class ScanQRcodeActivity extends BaseScanActivity implements ScanQRcodeLinstener {
    private String TAG = ScanQRcodeActivity.class.getSimpleName();

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
```
#Required permissions
```java
    <!--摄像机权限-->
    <uses-permission android:name="android.permission.CAMERA" />
    <!--手机震动权限-->
    <uses-permission android:name="android.permission.VIBRATE" />
    <!--读取本地图片权限-->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
