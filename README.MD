# 公网IPv4上传程序

## 简介

本项目是一个用于定时检查并更新腾讯云DNS解析记录的公网IP地址的工具。它可以帮助用户自动同步本地公网IP地址与腾讯云上解析记录的IP地址，确保域名解析始终指向当前的公网IP。

## 功能

- 定时检查本地公网IP地址
- 获取腾讯云DNS解析记录的IP地址
- 如果本地IP与解析记录不一致，自动更新腾讯云DNS解析记录

## 使用前提

- 已在腾讯云注册账号并获取了`secretId`和`secretKey`
- 已在腾讯云DNS服务中添加了需要更新的域名，并获取了对应的`RecordId`

## 配置

在`PublicIpv4Upload.java`文件中，需要配置以下参数：

- `SECRET_ID`：腾讯云账号的`secretId`
- `SECRET_KEY`：腾讯云账号的`secretKey`
- `DOMAIN`：需要更新的域名
- `RECORD_TYPE`：记录类型，通常为`A`
- `RECORD_LINE`：记录线路，通常为`默认`
- `RECORD_ID`：需要更新的解析记录的`RecordId`

## 使用方法

1. 克隆项目到本地
2. 在`PublicIpv4Upload.java`中配置上述参数
3. 使用Java编译并运行项目
4. `gradle build`编译后运行以下

```shell
nohup java -jar PublicIP-1.0-SNAPSHOT.jar 
```

## 定时任务

项目使用`CronUtil`库设置了一个动态定时任务，每10分钟执行一次IP检查和更新操作。

## 注意事项

- 请确保`secretId`和`secretKey`的保密性，不要泄露
- 定时任务的执行频率可以根据实际需求进行调整
- 项目使用了多个公网IP查询服务，以提高获取IP地址的可靠性

## 开发者

- linluoluo

## 免责声明

本项目仅供学习和参考使用，开发者不对因使用本项目而导致的任何问题负责。

## 许可证

本项目遵循MIT许可证。请查看LICENSE文件了解更多信息。