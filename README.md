# HttpUtils

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ejlchina/httputils/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ejlchina/httputils/)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://gitee.com/ejlchina-zhxu/httputils/blob/master/LICENSE)
[![Troy.Zhou](https://img.shields.io/badge/%E4%BD%9C%E8%80%85-troyzhxu-orange.svg)](https://github.com/troyzhxu)

## [自 v2.3.0 后更命名为 OkHttps 点我跳转](https://github.com/troyzhxu/okhttps)
## 本仓库已不再更新维护！！！
## HttpUtils 的功能，OkHttps 都有，并且更加强大，推荐请使用 OkHttps ！
## 新仓库地址：[https://github.com/troyzhxu/okhttps](https://github.com/troyzhxu/okhttps)


## 文档：[https://ok.zhxu.cn/](https://ok.zhxu.cn/)

## 介绍

　　HttpUtils 是近期开源的对 OkHttp 轻量封装的框架，它独创的异步预处理器，特色的标签，灵活的上传下载进度监听与过程控制功能，在轻松解决很多原本另人头疼问题的同时，设计上也力求纯粹与优雅。

 * 链式调用，一点到底
 * BaseURL、URL占位符、JSON自动封装与解析
 * 同步拦截器、异步预处理器、回调执行器
 * 文件上传下载（过程控制、进度监听）
 * TCP连接池、Http2

### 当前文档版本[2.3.0]
#### [查阅[2.1.x]点我](https://gitee.com/ejlchina-zhxu/httputils/blob/master/README-2.1.2.md) | [查阅[2.0.x]点我](https://gitee.com/ejlchina-zhxu/httputils/blob/master/README-2.0.0.md) | [查阅[1.0.x]点我](https://gitee.com/ejlchina-zhxu/httputils/blob/1.x/README.md)

## 目录

+ [安装教程](#安装教程)
  + [Maven](#maven)
  + [Gradle](#gradle)
+ [使用说明](#使用说明)
  + [1 简单示例](#1-简单示例)
    - [1.1 构建HTTP](#11-构建-http)
    - [1.2 同步请求](#12-同步请求)
    - [1.3 异步请求](#13-异步请求)
  + [2 请求方法（GET|POST|PUT|DELETE）](#2-请求方法getpostputdelete)
  + [3 解析请求结果](#3-解析请求结果)
    - [3.1 回调函数](#31-回调函数)
    - [3.2 HttpResult](#32-HttpResult)
    - [3.3 HttpCall](#33-HttpCall)
  + [4 构建HTTP任务](#4-构建HTTP任务)
  + [5 使用标签](#5-使用标签)
  + [6 配置 HTTP](#65-配置-http)
    - [6.1 设置 BaseUrl](#61-设置-baseurl)
    - [6.2 回调执行器](#62-回调执行器)
    - [6.3 配置 OkHttpClient](#63-配置-okhttpclient)
    - [6.4 并行预处理器](#64-并行预处理器)
    - [6.5 串行预处理器](#65-串行预处理器)
    - [6.6 全局回调处理](#66-全局回调处理)
    - [6.7 全局下载监听](#67-全局下载监听)
  + [7 使用 HttpUtils 类](#7-使用-httputils-类)
  + [8 文件下载](#8-文件下载)
    - [8.1 下载进度监听](#81-下载进度监听)
    - [8.2 下载过程控制](#82-下载过程控制)
    - [8.3 实现断点续传](#83-实现断点续传)
    - [8.4 实现分块下载](#84-实现分块下载)
  + [9 文件上传](#9-文件上传)
    - [9.1 上传进度监听](#91-上传进度监听)
    - [9.2 上传过程控制](#92-上传过程控制)
  + [10 执行线程自由切换（for Android）](#10-执行线程自由切换for-android)
+ [参考文档](#参考文档)
+ [联系方式](#联系方式)

## 安装教程

### Maven

```
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>httputils</artifactId>
     <version>2.3.0</version>
</dependency>
```
### Gradle

`compile 'com.ejlchina:httputils:2.3.0'`

## 使用说明

### 1 简单示例

#### 1.1 构建 HTTP

```java
HTTP http = HTTP.builder().build();
```
　　以上代码构建了一个最简单的`HTTP`实例，它拥有以下三个方法：

* `async(String url)` 开始一个异步HTTP任务
* `sync(String url)` 开始一个同步HTTP任务
* `cancel(String tag)` 根据标签批量取消HTTP任务

　　为了使用方便，在构建的时候，我们更愿意指定一个`BaseUrl`（请参见[5.1 设置 BaseUrl](#51-设置-baseurl)）:

```java
HTTP http = HTTP.builder()
        .baseUrl("http://api.demo.com")
        .build();
```
　　为了简化文档，下文中出现的`http`均是在构建时设置了`BaseUrl`的`HTTP`实例。

#### 1.2 同步请求

　　使用方法`sync(String url)`开始一个同步请求：

```java
List<User> users = http.sync("/users") // http://api.demo.com/users
        .get()                         // GET请求
        .getBody()                     // 获取响应报文体
        .toList(User.class);           // 得到目标数据
```
　　方法`sync`返回一个同步`HttpTask`，可链式使用。

#### 1.3 异步请求

　　使用方法`async(String url)`开始一个异步请求：

```java
http.async("/users/1")                //  http://api.demo.com/users/1
        .setOnResponse((HttpResult result) -> {
            // 得到目标数据
            User user = result.getBody().toBean(User.class);
        })
        .get();                       // GET请求
```
　　方法`async`返回一个异步`HttpTask`，可链式使用。

### 2 请求方法（GET|POST|PUT|DELETE）

　　同步与异步的`HttpTask`都拥有`get`、`post`、`put`与`delete`方法。不同的是：同步`HttpTask`的这些方法返回一个`HttpResult`，而异步`HttpTask`的这些方法返回一个`HttpCall`。

```java
HttpResult res1 = http.sync("/users").get();     // 同步 GET
HttpResult res2 = http.sync("/users")post();     // 同步 POST
HttpResult res3 = http.sync("/users/1").put();   // 同步 PUT
HttpResult res4 = http.sync("/users/1").delete();// 同步 DELETE
HttpCall call1 = http.async("/users").get();     // 异步 GET
HttpCall call2 = http.async("/users").post();    // 异步 POST
HttpCall call3 = http.async("/users/1").put();   // 异步 PUT
HttpCall call4 = http.async("/users/1").delete();// 异步 DELETE
```
### 3 解析请求结果

#### 3.1 回调函数

　　只有异步请求才可以设置回调函数：

```java
http.async("/users/{id}")             // http://api.demo.com/users/1
        .addPathParam("id", 1)
        .setOnResponse((HttpResult result) -> {
            // 响应回调
        })
        .setOnException((IOException e) -> {
            // 异常回调
        })
        .setOnComplete((State state) -> {
            // 完成回调，无论成功失败都会执行
        })
        .get();
```

#### 3.2 HttpResult

　　`HttpResult`是HTTP请求执行完后的结果，它是同步请求方法（ `get`、`post`、`put`、`delete`）的返回值，也是异步请求响应回调（`OnResponse`）的参数，它定义了如下方法：

* `getState()`         得到请求执行状态枚举，它有以下取值：
    * `State.CANCELED`      请求被取消
    * `State.RESPONSED`     已收到响应
    * `State.TIMEOUT`       请求超时
    * `State.NETWORK_ERROR` 网络错误
    * `State.EXCEPTION`     其它请求异常
* `getStatus()`        得到HTTP状态码
* `isSuccessful()`     是否响应成功，状态码在 [200..300) 之间
* `getHeaders()`       得到HTTP响应头
* `getHeaders(String name)` 得到HTTP响应头
* `getHeader(String name)`  得到HTTP响应头
* `getBody()`          得到响应报文体`Body`实例，它定义了如下方法（对同一个`Body`实例，以下的`toXXX()`类方法只能使用一个且仅能调用一次）：
    * `toBytes()`                     返回字节数组
    * `toByteStream()`                返回字节输入流
    * `toCharStream()`                返回字符输入流
    * `toString()`                    返回字符串
    * `toJsonObject()`                返回Json对象
    * `toJsonArray()`                 返回Json数组
    * `toBean(Class<T> type)`         返回根据type自动json解析后的JavaBean
    * `toList(Class<T> type)`         返回根据type自动json解析后的JavaBean列表
    * `toFile(String filePath)`       下载到指定路径
    * `toFile(File file)`             下载到指定文件
    * `toFolder(String dirPath)`      下载到指定目录
    * `toFolder(File dir)`            下载到指定目录
    * `getContentType()`              返回报文体的媒体类型
    * `getContentLength()`            返回报文体的字节长度
    * `cache()`                       缓存报文体，开启缓存后可重复使用`toXXX()`类方法
    * `close()`                       关闭报文体，未对报文体做任何消费时使用，比如只读取报文头
* `getError()`         执行中发生的异常，自动捕获执行请求是发生的 网络超时、网络错误 和 其它请求异常
* `close()`            关闭报文，未对报文体做任何消费时使用，比如只读取长度

　　示例，请求结果自动转Bean和List：

```java
// 自动转Bean
Order order = http.sync("/orders/1")
        .get().getBody().toBean(Order.class);
        
// 自动转List
List<Order> orders = http.sync("/orders")
        .get().getBody().toList(Order.class);
```

　　示例，下载文件到指定目录：

```java
String path = "D:/reports/2020-03-01.xlsx";    // 文件保存目录

// 同步下载
http.sync("/reports/2020-03-01.xlsx")
        .get().getBody().toFile(path);

// 异步下载
http.async("/reports/2020-03-01.xlsx")
        .setOnResponse((HttpResult result) -> {
            result.getBody().toFile(path);
        })
        .get();
```

#### 3.3 HttpCall

　　`HttpCall`对象是异步请求方法（`get`、`post`、`put`、`delete`）的返回值，与`java`的`Future`接口很像，它有如下方法：

* `cancel()` 取消本次请求，返回取消结果
* `isCanceled()` 返回请求是否被取消
* `isDone()` 返回是否执行完成，包含取消和失败
* `getResult()` 返回执行结果`HttpResult`对象，若请求未执行完，则挂起当前线程直到执行完成再返回

　　取消一个异步请求示例：

```java
HttpCall call = http.async("/users/1").get();

System.out.println(call.isCanceled());     // false

boolean success = call.cancel();           // 取消请求

System.out.println(success);               // true
System.out.println(call.isCanceled());     // true
```

### 4 构建HTTP任务

　　`HTTP`对象的`sync`与`async`方法返回一个`HttpTask`对象，该对象提供了可链式调用的`addXXX`与`setXXX`系列方法用于构建任务本身。

* `addHeader(String name, String value)`    添加请求头
* `addHeader(Map<String, String> headers)`  添加请求头

* `addPathParam(String name, Object value)` 添加路径参数：替换URL里的{name}占位符
* `addPathParam(Map<String, ?> params)`     添加路径参数：替换URL里的{name}占位符

* `addUrlParam(String name, Object value)`  添加URL参数：拼接在URL的?之后（查询参数）
* `addUrlParam(Map<String, ?> params)`      添加URL参数：拼接在URL的?之后（查询参数）

* `addBodyParam(String name, Object value)` 添加Body参数：以表单key=value&的形式放在报文体内（表单参数）
* `addBodyParam(Map<String, ?> params)`     添加Body参数：以表单key=value&的形式放在报文体内（表单参数）

* `addJsonParam(String name, Object value)` 添加Json参数：请求体为Json（支持多层结构）
* `addJsonParam(Map<String, ?> params)`     添加Json参数：请求体为Json（支持多层结构）

* `setRequestJson(Object json)`             设置请求体的Json字符串 或待转换为 Json的 JavaBean        
* `setRequestJson(Object bean, String dateFormat)` 设置请求体的Json字符串 或待转换为 Json的 JavaBean 

* `addFileParam(String name, String filePath)` 上传文件
* `addFileParam(String name, File file)` 上传文件
* `addFileParam(String name, String type, InputStream inputStream)` 上传文件
* `addFileParam(String name, String type, String fileName, InputStream input)` 上传文件
* `addFileParam(String name, String type, byte[] content)` 上传文件
* `addFileParam(String name, String type, String fileName, byte[] content)` 上传文件

* `setTag(String tag)` 为HTTP任务添加标签
* `setRange(long rangeStart)` 设置Range头信息，用于断点续传
* `setRange(long rangeStart, long rangeEnd)` 设置Range头信息，用于分块下载
* `setRange(long rangeStart, long rangeEnd)` 设置Range头信息，用于分块下载

* `bind(Object object)` 绑定一个对象，可用于实现Android里的生命周期绑定

### 5 使用标签

　　有时候我们想对HTTP任务加以分类，这时候可以使用标签功能：

```java
http.async("/users")    //（1）
        .setTag("A").get();
        
http.async("/users")    //（2）
        .setTag("A.B").get();
        
http.async("/users")    //（3）
        .setTag("B").get();
        
http.async("/users")    //（4）
        .setTag("B.C").get();
        
http.async("/users")    //（5）
        .setTag("C").get();
```
　　当使用标签后，就可以按标签批量的对HTTP任务进行取消：

```java
int count = http.cancel("B");              //（2）（3）（4）被取消（取消标签包含"B"的任务）
System.out.println(count);                 // 输出 3
```
　　同样的，只有异步HTTP任务才可以被取消。标签除了可以用来取消任务，在预处理器中它也可以发挥作用，请参见[并行预处理器](#54-并行预处理器)与[串行预处理器](#55-串行预处理器)。

### 6 配置 HTTP

#### 6.1 设置 BaseUrl

```java
HTTP http = HTTP.builder()
        .baseUrl("http://api.demo.com")    // 设置 BaseUrl
        .build();
```
　　该配置全局生效，在配置了`BaseUrl`之后，具体的请求便可以省略`BaseUrl`部分，使得代码更加简洁，例如：

```java
http.sync("/users").get()                  // http://api.demo.com/users

http.sync("/auth/signin")                  // http://api.demo.com/auth/signin
        .addBodyParam("username", "Jackson")
        .addBodyParam("password", "xxxxxx")
        .post()                            // POST请求
```
　　在配置了`BaseUrl`之后，如有特殊请求任务，仍然可以使用全路径的方式，一点都不妨碍：

```java
http.sync("https://www.baidu.com").get()
```

#### 6.2 回调执行器

　　如何想改变执行回调函数的线程时，可以配置回调执行器。例如在Android里，让所有的回调函数都在UI线程执行，则可以在构建`HTTP`时配置如下：

```java
HTTP http = HTTP.builder()
        .callbackExecutor((Runnable run) -> {
            runOnUiThread(run);            // 在UI线程执行
        })
        .build();
```
　　该配置影响所有回调。

#### 6.3 配置 OkHttpClient

　　与其他封装 OkHttp 的框架不同，HttpUtils 并不会遮蔽 OkHttp 本身就很好用的功能，如下：

```java
HTTP http = HTTP.builder()
    .config((Builder builder) -> {
        // 配置连接池 最小10个连接（不配置默认为 5）
        builder.connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));
        // 配置连接超时时间（默认10秒）
        builder.connectTimeout(20, TimeUnit.SECONDS);
        // 配置拦截器
        builder.addInterceptor((Chain chain) -> {
            Request request = chain.request();
            // 必须同步返回，拦截器内无法执行异步操作
            return chain.proceed(request);
        });
        // 其它配置: SSL、缓存、代理、事件监听...
    })
    .build();
```

#### 6.4 并行预处理器

　　预处理器（`Preprocessor`）可以让我们在请求发出之前对请求本身做一些改变，但与`OkHttp`的拦截器（`Interceptor`）不同：预处理器可以让我们**异步**处理这些问题。

　　例如，当我们想为请求任务自动添加`Token`头信息，而`Token`只能通过异步方法`requestToken`获取时，这时使用`Interceptor`就很难处理了，但可以使用预处理器轻松解决：

```java
HTTP http = HTTP.builder()
        .addPreprocessor((PreChain chain) -> {
            HttpTask<?> task = chain.getTask();// 获得当前的HTTP任务
            if (!task.isTagged("Auth")) {      // 根据标签判断该任务是否需要Token
                return;
            }
            requestToken((String token) -> {   // 异步获取 Token
                task.addHeader("Token", token);// 为任务添加头信息
                chain.proceed();               // 继续当前的任务
            });
        })
        .build();
```
　　和`Interceptor`一样，`Preprocessor`也可以添加多个。

#### 6.5 串行预处理器

　　普通预处理器都是可并行处理的，然而有时我们希望某个预处理器同时只处理一个任务。比如 当`Token`过期时我们需要去刷新获取新`Token`，而刷新`Token`这个操作只能有一个任务去执行，因为如果`n`个任务同时执行的话，那么必有`n-1`个任务刚刷新得到的`Token`可能就立马失效了，而这是我们所不希望的。

　　为了解决这个问题，HttpUtils 提供了串行预处理器，它可以让 HTTP 任务排好队，一个一个地进入预处理器：

```java
HTTP http = HTTP.builder()
        .addSerialPreprocessor((PreChain chain) -> {
            HttpTask<?> task = chain.getTask();
            if (!task.isTagged("Auth")) {
                return;
            }
            // 检查过期，若需要则刷新Token
            requestTokenAndRefreshIfExpired((String token) -> {
                task.addHeader("Token", token);            
                chain.proceed();               // 调用此方法前，不会有其它任务进入该处理器
            });
        })
        .build();
```
　　串行预处理器实现了让HTTP任务排队串行处理的功能，但值得一提的是：它并没有因此而阻塞任何线程！

#### 6.6 全局回调处理

```java
HTTP http = HTTP.builder()
        .responseListener((HttpTask<?> task, HttpResult result) -> {
            // 所有请求响应后都会走这里
            
            return true; // 返回 true 表示继续执行 task 的 OnResponse 回调，false 表示不再执行
        })
        .completeListener((HttpTask<?> task, State state) -> {
            // 所有请求执行完都会走这里
            
            return true; // 返回 true 表示继续执行 task 的 OnComplete 回调，false 表示不再执行
        })
        .exceptionListener((HttpTask<?> task, IOException error) -> {
            // 所有请求发生异常都会走这里
            
            return true; // 返回 true 表示继续执行 task 的 OnException 回调，false 表示不再执行
        })
        .build();
```

#### 6.7 全局下载监听

```java
HTTP http = HTTP.builder()
        .downloadListener((HttpTask<?> task, Download download) -> {
            // 所有下载在开始之前都会先走这里
            Ctrl ctrl = download.getCtrl();         // 下载控制器
            
        })
        .build();
```

### 7 使用 HttpUtils 类

　　类`HttpUtils`本是 1.x 版本里的最重要的核心类，由于在 2.x 版本里抽象出了`HTTP`接口，使得它的重要性已不如往昔。但合理的使用它，仍然可以带来不少便利，特别是在没有IOC容器的环境里，比如在Android开发和一些工具项目的开发中。

　　类`HttpUtils`共定义了四个静态方法：
 
* `async(String url)`  开始一个异步请求 （内容通过一个`HTTP`单例实现）
* `sync(String url)`   开始一个同步请求 （内容通过一个`HTTP`单例实现）
* `cancel(String tag)` 按标签取消请求（内容通过一个`HTTP`单例实现）
* `of(HTTP http)`      配置`HttpUtils`持有的`HTTP`实例（不调用此方法前默认使用一个没有没有经过任何配置的`HTTP`懒实例）

　　也就是说，能使用`http`实例的地方，都可以使用`HttpUtils`类，例如：

```java
// 在配置HTTP实例之前，只能使用全路径方式
List<Role> roles = HttpUtils.sync("http://api.demo.com/roles")
        .get().getBody().toList(Role.class);

// 配置HTTP实例,全局生效
HttpUtils.of(HTTP.builder()
        .baseUrl("http://api.demo.com")
        .build());

// 内部使用新的HTTP实例
List<User> users = HttpUtils.sync("/users")
        .get().getBody().toList(User.class);
```

### 8 文件下载

　　HttpUtils 并没有把文件的下载排除在常规的请求之外，同一套API，它优雅的设计使得下载与常规请求融合的毫无违和感，一个最简单的示例：

```java
http.sync("/download/test.zip")
        .get()                           // 使用 GET 方法（其它方法也可以，看服务器支持）
        .getBody()                       // 得到报文体
        .toFile("D:/download/test.zip")  // 指定下载的目录，文件名将根据下载信息自动生成
        .start();                        // 启动下载
```
　　或使用异步连接方式：

```java
http.async("/download/test.zip")
        .setOnResponse((HttpResult result) -> {
            result.getBody().toFile("D:/download/test.zip").start();
        })
        .get();
```
　　这里要说明一下：`sync`与`async`的区别在于连接服务器并得到响应这个过程的同步与异步（这个过程的耗时在大文件下载中占比极小），而`start`方法启动的下载过程则都是异步的。

#### 8.1 下载进度监听

　　就直接上代码啦，诸君一看便懂：

```java
http.sync("/download/test.zip")
        .get()
        .getBody()
        .setStepBytes(1024)   // 设置每接收 1024 个字节执行一次进度回调（不设置默认为 8192）  
 //     .setStepRate(0.01)    // 设置每接收 1% 执行一次进度回调（不设置以 StepBytes 为准）  
        .setOnProcess((Process process) -> {           // 下载进度回调
            long doneBytes = process.getDoneBytes();   // 已下载字节数
            long totalBytes = process.getTotalBytes(); // 总共的字节数
            double rate = process.getRate();           // 已下载的比例
            boolean isDone = process.isDone();         // 是否下载完成
        })
        .toFolder("D:/download/")        // 指定下载的目录，文件名将根据下载信息自动生成
 //     .toFile("D:/download/test.zip")  // 指定下载的路径，若文件已存在则覆盖
        .setOnSuccess((File file) -> {   // 下载成功回调
            
        })
        .start();
```
　　值得一提的是：由于 HttpUtils 并没有把下载做的很特别，这里设置的进度回调不只对下载文件起用作，即使对响应JSON的常规请求，只要设置了进度回调，它也会告诉你报文接收的进度（提前是服务器响应的报文有`Content-Length`头），例如：

```java
List<User> users = http.sync("/users")
        .get()
        .getBody()
        .setStepBytes(2)
        .setOnProcess((Process process) -> {
            System.out.println(process.getRate());
        })
        .toList(User.class);
```

#### 8.2 下载过程控制

　　过于简单：还是直接上代码：

```java
Ctrl ctrl = http.sync("/download/test.zip")
        .get()
        .getBody()
        .setOnProcess((Process process) -> {
            System.out.println(process.getRate());
        })
        .toFolder("D:/download/")
        .start();   // 该方法返回一个下载过程控制器
 
ctrl.status();      // 下载状态
ctrl.pause();       // 暂停下载
ctrl.resume();      // 恢复下载
ctrl.cancel();      // 取消下载（同时会删除文件，不可恢复）
```
　　无论是同步还是异步发起的下载请求，都可以做以上的控制：

```java
http.async("/download/test.zip")
        .setOnResponse((HttpResult result) -> {
            // 拿到下载控制器
            Ctrl ctrl = result.getBody().toFolder("D:/download/").start();
        })
        .get();
```

#### 8.3 实现断点续传

　　HttpUtils 对断点续传并没有再做更高层次的封装，因为这是app该去做的事情，它在设计上使各种网络问题的处理变简单的同时力求纯粹。下面的例子可以看到，HttpUtils 通过一个失败回调拿到**断点**，便将复杂的问题变得简单：

```java
http.sync("/download/test.zip")
        .get()
        .getBody()
        .toFolder("D:/download/")
        .setOnFailure((Failure failure) -> {         // 下载失败回调，以便接收诸如网络错误等失败信息
            IOException e = failure.getException();  // 具体的异常信息
            long doneBytes = failure.getDoneBytes(); // 已下载的字节数（断点），需要保存，用于断点续传
            File file = failure.getFile();           // 下载生成的文件，需要保存 ，用于断点续传（只保存路径也可以）
        })
        .start();
```
　　下面代码实现续传：

```java
long doneBytes = ...    // 拿到保存的断点
File file =  ...        // 待续传的文件

http.sync("/download/test.zip")
        .setRange(doneBytes)                         // 设置断点（已下载的字节数）
        .get()
        .getBody()
        .toFile(file)                                // 下载到同一个文件里
        .setAppended()                               // 开启文件追加模式
        .setOnSuccess((File file) -> {

        })
        .setOnFailure((Failure failure) -> {
        
        })
        .start();
```

#### 8.4 实现分块下载

　　当文件很大时，有时候我们会考虑分块下载，与断点续传的思路是一样的，示例代码：

```java
static String url = "http://api.demo.com/download/test.zip"

public static void main(String[] args) {
    long totalSize = HttpUtils.sync(url).get().getBody()
            .close()             // 因为这次请求只是为了获得文件大小，不消费报文体，所以直接关闭
            .getContentLength(); // 获得待下载文件的大小（由于未消费报文体，所以该请求不会消耗下载报文体的时间和网络流量）
    download(totalSize, 0);      // 从第 0 块开始下载
    sleep(50000);                // 等待下载完成（不然本例的主线程就结束啦）
}

static void download(long totalSize, int index) {
    long size = 3 * 1024 * 1024;                 // 每块下载 3M  
    long start = index * size;
    long end = Math.min(start + size, totalSize);
    HttpUtils.sync(url)
            .setRange(start, end)                // 设置本次下载的范围
            .get().getBody()
            .toFile("D:/download/test.zip")      // 下载到同一个文件里
            .setAppended()                       // 开启文件追加模式
            .setOnSuccess((File file) -> {
                if (end < totalSize) {           // 若未下载完，则继续下载下一块
                    download(totalSize, index + 1); 
                } else {
                    System.out.println("下载完成");
                }
            })
            .start();
}
```

### 9 文件上传

　　一个简单文件上传的示例：

```java
http.sync("/upload")
        .addFileParam("test", "D:/download/test.zip")
        .post()     // 上传发法一般使用 POST 或 PUT，看服务器支持
```
　　异步上传也是完全一样：

```java
http.async("/upload")
        .addFileParam("test", "D:/download/test.zip")
        .post()
```

#### 9.1 上传进度监听

　　HttpUtils 的上传进度监听，监听的是所有请求报文体的发送进度，示例代码：

```java
http.sync("/upload")
        .addBodyParam("name", "Jack")
        .addBodyParam("age", 20)
        .addFileParam("avatar", "D:/image/avatar.jpg")
        .setStepBytes(1024)   // 设置每发送 1024 个字节执行一次进度回调（不设置默认为 8192）  
 //     .setStepRate(0.01)    // 设置每发送 1% 执行一次进度回调（不设置以 StepBytes 为准）  
        .setOnProcess((Process process) -> {           // 上传进度回调
            long doneBytes = process.getDoneBytes();   // 已发送字节数
            long totalBytes = process.getTotalBytes(); // 总共的字节数
            double rate = process.getRate();           // 已发送的比例
            boolean isDone = process.isDone();         // 是否发送完成
        })
        .post()
```
　　咦！怎么感觉和下载的进度回调的一样？没错！HttpUtils 还是使用同一套API处理上传和下载的进度回调，区别只在于上传是在`get/post`方法之前使用这些API，下载是在`getBody`方法之后使用。很好理解：`get/post`之前是准备发送请求时段，有上传的含义，而`getBody`之后，已是报文响应的时段，当然是下载。

#### 9.2 上传过程控制

　　上传文件的过程控制就很简单，和常规请求一样，只有异步发起的上传可以取消：

```java
HttpCall call = http.async("/upload")
        .addFileParam("test", "D:/download/test.zip")
        .setOnProcess((Process process) -> {
            System.out.println(process.getRate());
        })
        .post()

call.cancel();  // 取消上传
```
　　上传就没有暂停和继续这个功能啦，应该没人有这个需求吧?

### 10 执行线程自由切换（for Android）

　　在 Android 开发中，经常会把某些代码放到特点的线程去执行，比如网络请求响应后的页面更新在主线程（UI线程）执行，而保存文件则在IO线程操作。HttpUtils 为这类问题提供了良好的方案。

　　在 **默认** 情况下，**所有回调** 函数都会 **在 IO 线程** 执行。为什么会设计如此呢？这是因为 HttpUtils 只是纯粹的 Java 领域 Http工具包，本身对 Android 不会有任何依赖，因此也不知 Android 的 UI 线程为何物。这么设计也让它在 Android 之外有更多的可能性。

　　但是在 Android 里使用  HttpUtils 的话，UI线程的问题能否优雅的解决呢？当然可以！简单粗暴的方法就是配置一个 回调执行器：

 ```java
HTTP http = HTTP.builder()
        .callbackExecutor((Runnable run) -> {
            // 实际编码中可以吧 Handler 提出来，不需要每次执行回调都重新创建
            new Handler(Looper.getMainLooper()).post(run); // 在主线程执行
        })
        .build();
```
　　上述代码便实现了让 **所有** 的 **回调函数** 都在 **主线程** 执行的目的，如：

```java
http.async("/users")
        .addBodyParam("name", "Jack")
        .setOnProcess((Process process) -> {
            // 在主线程执行
        })
        .setOnResponse((HttpResult result) -> {
            // 在主线程执行
        })
        .setOnException((Exception e) -> {
            // 在主线程执行
        })
        .setOnComplete((State state) -> {
            // 在主线程执行
        })
        .post();
```
　　但是，如果同时还想让某些回调放在IO线程，实现 **自由切换**，怎么办呢?HttpUtils 给出了非常灵活的方法，如下：

```java
http.async("/users")
        .addBodyParam("name", "Jack")
        .setOnProcess((Process process) -> {
            // 在主线程执行
        })
        .nextOnIO()          // 指定下一个回调在 IO 线程执行
        .setOnResponse((HttpResult result) -> {
            // 在 IO 线程执行
        })
        .setOnException((Exception e) -> {
            // 在主线程执行（没有指明 nextOnIO 则在回调执行器里执行）
        })
        .nextOnIO()          // 指定下一个回调在 IO 线程执行
        .setOnComplete((State state) -> {
            // 在 IO 线程执行
        })
        .post();
```
　　无论是哪一个回调，都可以使用`nextOnIO()`方法自由切换。同样，对于文件下载也是一样：

```java
http.sync("/download/test.zip")
        .get()
        .getBody()
        .setOnProcess((Process process) -> {
            // 在主线程执行
        })
        .toFolder("D:/download/")
        .nextOnIO()          // 指定下一个回调在 IO 线程执行
        .setOnSuccess((File file) -> {
            // 在 IO 线程执行
        })
        .setOnFailure((Failure failure) -> {
            // 在主线程执行
        })
        .start();
```

## 后续计划

[更多特性，请访问 OkHttps](https://gitee.com/ejlchina-zhxu/okhttps)

## 联系方式

* 邮箱：zhou.xu@ejlchina.com

## 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request
