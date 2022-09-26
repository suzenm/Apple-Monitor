## AppleMonitor

一个用 Java 实现的 Apple 线下商店库存监控工具,支持bark,dingtalk，企业微信等监控方式。

## 使用效果

![控制台日志](docs/images/console-view.png)

![Bark](docs/images/bark-view.png)

## 如何使用

1. 下载构建的产物压缩包 [releases版本](https://github.com/MoshiCoCo/Apple-Monitor/releases)
2. 解压压缩包，文件内会包含一个可执行的jar和一份config.json配置文件，以及说明文档若干。
3. 编辑config.json配置你需要监控的产品型号以及地区即可，可支持cron表达式自定义监控频率。
4. 执行命令 `java -jar apple-monitor-v0.0.1.jar`

配置文件参数示例

```json
{
  "cronExpressions": "*/10 * * * * ?",
  "barkPushUrl": "https://bark.xxx.com/push",
  "barkPushToken": "",
  "deviceCodes": [
    "MQ0D3CH/A"
  ],
  "location": "广东 深圳 南山区",
  "storeWhiteList": [
    "益田假日",
    "珠江新城",
    "天环广场"
  ]
}
```

| 值               | 含义                                           |
|-----------------|----------------------------------------------|
| cronExpressions | 执行的cron表达式                                   |
| barkPushUrl     | bark推送服务器地址                                  |
| barkPushToken   | bark token                                   |
| deviceCodes     | 需要监控的产品代码                                    |
| location        | 你所在的区域，要用苹果官网风格的地址，例如 广东 深圳 南山区 或者 重庆 重庆 XX区 |
| storeWhiteList  | 商店白名单，一个区域可能有多个商店，仅监控白名单中的商店，模糊匹配，不填则默认监控所有  |

## 支持的推送方式

- 钉钉
- bark
- 企业微信
- server酱