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

 * `okhttp` 核心依赖，底层依赖`okio`包
 * `fastjson` 阿里的快速`json`解析包

### 联系方式

 * 邮箱：zhou.xu@ejlchina.com

### 当前文档版本 2.1.0 
#### [查阅 2.0.0 点我跳转](https://gitee.com/ejlchina-zhxu/httputils/blob/master/README-2.0.0.md) | [查阅 1.x.x 点我跳转](https://gitee.com/ejlchina-zhxu/httputils/blob/1.x/README.md)

## 安装教程

### Maven

```
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>httputils</artifactId>
     <version>2.1.1</version>
</dependency>
```
### Gradle

`compile 'com.ejlchina:httputils:2.1.1'`

## 使用说明

+ [1 简单示例](#1-简单示例)
  - [1.1 构建-http](#11-构建-http)
  - [1.2 同步请求](#12-同步请求)
  - [1.3 异步请求](#13-异步请求)
+ [2 请求方法](#2-请求方法)
  - [2.1 GET](#21-GET)
  - [2.2 POST](#22-POST)
  - [2.3 PUT](#23-PUT)
  - [2.4 DELETE](#24-DELETE)
+ [3 解析执行结果](#3-解析执行结果)
  - [3.1 回调函数](#31-回调函数)
  - [3.2 HttpResult](#32-HttpResult)
  - [3.3 HttpCall](#33-HttpCall)
+ [4 构建HTTP任务](#4-构建HTTP任务)
  - [4.1 添加请求头](#41-添加请求头)
  - [4.2 添加路径参数](#42-添加路径参数)
  - [4.3 添加查询参数](#43-添加查询参数)
  - [4.4 添加表单参数](#44-添加表单参数)
  - [4.5 添加Json参数](#45-添加Json参数)
  - [4.6 添加文件参数](#46-添加文件参数)
  - [4.7 添加标签](#47-添加标签)
+ [5 配置 HTTP](#5-配置-http)
  - [5.1 设置 BaseUrl](#51-设置-baseurl)
  - [5.2 回调执行器](#52-回调执行器)
  - [5.3 配置 OkHttpClient](#53-配置-okhttpclient)
  - [5.4 并行预处理器](#54-并行预处理器)
  - [5.5 串行预处理器](#55-串行预处理器)

### 1 简单示例

#### 1.1 构建 HTTP

```java
HTTP http = HTTP.builder().build();
```
　　`HTTP`对象有以下三个方法：

* `async(String urlPath)` 开始一个异步HTTP任务
* `sync(String urlPath)` 开始一个同步HTTP任务
* `cancel(String tag)` 根据标签批量取消HTTP任务

　　为了简化文档，下文中出现的`http`均是已构建好的`HTTP`对象。

#### 1.2 同步请求

　　使用方法`sync(String url)`开始一个同步请求：

```java
// 最终路径 http://api.demo.com/users?name=Jack
User user = http.sync("http://api.demo.com/users")
        .addUrlParam("name", "Jack")                // 添加查询参数
        .get()                                      // 发送GET请求
        .getBody()                                  // 获取响应报文体
        .toBean(User.class);                        // 得到目标数据
```
　　方法`sync`返回一个同步`HttpTask`，可链式使用。

#### 1.3 异步请求

　　使用方法`async(String url)`开始一个异步请求：

```java
// 最终路径为 http://api.demo.com/users/1
http.async("http://api.demo.com/users/{id}")
        .addPathParam("id", 1)
        .setOnResponse((HttpResult result) -> {
            // 得到目标数据
            User user = result.getBody().toBean(User.class);
        })
        .get();          // GET请求
```
　　方法`async`返回一个异步`HttpTask`，可链式使用。

### 2 请求方法

　　同步与异步的`HttpTask`都拥有`get`、`post`、`put`与`delete`方法。不同的是：同步`HttpTask`的这些方法返回一个`HttpResult`，而异步`HttpTask`的这些方法返回一个`HttpCall`。

#### 2.1 GET

```java
HttpResult result = http.sync("http://api.demo.com/users").get();     // 同步 GET

HttpCall call = http.async("http://api.demo.com/users")
        .setOnResponse((HttpResult result) -> {
        
        }).get();                                                     // 异步 GET
```
#### 2.2 POST

```java
HttpResult result = http.sync("http://api.demo.com/users")
        .addBodyParam("name", "Jack")
        .addBodyParam("age", 20)
        .post();                                                      // 同步 POST

HttpCall call = http.async("http://api.demo.com/users")
        .addBodyParam("name", "Jack")
        .addBodyParam("age", 20)
        .setOnResponse((HttpResult result) -> {
        
        }).post();                                                    // 异步 POST
```
#### 2.3 PUT

```java
HttpResult result = http.sync("http://api.demo.com/users/1")
        .addJsonParam("name", "Jack")
        .put();                                                       // 同步 PUT

HttpCall call = http.async("http://api.demo.com/users/1")
        .addJsonParam("name", "Jack")
        .setOnResponse((HttpResult result) -> {
        
        })
        .put();                                                       // 异步 PUT
```
#### 2.4 DELETE

```java
HttpResult result = http.sync("http://api.demo.com/users/1").delete();// 同步 DELETE

HttpCall call = http.async("http://api.demo.com/users/1")
        .setOnResponse((HttpResult result) -> {
        
        })
        .delete();                                                    // 异步 DELETE
```
### 3 解析执行结果

#### 3.1 回调函数

　　只有异步请求才可以设置回调函数：

```java
http.async("http://api.demo.com/users/1")
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
　　所有的同步请求方法均返回一个 HttpResult 对象，所有的异步请求方法均返回一个 HttpCall 对象。

#### 3.2 HttpResult

　　`HttpResult`对象是HTTP请求执行完后的结果，它是同步请求方法（ `get`、`post`、`put`、`delete`）的返回值，也是异步请求响应回调（`OnResponse`）的参数，它有如下方法：

* `getState()`         得到请求执行状态枚举，它有以下取值：
    * `State.CANCELED`      请求被取消
    * `State.RESPONSED`     已收到响应
    * `State.TIMEOUT`       请求超时
    * `State.NETWORK_ERROR` 网络错误
    * `State.EXCEPTION`     其它请求异常
* `getStatus()`        得到HTTP状态码
* `isSuccessful()`     是否响应成功，状态码在 [200..300) 之间
* `getHeaders()`       得到HTTP响应头
* `getBody()`          得到响应报文体`Body`对象，它有如下方法：
    * `toBytes()`                     返回字节数组
    * `toByteStream()`                返回字节输入流
    * `toCharStream()`                返回字符输入流
    * `toString()`                    返回字符串
    * `toJsonObject()`                返回Json对象
    * `toJsonArray()`                 返回Json数组
    * `toBean(Class<T> type)`         返回根据type自动json解析后的JavaBean
    * `toBean(TypeReference<T> type)` 返回根据type自动json解析后的JavaBean
    * `toList(Class<T> type)`         返回根据type自动json解析后的JavaBean列表
    * `toFile(String filePath)`       下载到指定路径并返回保存后的文件
    * `toFile(File file)`             下载到指定文件并返回保存后的文件
    * `getContentType()`              返回报文体的媒体类型
    * `getContentLength()`            返回报文体的字节长度
    * 对同一个`Body`对象，以上`toXXX()`类方法只能使用一个且仅能使用一次
* `getError()`         执行中发生的异常，自动捕获执行请求是发生的 网络超时、网络错误 和 其它请求异常

　　例如，下载文件到指定目录：

```java
String path = "D:/reports/2020-03-01.xlsx";    // 文件保存目录

// 同步下载
http.sync("http://api.demo.com/reports/2020-03-01.xlsx")
        .get().getBody().toFile(path);

// 异步下载
http.async("http://api.demo.com/reports/2020-03-01.xlsx")
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
HttpCall call = http.async("http://api.demo.com/users/1").get();

System.out.println(call.isCanceled());     // false

boolean success = call.cancel();           // 取消请求

System.out.println(success);               // true
System.out.println(call.isCanceled());     // true
```

### 4 构建HTTP任务

　　`HTTP`对象的`sync`与`async`方法返回一个`HttpTask`对象，该对象提供了一系列可链式使用的`addXXX`与`setXXX`方法用于构建任务本身。

#### 4.1 添加请求头

　　单个添加（同步异步添加方法一样）：

```java
http.sync("http://api.demo.com/orders")
        .addHeader("Token", "xxxxxx")
        .addHeader("Accept", "application/json")
        .get();
```
　　多个添加（同步异步添加方法一样）：

```java
Map<String, String> headers = new HashMap<>()
headers.put("Token", "xxxxxx");
headers.put("Accept", "application/json");

http.sync("http://api.demo.com/orders")
        .addHeader(headers)
        .get();
```

#### 4.2 添加路径参数

　　路径参数用于替换URL字符串中的占位符。

　　单个添加（同步异步添加方法一样）：

```java
http.sync("http://api.demo.com/shops/{shopName}/products/{productId}")
        .addPathParam("shopName", "taobao")
        .addPathParam("productId", 20)
        .get();
```
　　多个添加（同步异步添加方法一样）：

```java
Map<String, String> params = new HashMap<>()
params.put("shopName", "taobao");
params.put("productId", 20);

http.sync("http://api.demo.com/shops/{shopName}/products/{productId}")
        .addPathParam(params)
        .get();
```

#### 4.3 添加查询参数

　　查询参数（URL参数）用于拼接在 url 字符串的 ? 之后。

　　单个添加（同步异步添加方法一样）：

```java
http.sync("http://api.demo.com/products")
        .addUrlParam("name", "手机")
        .addUrlParam("type", "5G")
        .get();
```
　　多个添加（同步异步添加方法一样）：

```java
Map<String, String> params = new HashMap<>()
params.put("name", "手机");
params.put("type", "5G");

http.sync("http://api.demo.com/products")
        .addUrlParam(params)
        .get();
```

#### 4.4 添加表单参数

　　表单参数（Body参数）以 key=value& 的形式携带与请求报文体内。

　　单个添加（同步异步添加方法一样）：

```java
http.sync("http://api.demo.com/signin")
        .addBodyParam("username", "Jackson")
        .addBodyParam("password", "xxxxxx")
        .post();
```
　　多个添加（同步异步添加方法一样）：

```java
Map<String, String> params = new HashMap<>()
params.put("username", "Jackson");
params.put("password", "xxxxxx");

http.sync("http://api.demo.com/signin")
        .addBodyParam(params)
        .post();
```

#### 4.5 添加Json参数

　　JSON 参数最终以 json 字符串的形式携带与请求报文体内。

　　单个添加（同步异步添加方法一样）：

```java
http.sync("http://api.demo.com/signin")
        .addJsonParam("username", "Jackson")
        .addJsonParam("password", "xxxxxx")
        .post();
```
　　多个添加（同步异步添加方法一样）：

```java
Map<String, Object> params = new HashMap<>()
params.put("username", "Jackson");
params.put("password", "xxxxxx");

http.sync("http://api.demo.com/signin")
        .addJsonParam(params)
        .post();
```
　　直接设置JSON字符串：

```java
http.sync("http://api.demo.com/signin")
        .setRequestJson("\"username\":\"Jackson\",\"password\":\"xxxxxx\"")
        .post();
```
　　JavaBean 自动转 JSON：

```java
Login login = new Login();
login.setUsername("Jackson");
login.setPassword("xxxxxx");

http.sync("http://api.demo.com/signin")
        .setRequestJson(login)
        .post();
```

#### 4.6 添加文件参数

　　上传本地文件：

```java
File file1 = new File("D:/1.jpg");
File file2 = new File("D:/2.jpg");

http.sync("http://api.demo.com/upload")
        .addFileParam("image1", file1)
        .addFileParam("image2", file2)
        .post();
```
　　使用文件输入流上传：

```java
// 获得文件的输入流
InputStream input = ...

http.sync("http://api.demo.com/upload")
        .addFileParam("image", "jpg", input)
        .post();
```
　　使用文件字节数组上传：

```java
// 获得文件的字节数组
byte[] content = ...

http.sync("http://api.demo.com/upload")
        .addFileParam("image", "jpg", content)
        .post();
```
　　文件参数和表单参数可以一起添加：

```java
File file = new File("D:/首页广告.jpg");

http.sync("http://api.demo.com/messages")
        .addBodyParam("name", "广告图")
        .addFileParam("image", file)
        .post();
```

#### 4.7 添加标签

　　有时候我们想对HTTP任务加以分类，这时候可以使用标签功能：

```java
http.async("http://api.demo.com/users")    //（1）
        .setTag("A")
        .get();
        
http.async("http://api.demo.com/users")    //（2）
        .setTag("A.B")
        .get();
        
http.async("http://api.demo.com/users")    //（3）
        .setTag("B")
        .get();
        
http.async("http://api.demo.com/users")    //（4）
        .setTag("B.C")
        .get();
        
http.async("http://api.demo.com/users")    //（5）
        .setTag("C")
        .get();
```
　　当使用标签后，就可以按标签批量的对HTTP任务进行取消：

```java
int count = http.cancel("B");              //（2）（3）（4）被取消（取消标签包含"B"的任务）
System.out.println(count);                 // 输出 3
```
　　同样的，只有异步HTTP任务才可以被取消。标签除了可以用来取消任务，在预处理器中它也可以发挥作用，请参见[并行预处理器](#54-并行预处理器)与[串行预处理器](#55-串行预处理器)。

### 5 配置 HTTP

#### 5.1 设置 BaseUrl

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

#### 5.2 回调执行器

　　如何想改变执行回调函数的线程时，可以配置回调执行器。例如在Android里，让所有的回调函数都在UI线程执行，则可以在构建`HTTP`时配置如下：

```java
HTTP http = HTTP.builder()
        .callbackExecutor((Runnable run) -> {
            runOnUiThread(run);            // 在UI线程执行
        })
        .build();
```
　　该配置影响的回调为：`OnResponse`、`OnException`和`OnComplete`。

#### 5.3 配置 OkHttpClient

　　与其他封装`OkHttp`的框架不同，`HttpUtils`并不会遮蔽`OkHttp`本身就很好用的功能，如下：

```java
HTTP http = HTTP.builder()
    .config((Builder builder) -> {
        // 配置连接池 最小10个连接（不配置默认为 5）
        builder.connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));
        // 配置连接超时时间
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

#### 5.4 并行预处理器

　　预处理器（`Preprocessor`）可以让我们在请求发出之前对请求本身做一些改变，但与`OkHttp`的拦截器（`Interceptor`）不同的是：预处理器可以让我们异步处理这些问题。

　　例如，当我们想为请求任务自动添加`Token`头信息，而`Token`只能通过异步方法`requestToken`获取时，这时使用`Interceptor`就很难处理了，但我们可以使用预处理器轻松解决：

```java
HTTP http = HTTP.builder()
        .addPreprocessor((Process process) -> {
            HttpTask<?> task = process.getTask();// 获得当前的HTTP任务
            if (!task.tagMatched("Auth")) {      // 根据标签判断该任务是否需要Token
                return;
            }
            requestToken((String token) -> {     // 异步获取 Token
                task.addHeader("Token", token);  // 为任务添加 Token头信息
                process.proceed();               // 继续当前的任务
            });
        })
        .build();
```
　　和`Interceptor`一样，`Preprocessor`也可以添加多个。

#### 5.5 串行预处理器

　　普通预处理器都是可并行处理的，然而有时我们希望某个预处理器同时只处理一个任务。比如 当`Token`过期时我们需要去刷新获取新`Token`，而刷新`Token`这个操作只能有一个任务去执行，因为如果`n`个任务同时执行的话，那么必有`n-1`个任务刚刷新得到的`Token`可能就立马失效了，而这是我们所不希望的。

　　为了解决这个问题，`HttpUtils`提供了串行预处理器，它可以让HTTP任务排好队，一个一个地进入预处理器：

```java
HTTP http = HTTP.builder()
        .addSerialPreprocessor((Process process) -> {
            HttpTask<?> task = process.getTask();
            if (!task.tagMatched("Auth")) {
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
