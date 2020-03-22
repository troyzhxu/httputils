# HttpUtils

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ejlchina/httputils/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ejlchina/httputils/)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://gitee.com/ejlchina-zhxu/httputils/blob/master/LICENSE)
[![Troy.Zhou](https://img.shields.io/badge/%E4%BD%9C%E8%80%85-ejlchina-orange.svg)](https://github.com/ejlchina)

## 介绍

　　Http工具包，封装 OkHttp，自动解析，链式用法、异步同步、前后端通用

 * 支持异步、同步请求
 * 支持Restfull风格
 * JSON自动封装与解析
 * TCP连接池
 * 请求拦截器
 * Http2
 * 回调线程配置
 * 异步预处理器
 * GET|POST|PUT|DELETE
 * 文件上传下载

### 依赖说明

 * `okhttp-3.14.7` 核心依赖，底层依赖`okio`包
 * `fastjson-1.2.67` 阿里的快速`json`解析包

### 联系方式

 * 邮箱：zhou.xu@ejlchina.com

### 当前文档版本[2.1.2]
#### [查阅[2.1.0]点我](https://gitee.com/ejlchina-zhxu/httputils/blob/master/README-2.1.0.md) | [查阅[2.0.0]点我](https://gitee.com/ejlchina-zhxu/httputils/blob/master/README-2.0.0.md) | [查阅[1.x.x]点我](https://gitee.com/ejlchina-zhxu/httputils/blob/1.x/README.md)

## 安装教程

### Maven

```
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>httputils</artifactId>
     <version>2.2.0</version>
</dependency>
```
### Gradle

`compile 'com.ejlchina:httputils:2.2.0'`

## 使用说明

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
+ [7 使用 HttpUtils 类](#7-使用-httputils-类)
+ [8 文件下载与上传](#8-文件下载与上传)
  - [8.1 下载进度监听](#81-下载进度监听)
  - [8.2 下载过程控制](#82-下载过程控制)
  - [8.3 文件断点续传](#83-文件断点续传)
  - [8.4 文件分块下载](#84-文件分块下载)
  - [8.5 上传进度监听](#85-上传进度监听)
  - [8.6 上传过程控制](#86-上传过程控制)

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
        .setOnException((Exception e) -> {
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
* `getError()`         执行中发生的异常，自动捕获执行请求是发生的 网络超时、网络错误 和 其它请求异常

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

　　`HTTP`对象的`sync`与`async`方法返回一个`HttpTask`对象，该对象提供了一系列可链式调用的`addXXX`与`setXXX`方法用于构建任务本身。

* `addHeader(String name, String value)`    添加请求头
* `addHeader(Map<String, String> headers)`  添加请求头

* `addPathParam(String name, String value)` 添加路径参数：替换URL里的{name}占位符
* `addPathParam(String name, Number value)` 添加路径参数：替换URL里的{name}占位符
* `addPathParam(Map<String, ?> params)`     添加路径参数：替换URL里的{name}占位符

* `addUrlParam(String name, String value)`  添加URL参数：拼接在URL的?之后（查询参数）
* `addUrlParam(String name, Number value)`  添加URL参数：拼接在URL的?之后（查询参数）
* `addUrlParam(Map<String, ?> params)`      添加URL参数：拼接在URL的?之后（查询参数）

* `addBodyParam(String name, String value)` 添加Body参数：以表单key=value&的形式放在报文体内（表单参数）
* `addBodyParam(String name, Number value)` 添加Body参数：以表单key=value&的形式放在报文体内（表单参数）
* `addBodyParam(Map<String, ?> params)`     添加Body参数：以表单key=value&的形式放在报文体内（表单参数）

* `addJsonParam(String name, Object value)` 添加Json参数：请求体为Json（支持多层结构）
* `addJsonParam(Map<String, ?> params)`     添加Json参数：请求体为Json（支持多层结构）

* `setRequestJson(String json)`             设置请求体的Json字符串（支持多层结构）
* `setRequestJson(Object bean)`             将依据 bean的get方法序列化为 json 字符串（支持多层结构）
* `setRequestJson(Object bean, String dateFormat)` 将依据 bean的get方法序列化为 json 字符串（支持多层结构）

* `addFileParam(String name, String filePath)` 上传文件
* `addFileParam(String name, File file)` 上传文件
* `addFileParam(String name, String type, InputStream inputStream)` 上传文件
* `addFileParam(String name, String type, String fileName, InputStream input)` 上传文件
* `addFileParam(String name, String type, byte[] content)` 上传文件
* `addFileParam(String name, String type, String fileName, byte[] content)` 上传文件

* `setTag(String tag)` 为HTTP任务添加标签
* `setRangeHeader(long rangeStart)` 设置Range头信息，用于断点续传
* `setRangeHeader(long rangeStart, long rangeEnd)` 设置Range头信息，用于分块下载

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

　　与其他封装`OkHttp`的框架不同，`HttpUtils`并不会遮蔽`OkHttp`本身就很好用的功能，如下：

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
        .addPreprocessor((Process process) -> {
            HttpTask<?> task = process.getTask();// 获得当前的HTTP任务
            if (!task.isTagged("Auth")) {        // 根据标签判断该任务是否需要Token
                return;
            }
            requestToken((String token) -> {     // 异步获取 Token
                task.addHeader("Token", token);  // 为任务添加头信息
                process.proceed();               // 继续当前的任务
            });
        })
        .build();
```
　　和`Interceptor`一样，`Preprocessor`也可以添加多个。

#### 6.5 串行预处理器

　　普通预处理器都是可并行处理的，然而有时我们希望某个预处理器同时只处理一个任务。比如 当`Token`过期时我们需要去刷新获取新`Token`，而刷新`Token`这个操作只能有一个任务去执行，因为如果`n`个任务同时执行的话，那么必有`n-1`个任务刚刷新得到的`Token`可能就立马失效了，而这是我们所不希望的。

　　为了解决这个问题，`HttpUtils`提供了串行预处理器，它可以让HTTP任务排好队，一个一个地进入预处理器：

```java
HTTP http = HTTP.builder()
        .addSerialPreprocessor((Process process) -> {
            HttpTask<?> task = process.getTask();
            if (!task.isTagged("Auth")) {
                return;
            }
            // 检查过期，若需要则刷新Token
            requestTokenAndRefreshIfExpired((String token) -> {
                task.addHeader("Token", token);            
                process.proceed();               // 调用此方法前，不会有其它任务进入该处理器
            });
        })
        .build();
```
　　串行预处理器实现了让HTTP任务排队串行处理的功能，但值得一提的是：它并没有因此而阻塞任何线程！

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

### 8 文件下载与上传

#### 8.1 下载进度监听

文档待完善，抢先体验可阅读源码

#### 8.2 下载过程控制

文档待完善，抢先体验可阅读源码

#### 8.3 文件断点续传

文档待完善，抢先体验可阅读源码

#### 8.4 文件分块下载

文档待完善，抢先体验可阅读源码

#### 8.5 上传进度监听

文档待完善，抢先体验可阅读源码

#### 8.6 上传过程控制

文档待完善，抢先体验可阅读源码

## 计划开发

* 多回调执行器配置，具体请求回调可以自由切换执行器
* 

## 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


## 码云特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  码云官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解码云上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是码云最有价值开源项目，是码云综合评定出的优秀开源项目
5.  码云官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  码云封面人物是一档用来展示码云会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
