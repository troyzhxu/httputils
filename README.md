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

```
	// 最终路径 http://api.demo.com/users?name=Jack
	HttpResult<User, ?> result = HttpUtils.sync("http://api.demo.com/users", User.class)
	
			// 设置URL参数
			.addUrlParam("name", "Jack")
			
			// 发起  GET 请求
			.get();
	
	// 得到状态码
	int status = result.getStatus();

	// 得到返回头
	Headers headers = result.getHeaders();

	// 得到目标数据
	User user = result.getOkData();
```

#### 2.异步请求

```
	// 最终路径为 http://api.demo.com/users/1
	HttpUtils.async("http://api.demo.com/users/{id}", User.class)
	
			// 设置路径参数
			.addPathParam("id", 1)
			
			// 设置回调函数
			.setOnSuccess((int status, Headers headers, User user) -> {
				// 接收到解析好的 user 对象
	
			})
			
			// 发起  GET 请求
			.get();
```


#### 3.取消请求

```
	// 只要异步请求才可以取消
	HttpCall call = HttpUtils.async("http://api.demo.com/users", new TypeReference<List<User>>(){})
			
			// 设置回调函数
			.setOnSuccess((int status, Headers headers, List<User> users) -> {
				// 接收到解析好的 users 列表
	
			})
			
			// 发起  GET 请求
			.get();

	call.cancel();   // 取消请求

	System.out.println("是否完成: " + call.isDone());		 // false
	System.out.println("是否取消: " + call.isCanceled());	 // true
```

#### 4.回调函数

```
	// 只要异步请求才可以设置回调函数
	HttpUtils.async("http://api.demo.com/users/1", User.class, Error.class)
			
			// 成功回调,状态码在[200, 300)之间（根据 User.class 自动解析出 user 对象）
			.setOnSuccess((int status, Headers headers, User user) -> {
	
			})
			// 失败回调,状态码不在[200, 300)之间（根据 Error.class 自动解析出 error 对象）
			.setOnFailure((int status, Headers headers, Error error) -> {
	
			})
			// 异常回调
			.setOnException((Exception e) -> {
	
			})
			// 完成回调，无论成功失败都会执行
			.setOnComplete((int state) -> {
	
			})
			// 发起  GET 请求
			.get();
			
			
	// 只要异步请求才可以设置回调函数
	HttpUtils.async("http://api.demo.com/users/1", User.class, Error.class)
			
			// 请求返回回调（设置了OnResponse，就不可以再设置 OnSuccess 和 OnFailure 回调）
			.setOnResponse((int status, Headers headers, ResponseBody body) -> {
	
			})
			// 异常回调
			.setOnException((Exception e) -> {
	
			})
			// 完成回调，无论成功失败都会执行
			.setOnComplete((int state) -> {
	
			})
			// 发起  GET 请求
			.get();
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
