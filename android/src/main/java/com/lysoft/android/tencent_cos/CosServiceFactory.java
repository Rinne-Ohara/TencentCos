package com.lysoft.android.tencent_cos;

import android.content.Context;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;
import com.tencent.qcloud.core.common.QCloudClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link CosXmlService} 是您访问 COS 服务的核心类，它封装了所有 COS 服务的基础 API 方法。
 * <p>
 * 每一个{@link CosXmlService} 对象只能对应一个 region，如果您需要同时操作多个 region 的
 * Bucket，请初始化多个 {@link CosXmlService} 对象。
 * <p>
 * {@link TransferManager} 进一步封装了 {@link CosXmlService} 的上传和下载接口，当您需要
 * 上传文件到 COS 或者从 COS 下载文件时，请优先使用这个类。
 * <p>
 * Created by rickenwang on 2018/10/19.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class CosServiceFactory {
    private static final String defaultRegion = "ap-shanghai";

    private static Map<String, CosXmlService> cosXmlServiceMap = new HashMap<>();

    public static CosXmlService getCosXmlService(Context context, String region, String secretId,
                                                 String secretKey, String token, long startTime, long expiredTime, boolean refresh) {
        if (refresh) {
            cosXmlServiceMap.remove(region);
        }

        CosXmlService cosXmlService = cosXmlServiceMap.get(region);

        if (cosXmlService == null) {
            CosXmlServiceConfig cosXmlServiceConfig = getCosXmlServiceConfig(region);
            QCloudCredentialProvider qCloudCredentialProvider = getCredentialProviderWithIdAndKey(secretId, secretKey, token, startTime, expiredTime);
            cosXmlService = new CosXmlService(context, cosXmlServiceConfig, qCloudCredentialProvider);
            cosXmlServiceMap.put(region, cosXmlService);
        }

        return cosXmlService;
    }

    public static CosXmlService getCosXmlService(Context context, String secretId, String secretKey, String token, long startTime, long expiredTime, boolean refresh) {
        return getCosXmlService(context, defaultRegion, secretId, secretKey, token, startTime, expiredTime, refresh);
    }


    /**
     * 获取配置类
     */
    private static CosXmlServiceConfig getCosXmlServiceConfig(String region) {
        return new CosXmlServiceConfig.Builder()
                .setRegion(region)
                .setDebuggable(true)
                .isHttps(true)
                .builder();
    }

    /**
     * 获取QCloudCredentialProvider对象，来给 SDK 提供临时密钥
     *
     * @param secretKey 永久密钥 secretKey
     * @parma secretId 永久密钥 secretId
     */
    private static QCloudCredentialProvider getCredentialProviderWithIdAndKey(String secretId, String secretKey, String sessionToken, long startTime, long expiredTime) {
        QCloudCredentialProvider myCredentialProvider = new BasicLifecycleCredentialProvider() {
            @Override
            protected QCloudLifecycleCredentials fetchNewCredentials() throws QCloudClientException {
                return new SessionQCloudCredentials(secretId, secretKey,
                        sessionToken, startTime, expiredTime);
            }
        };
        return myCredentialProvider;
    }
}
