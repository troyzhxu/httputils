# HttpUtils

## 介绍
Http工具包，封装 OkHttp，自动解析，链式用法、异步同步、前后端通用

 * 支持异步、同步请求
 * 支持Restfull风格
 * JSON自动封装与解析
 * TCP连接池
 * 请求拦截器
 * Http2
 * 回调线程配置
 * GET|POST|PUT|DELETE
 * 文件上传下载

## 安装教程

### Maven

```
<dependency>
     <groupId>com.ejlchina</groupId>
     <artifactId>httputils</artifactId>
     <version>0.0.1</version>
</dependency>
```

### Gradle

`compile 'com.ejlchina:httputils:0.0.1'`

## 使用说明

#### 1.同步请求

所有 HttpUtils.sync(...) 均为同步请求方法

```
	// 最终路径 http://api.demo.com/users?name=Jack
	HttpResult<User, ?> result = HttpUtils.sync("http://api.demo.com/users", User.class)
			.addUrlParam("name", "Jack")
			.get();							// GET请求
			
	int status = result.getStatus();		// 得到HTTP状态码
	Headers headers = result.getHeaders();	// 得到返回头
	User user = result.getOkData();			// 得到目标数据
```

#### 2.异步请求

所有 HttpUtils.async(...) 均为异步请求方法

```
	// 最终路径为 http://api.demo.com/users/1
	HttpUtils.async("http://api.demo.com/users/{id}", User.class)
			.addPathParam("id", 1)
			.setOnSuccess((int status, Headers headers, User user) -> {
				// 请求成功处理
			})
			.get();	  // GET请求
```

#### 3.BaseUrl 配置

```
	HttpClient.setBaseUrl("http://api.demo.com");
```
在配置了 BaseUrl 之后，具体的请求便可以省略 BaseUrl 部分，例如：

```
	HttpUtils.sync("/users").get()					// http://api.demo.com/users
	
	HttpUtils.sync("/auth/signin")					// http://api.demo.com/auth/signin
			.addBodyParam("username", "Jackson")
			.addBodyParam("password", "xxxxxx")
			.post()
```
在配置了 BaseUrl 之后，仍然可以请求全路径的接口，如：

```
	HttpUtils.sync("https://www.baidu.com").get()
```

#### 4.请求方法

GET 请求（同步异步请求方法一致）

```
	HttpUtils.sync("/users").get()
```
POST 请求（同步异步请求方法一致）

```
	HttpUtils.sync("/users")
			.addJsonParam("name", "Jack")
			.addJsonParam("age", 20)
			.post()
```
PUT 请求（同步异步请求方法一致）

```
	HttpUtils.sync("/users/1")
			.addJsonParam("name", "Jack")
			.put()
```
DELETE 请求（同步异步请求方法一致）

```
	HttpUtils.sync("/users/1")
			.delete()
```

#### 5.取消异步请求

只有异步请求才可以被取消

异步请求的 get、post、put、delete方法返回一个HttpCall对象，该对象可以查看请求执行的状态，也可以取消请求

```
	HttpCall call = HttpUtils.async("/users/1")
			.setOnSuccess((int status, Headers headers, Object user) -> {
				
			})
			.get();	 // 发起  GET 请求

	call.cancel();   // 取消请求

	System.out.println("是否完成: " + call.isDone());		 // false
	System.out.println("是否取消: " + call.isCanceled());	 // true
```

#### 6.异步回调函数

只有异步请求才可以设置回调函数

```
	HttpUtils.async("/users/1", User.class, Error.class)
			.setOnSuccess((int status, Headers headers, User user) -> {
				// 成功回调,状态码在[200, 300)之间（根据 User.class 自动解析出 user 对象）
			})
			.setOnFailure((int status, Headers headers, Error error) -> {
				// 失败回调,状态码在[200, 300)之外（根据 Error.class 自动解析出 error 对象）
			})
			.setOnException((Exception e) -> {
				// 异常回调
			})
			.setOnComplete((int state) -> {
				// 完成回调，无论成功失败都会执行
			})
			.get();
```

```
	HttpUtils.async("/files/report.xlsx")
			.setOnResponse((int status, Headers headers, ResponseBody body) -> {
				// 响应回调（设置了OnResponse，就不可以再设置 OnSuccess 和 OnFailure 回调）
			})
			.setOnException((Exception e) -> {
				// 异常回调
			})
			.setOnComplete((int state) -> {
				// 完成回调，无论成功失败都会执行
			})
			.get();
```

#### 7.响应数据自动解析

HttpUtils.sync(...) 和 HttpUtils.async(...) 最多有三个参数：第一个为url字符串，第二个为响应成功数据的目标解析类型，第三个为响应失败数据的目标解析类型

异步请求成功返回数据 解析为 Book 对象

```
	HttpUtils.async("/books/1", Book.class)
			.setOnSuccess((int status, Headers headers, Book book) -> {
	
			})
			.get();
```
异步请求成功返回数据 解析为 Book 对象，请求失败返回数据 解析为 String 对象

```
	HttpUtils.async("/books/1", Book.class, String.class)
			.setOnSuccess((int status, Headers headers, Book book) -> {
	
			})
			.setOnFailure((int status, Headers headers, String error) -> {
			
			})
			.get();
```
异步请求成功返回数据 解析为 Book 列表

```
	HttpUtils.async("/books", new TypeReference<List<Book>>(){})
			.setOnSuccess((int status, Headers headers, List<Book> books) -> {

			})
			.get();
```
同步请求成功返回数据 解析为 Book 对象

```
	Book book = HttpUtils.sync("/books/1", Book.class).get().getOkData();
```
同步请求成功返回数据 解析为 Book 对象，请求失败返回数据 解析为 String 对象

```
	HttpResult<User, String> result = HttpUtils.sync("/books/1", Book.class, String.class).get();
	
	Book book = result.getOkData();
	String error = result.getFailData();
```
同步请求成功返回数据 解析为 Book 列表

```
	List<Book> books = HttpUtils.sync("/books", new TypeReference<List<Book>>(){})
			.get().getOkData();
```

#### 8.添加请求头

单个添加（同步异步添加方法一样）

```
	HttpUtils.sync("/orders")
			.addHeader("Access-Token", "xxxxxx")
			.addHeader("Content-Type", "application/json")
			.get();
```
多个添加（同步异步添加方法一样）

```
	Map<String, String> headers = new HashMap<>()
	headers.put("Access-Token", "xxxxxx");
	headers.put("Accept", "application/json");
	
	HttpUtils.sync("/orders")
			.addHeader(headers)
			.get();
```

#### 9.路径参数

路径参数用于替换URL字符串中的占位符

单个添加（同步异步添加方法一样）

```
	HttpUtils.sync("/shops/{shopName}/products/{productId}")
			.addPathParam("shopName", "taobao")
			.addPathParam("productId", 20)
			.get();
```
多个添加（同步异步添加方法一样）

```
	Map<String, String> params = new HashMap<>()
	params.put("shopName", "taobao");
	params.put("productId", 20);
	
	HttpUtils.sync("/shops/{shopName}/products/{productId}")
			.addPathParam(params)
			.get();
```

#### 10.查询参数

查询参数（URL参数）用于拼接在 url 字符串的 ? 之后

单个添加（同步异步添加方法一样）

```
	HttpUtils.sync("/products")
			.addUrlParam("name", "手机")
			.addUrlParam("tag", "5G")
			.get();
```
多个添加（同步异步添加方法一样）

```
	Map<String, String> params = new HashMap<>()
	params.put("name", "手机");
	params.put("tag", 5G);
	
	HttpUtils.sync("/products")
			.addUrlParam(params)
			.get();
```

#### 11.表单参数

表单参数（Budy参数）以 key=value& 的形式携带与请求报文体内

单个添加（同步异步添加方法一样）

```
	HttpUtils.sync("/signin")
			.addBodyParam("username", "Jackson")
			.addBodyParam("password", "xxxxxx")
			.post();
```
多个添加（同步异步添加方法一样）

```
	Map<String, String> params = new HashMap<>()
	params.put("username", "Jackson");
	params.put("password", "xxxxxx");
	
	HttpUtils.sync("/signin")
			.addBodyParam(params)
			.post();
```

#### 12. JSON参数

JSON参数 json 字符串的形式携带与请求报文体内

单个添加（同步异步添加方法一样）

```
	HttpUtils.sync("/signin")
			.addJsonParam("username", "Jackson")
			.addJsonParam("password", "xxxxxx")
			.post();
```
多个添加（同步异步添加方法一样）

```
	Map<String, String> params = new HashMap<>()
	params.put("username", "Jackson");
	params.put("password", "xxxxxx");
	
	HttpUtils.sync("/signin")
			.addJsonParam(params)
			.post();
```
添加JSON字符串

```
	HttpUtils.sync("/signin")
			.setRequestJson("\"username\":\"Jackson\",\"password\":\"xxxxxx\"")
			.post();
```
Java Bean 自动转 JSON

```
	Login login = new Login();
	login.setUsername("Jackson");
	login.setPassword("xxxxxx");
	
	HttpUtils.sync("/signin")
			.setRequestJson(login)
			.post();
```

#### 13. 文件参数

同步和异步添加文件方法是一样的

上传本地文件

```
	File file1 = new File("D:/1.jpg");
	File file2 = new File("D:/2.jpg");
	
	HttpUtils.sync("/upload")
			.addFileParam("image1", file1)
			.addFileParam("image2", file2)
			.post();
```
使用文件输入流上传

```
	// 获得文件的输入流
	InputStream input = ...
	
	HttpUtils.sync("/upload")
			.addFileParam("image", "jpg", input)
			.post();
```
使用文件字节数组上传

```
	// 获得文件的字节数组
	byte[] content = ...
	
	HttpUtils.sync("/upload")
			.addFileParam("image", "jpg", content)
			.post();
```
文件参数和表单参数可以一起添加

```
	File file = new File("D:/首页广告.jpg");
	
	HttpUtils.sync("/messages")
			.addBodyParam("name", "广告图")
			.addFileParam("image", file)
			.post();
```

#### 14. HttpClient 配置

```
	// HttpClient 全局配置
	HttpClient.config((Builder builder) -> {
		
		// 配置连接池 最小10个连接（不配置默认为 5）
		builder.connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));
		
		// 配置连接超时时间
		builder.connectTimeout(20, TimeUnit.SECONDS);

		// 其它配置: 拦截器、SSL、缓存、代理...
	});
```


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
