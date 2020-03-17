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

## 安装教程

### Maven

```
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>httputils</artifactId>
     <version>2.0.0</version>
</dependency>
```

### Gradle

`compile 'com.ejlchina:httputils:2.0.0'`

## 使用说明

### 1 简单示例

#### 1.1 构建 HttpClient

```java
	HttpClient http = HttpClient.builder().build();		
```
　　为了简化文档，下文中出现的`http`均是已构建好的`HttpClient`对象。

#### 1.2 同步请求

　　使用方法`sync(String url)`发起同步请求

```java
	// 最终路径 http://api.demo.com/users?name=Jack
	User user = http.sync("http://api.demo.com/users")
			.addUrlParam("name", "Jack")				// 添加查询参数
			.get()										// 发送GET请求
			.getBody()									// 获取响应报文体
			.toBean(User.class);						// 得到目标数据
```

#### 1.3 异步请求

　　使用方法`async(String url)`发起异步请求

```java
	// 最终路径为 http://api.demo.com/users/1
	http.async("http://api.demo.com/users/{id}")
			.addPathParam("id", 1)
			.setOnResponse((HttpResult result) -> {
				// 得到目标数据
				User user = result.getBody().toBean(User.class);
			})
			.get();	  	// GET请求
```
### 2 请求方法

#### 2.1 GET

```java
	http.sync("http://api.demo.com/users").get()		// 同步 GET 请求

	http.async("http://api.demo.com/users").get()		// 异步 GET 请求
```
#### 2.2 POST

```java
	http.sync("/users")
			.addJsonParam("name", "Jack")
			.addJsonParam("age", 20)
			.post()										// 同步 POST 请求

	http.async("http://api.demo.com/users")
			.addJsonParam("name", "Jack")
			.addJsonParam("age", 20)
			.post()										// 异步 POST 请求
```
#### 2.3 PUT

```java
	http.sync("http://api.demo.com/users/1")
			.addJsonParam("name", "Jack")
			.put()										// 同步 PUT 请求

	http.async("http://api.demo.com/users/1")
			.addJsonParam("name", "Jack")
			.put()										// 异步 PUT 请求
```
#### 2.4 DELETE

```java
	http.sync("http://api.demo.com/users/1").delete()	// 同步 DELETE 请求
	
	http.async("http://api.demo.com/users/1").delete()	// 异步 DELETE 请求
```
### 3 获取请求结果

#### 3.1 请求回调

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

　　`HttpResult` 对象是HTTP请求执行完后的结果，它是同步请求方法（ `get`、`post`、`put`、`delete`）的返回值，也是异步请求响应回调（`OnResponse`）的参数，它有如下方法：

* `getState()` 		得到请求执行状态枚举，它有以下取值：
	* `State.CANCELED` 请求被取消
	* `State.RESPONSED` 已收到响应
	* `State.TIMEOUT` 请求超时
	* `State.NETWORK_ERROR` 网络错误
	* `State.EXCEPTION` 其它请求异常
* `getStatus()` 	得到HTTP状态码
* `getHeaders()` 	得到HTTP响应头
* `getBody()` 		得到响应报文体 `ResultBody` 对象，它有如下方法：
    * `toBytes()` 						返回字节数组
    * `toByteStream()` 					返回字节输入流
    * `toCharStream()` 					返回字符输入流
    * `toString()` 						返回字符串
    * `toJsonObject()` 					返回Json对象
    * `toJsonArray()` 					返回Json数组
    * `toBean(Class<T> type)` 			返回根据type自动json解析后的JavaBean
    * `toBean(TypeReference<T> type)`	返回根据type自动json解析后的JavaBean
    * `toFile(String filePath)` 		下载到指定路径并返回保存后的文件（下载文件时非常有用）
    * `toFile(File file)` 				下载到指定文件并返回保存后的文件（下载文件时非常有用）
    * `getContentType()`				返回报文体的媒体类型
    * `getContentLength()`				返回报文体的字节长度
* `isSuccessful()` 	是否响应成功，状态码在 [200..300) 之间
* `isRedirect()` 	是否是重定向（300、301、302、303、307、308）
* `getError()` 		执行中发生的异常，自动捕获执行请求是发生的 网络超时、网络错误 和 其它请求异常

#### 3.3 HttpCall

　　`HttpCall` 对象是异步请求方法（ `get`、`post`、`put`、`delete`）的返回值，它有如下方法：

* `cancel()` 取消本次请求
* `isCanceled()` 请求是否被取消
* `isDone()` 请求是否执行完成，包含取消和失败
* `getState()` 请求执行的状态枚举，若请求未执行完，则返回 null

　　取消一个异步请求示例：

```java
	HttpCall call = http.async("http://api.demo.com/users/1").get();

	System.out.println(call.isCanceled());	 // false
	
	call.cancel();   // 取消请求

	System.out.println(call.isCanceled());	 // true
```

### 4 构建请求任务

#### 4.1 添加请求头

单个添加（同步异步添加方法一样）

```java
	http.sync("http://api.demo.com/orders")
			.addHeader("Access-Token", "xxxxxx")
			.addHeader("Content-Type", "application/json")
			.get();
```
多个添加（同步异步添加方法一样）

```java
	Map<String, String> headers = new HashMap<>()
	headers.put("Access-Token", "xxxxxx");
	headers.put("Accept", "application/json");
	
	http.sync("http://api.demo.com/orders")
			.addHeader(headers)
			.get();
```

#### 4.2 添加路径参数

路径参数用于替换URL字符串中的占位符

单个添加（同步异步添加方法一样）

```java
	http.sync("http://api.demo.com/shops/{shopName}/products/{productId}")
			.addPathParam("shopName", "taobao")
			.addPathParam("productId", 20)
			.get();
```
多个添加（同步异步添加方法一样）

```java
	Map<String, String> params = new HashMap<>()
	params.put("shopName", "taobao");
	params.put("productId", 20);
	
	http.sync("http://api.demo.com/shops/{shopName}/products/{productId}")
			.addPathParam(params)
			.get();
```

#### 4.3 添加查询参数

查询参数（URL参数）用于拼接在 url 字符串的 ? 之后

单个添加（同步异步添加方法一样）

```java
	http.sync("http://api.demo.com/products")
			.addUrlParam("name", "手机")
			.addUrlParam("tag", "5G")
			.get();
```
多个添加（同步异步添加方法一样）

```java
	Map<String, String> params = new HashMap<>()
	params.put("name", "手机");
	params.put("tag", 5G);
	
	http.sync("http://api.demo.com/products")
			.addUrlParam(params)
			.get();
```

#### 4.4 添加表单参数

表单参数（Budy参数）以 key=value& 的形式携带与请求报文体内

单个添加（同步异步添加方法一样）

```java
	http.sync("http://api.demo.com/signin")
			.addBodyParam("username", "Jackson")
			.addBodyParam("password", "xxxxxx")
			.post();
```
多个添加（同步异步添加方法一样）

```java
	Map<String, String> params = new HashMap<>()
	params.put("username", "Jackson");
	params.put("password", "xxxxxx");
	
	http.sync("http://api.demo.com/signin")
			.addBodyParam(params)
			.post();
```

#### 4.5 添加JSON参数

JSON参数 json 字符串的形式携带与请求报文体内

单个添加（同步异步添加方法一样）

```java
	http.sync("http://api.demo.com/signin")
			.addJsonParam("username", "Jackson")
			.addJsonParam("password", "xxxxxx")
			.post();
```
多个添加（同步异步添加方法一样）

```java
	Map<String, String> params = new HashMap<>()
	params.put("username", "Jackson");
	params.put("password", "xxxxxx");
	
	http.sync("http://api.demo.com/signin")
			.addJsonParam(params)
			.post();
```
添加JSON字符串

```java
	http.sync("http://api.demo.com/signin")
			.setRequestJson("\"username\":\"Jackson\",\"password\":\"xxxxxx\"")
			.post();
```
Java Bean 自动转 JSON

```java
	Login login = new Login();
	login.setUsername("Jackson");
	login.setPassword("xxxxxx");
	
	http.sync("http://api.demo.com/signin")
			.setRequestJson(login)
			.post();
```

#### 4.6 添加文件参数

同步和异步添加文件方法是一样的

上传本地文件

```java
	File file1 = new File("D:/1.jpg");
	File file2 = new File("D:/2.jpg");
	
	http.sync("http://api.demo.com/upload")
			.addFileParam("image1", file1)
			.addFileParam("image2", file2)
			.post();
```
使用文件输入流上传

```java
	// 获得文件的输入流
	InputStream input = ...
	
	http.sync("http://api.demo.com/upload")
			.addFileParam("image", "jpg", input)
			.post();
```
使用文件字节数组上传

```java
	// 获得文件的字节数组
	byte[] content = ...
	
	http.sync("http://api.demo.com/upload")
			.addFileParam("image", "jpg", content)
			.post();
```
文件参数和表单参数可以一起添加

```java
	File file = new File("D:/首页广告.jpg");
	
	http.sync("http://api.demo.com/messages")
			.addBodyParam("name", "广告图")
			.addFileParam("image", file)
			.post();
```

### 2 配置 HttpClient

#### 2.1 BaseUrl

```java
	HttpClient http = HttpClient.builder()
			.baseUrl("http://api.demo.com")		// 设置 BaseUrl
			.build();
```
　　该配置全局生效，在配置了`BaseUr`之后，具体的请求便可以省略 BaseUrl 部分，例如：

```java
	http.sync("/users").get()					// http://api.demo.com/users
	
	http.sync("/auth/signin")					// http://api.demo.com/auth/signin
			.addBodyParam("username", "Jackson")
			.addBodyParam("password", "xxxxxx")
			.post()								// POST请求
```
　　在配置了`BaseUrl`之后，仍然可以请求全路径的接口，如：

```java
	http.sync("https://www.baidu.com").get()
```

#### 2.2 回调执行器

　　如何想改变执行回调函数的线程时，可以配置回调函数执行器。例如在Android里，让所有的回调函数都在UI线程里执行，则可以在构建`HttpClient`时配置回调执行器：

```java
	HttpClient http = HttpClient.builder()
			.callbackExecutor((Runnable run) -> {
				runOnUiThread(run);				// 在UI线程执行
			})
			.build();
```

#### 2.3 原生`OkHttpClient`配置

```java
	HttpClient http = HttpClient.builder()
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
			// 其它配置: 拦截器、SSL、缓存、代理...
		})
		.build();
```

#### 2.3 预处理器

　　预处理器（`Preprocessor`）可以让我们在请求发出之前对请求本身做一些改变，但与 OkHttp 提供的拦截器（`Interceptor`）不同的是，预处理器可以让我们异步处理这些问题。

　　例如，当我们想为请求自动添加`Token`，而有效的`Token`只能通过异步方法`checkExpirationAndRefreshToken`获取时，我们可以添加这样的预处理器：

```java
	HttpClient http = HttpClient.builder()
			.addPreprocessor((Process process) -> {
				// 异步获取 Token
				checkExpirationAndRefreshToken((String token) -> {
					// 获取当前的请求任务
					HttpTask task = process.getHttpTask();
					// 为请求任务添加 Token 头信息
					task.addHeader("Token", token);
					// 继续当前的请求任务
					process.proceed();
				});	
			})
			.build();
```

该配置全局生效

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
