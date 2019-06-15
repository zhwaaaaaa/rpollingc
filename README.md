# RPOLLINGC 是一个类Rpc客户端框架。基于http长链接，用于发送一个请求，然后轮询结果。
## 背景
 早期项目上使用了 [celery](https://github.com/celery/celery) 需要调用然后轮询结果。
 早期使用HttpUrlConnection + 线程池模式。使用发送请求然后sleep轮询。
 不仅性能低，占用资源极高，且混合业务代码，非常不利于维护。所以我基于netty开发了这套框架，解决了这些问题。

## 使用场景
 发送一个请求，需要轮询结果的web client

## 特性
 - 基于netty长连接，轮询和定时采用selector
 - 使用jdk Proxy 调用，结合rxjava方便的实现异步。并且也支持同步调用
 - 采用jackson 序列化和反序列化
 
## 性能
 ###client: 
 - OS:win10. 
 - CPU:INTEL I7 4710M, 16G 内存。
 ### server:
 - OS: centos6.6
 - CPU:VMWare 4核心 4G内存
 - 服务器nginx。return 30 byte body.
 - `LongConnHttpClient` qps 9000+.
 
