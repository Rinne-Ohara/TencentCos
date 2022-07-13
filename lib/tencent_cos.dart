import 'dart:async';

import 'package:flutter/services.dart';

typedef ProgressCallback = void Function(int count, int total);

///上传成功
typedef OnSuccess = Function(String message);

///上传失败
typedef OnFail = Function(String message);

class TencentCosManager {
  static const MethodChannel _channel = MethodChannel('tencent_cos');

  static Future<void> upload(
    Map<String, Object> map, {
    ProgressCallback? onSendProgress,
    OnSuccess? onSuccess,
    OnFail? onFail,
  }) async {
    _channel.setMethodCallHandler((call) => _handler(call, onSendProgress));
    final resp = await _channel.invokeMethod("upload", map) as Map<String, Object>;
    bool isSuccess = resp["isSuccess"] as bool? ?? false;
    String message = resp["message"] as String? ?? "";
    if (isSuccess) {
      onSuccess?.call(message);
    } else {
      onFail?.call(message);
    }
  }

  /// 获取原生平台回传的信息
  /// engineState：引擎状态
  /// engineDownloadProgress：引擎下载进度
  static Future<void> _handler(MethodCall call, ProgressCallback? onSendProgress) async {
    switch (call.method) {
      case 'uploadProgress':
        final params = call.arguments as Map<String, Object>;
        int current = params["current"] as int? ?? 0;
        int total = params["current"] as int? ?? 0;
        onSendProgress?.call(current, total);
        break;
    }
  }
}
