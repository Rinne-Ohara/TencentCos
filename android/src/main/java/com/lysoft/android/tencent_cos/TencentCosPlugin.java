package com.lysoft.android.tencent_cos;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * TencentCosPlugin
 */
public class TencentCosPlugin implements FlutterPlugin, ActivityAware {

    private MethodChannel channel;

    private Context mContext;
    private Activity mActivity;
    private Map<String, COSXMLUploadTask> taskMap = new HashMap<>();

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        mContext = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "tencent_cos");
        channel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
                String tag = (String) call.argument("tag");
                switch (call.method) {
                    case "upload":
                        uploadFile(call, result);
                        break;
                    case "cosResume":
                        cosResume(tag);
                        break;
                    case "cosPause":
                        cosPause(tag);
                        break;
                    case "cosCancel":
                        cosCancel(tag);
                        break;
                }
            }
        });
    }


    private void uploadFile(MethodCall call, MethodChannel.Result result) {
        String tag = (String) call.argument("tag");
        String region = (String) call.argument("region");
        String tmpSecretId = (String) call.argument("tmpSecretId");
        String tmpSecretKey = (String) call.argument("tmpSecretKey");
        String sessionToken = (String) call.argument("sessionToken");
        String startTime = (String) call.argument("startTime");
        String expiredTime = (String) call.argument("expiredTime");
        String bucket = (String) call.argument("bucket");
        String key = (String) call.argument("key");
        String filePath = (String) call.argument("filePath");
        CosXmlService cosXmlService = CosServiceFactory.getCosXmlService(mContext,
                region, tmpSecretId, tmpSecretKey, sessionToken,
                Long.parseLong(startTime), Long.parseLong(expiredTime), true);

        TransferManager transferManager = new TransferManager(cosXmlService, new TransferConfig.Builder().build());

        COSXMLUploadTask cosxmlUploadTask = transferManager.upload(bucket, key, filePath, null);
        taskMap.put(tag, cosxmlUploadTask);
        cosxmlUploadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, Object> params = new HashMap<>();
                        params.put("current", complete);
                        params.put("total", target);
                        JSONObject jsonObject = new JSONObject(params);
                        channel.invokeMethod("uploadProgress", jsonObject.toString());
                    }
                });

            }
        });

        cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                taskMap.remove(tag);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, Object> params = new HashMap<>();
                        params.put("isSuccess", true);
                        params.put("message", cosXmlResult.printResult());
                        JSONObject jsonObject = new JSONObject(params);
                        result.success(jsonObject.toString());
                    }
                });

            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, @Nullable CosXmlClientException e, @Nullable CosXmlServiceException e1) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, Object> params = new HashMap<>();
                        params.put("isSuccess", false);
                        String message = "";
                        if (e != null) {
                            message += e.toString();
                        }
                        if (e1 != null) {
                            message += "\n" + e1.toString();
                        }
                        params.put("message", message);
                        JSONObject jsonObject = new JSONObject(params);
                        result.success(jsonObject.toString());
                    }
                });

            }
        });
    }

    private void cosCancel(String tag) {
        if (taskMap.containsKey(tag)) {
            COSXMLUploadTask cosxmlUploadTask = taskMap.get(tag);
            cosxmlUploadTask.cancel();
        }
    }

    private void cosPause(String tag) {
        if (taskMap.containsKey(tag)) {
            COSXMLUploadTask cosxmlUploadTask = taskMap.get(tag);
            cosxmlUploadTask.pause();
        }
    }

    private void cosResume(String tag) {
        if (taskMap.containsKey(tag)) {
            COSXMLUploadTask cosxmlUploadTask = taskMap.get(tag);
            cosxmlUploadTask.resume();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        channel = null;
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        mActivity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {
        mActivity = null;
    }
}
