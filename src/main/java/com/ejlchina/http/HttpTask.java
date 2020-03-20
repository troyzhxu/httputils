package com.ejlchina.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.ejlchina.http.internal.HttpClient;
import com.ejlchina.http.internal.HttpException;
import com.ejlchina.http.HttpResult.State;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;


/**
 * Created by 周旭（Troy.Zhou） on 2020/3/11.
 */
@SuppressWarnings("unchecked")
public abstract class HttpTask<C extends HttpTask<?>> {

    private static final MediaType TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static String PATH_PARAM_REGEX = "[A-Za-z0-9_\\-/]*\\{[A-Za-z0-9_\\-]+\\}[A-Za-z0-9_\\-/]*";

    protected HttpClient httpClient;
    private String urlPath;
    private Map<String, String> headers;
    private Map<String, String> pathParams;
    private Map<String, String> urlParams;
    private Map<String, String> bodyParams;
    private Map<String, String> jsonStrParams;
    private Map<String, Integer> jsonIntParams;
    private Map<String, FilePara> files;
    private String requestJson;
    protected boolean nothrow;
    protected String tag;

    
    public HttpTask(HttpClient httpClient, String url) {
    	this.httpClient = httpClient;
    	this.urlPath = url;
    }

    /**
     * 获取请求任务的URL地址
     * @return URL地址
     */
    public String getUrl() {
    	return urlPath;
    }
    
    /**
     * 获取请求任务的标签
     * @return 标签
     */
    public String getTag() {
		return tag;
	}
    
    /**
     * 标签匹配
     * 判断任务标签与指定的标签是否匹配（包含指定的标签）
     * @param tag 标签
     * @return 是否匹配
     */
    public boolean isTagged(String tag) {
    	if (this.tag != null && tag != null) {
    		return this.tag.contains(tag);
    	}
    	return false;
    }
    
    /**
     * 获取请求任务的头信息
     * @return 头信息
     */
    public Map<String, String> getHeaders() {
		return headers;
	}

	/**
     * 设置在发生异常时不向上抛出，设置后：
     * 异步请求可以在异常回调内捕获异常，同步请求在返回结果中找到该异常
     * @return HttpTask 实例
     */
    public C nothrow() {
    	this.nothrow = true;
		return (C) this;
    }
    
    /**
     * 为请求任务添加标签
     * @param tag 标签
     * @return HttpTask 实例
     */
    public C setTag(String tag) {
    	this.tag = tag;
    	return (C) this;
    }

	/**
     * 添加请求头
     * @param name 请求头名
     * @param value 请求头值
     * @return HttpTask 实例
     */
	public C addHeader(String name, String value) {
    	if (name != null && value != null) {
    		if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put(name, value);
    	}
        return (C) this;
    }

    /**
     * 添加请求头
     * @param headers 请求头集合
     * @return HttpTask 实例
     */
    public C addHeader(Map<String, String> headers) {
    	if (headers != null) {
    		if (this.headers == null) {
            	this.headers = new HashMap<>();
            }
            this.headers.putAll(headers);
    	}
        return (C) this;
    }
    
    /**
     * 路径参数：替换URL里的{name}
     * @param name 参数名
     * @param value 参数值
     * @return HttpTask 实例
     **/
    public C addPathParam(String name, String value) {
    	if (name != null && value != null) {
	        if (pathParams == null) {
	            pathParams = new HashMap<>();
	        }
	        pathParams.put(name, value);
    	}
        return (C) this;
    }
    
    /**
     * 路径参数：替换URL里的{name}
     * @param name 参数名
     * @param value 参数值
     * @return HttpTask 实例
     **/
    public C addPathParam(String name, Number value) {
    	if (value != null) {
    		addPathParam(name, value.toString());
    	}
    	return (C) this;
    }

    /**
     * 路径参数：替换URL里的{name}
     * @param params 参数集合
     * @return HttpTask 实例
     **/
    public C addPathParam(Map<String, ?> params) {
        if (params != null) {
            if (pathParams == null) {
                pathParams = new HashMap<>();
            }
            params.forEach((String name, Object value) -> {
            	if (name != null && value != null) {
            		pathParams.put(name, value.toString());
            	}
            });
        }
        return (C) this;
    }
    
    /**
     * URL参数：拼接在URL后的参数
     * @param name 参数名
     * @param value 参数值
     * @return HttpTask 实例
     **/
    public C addUrlParam(String name, String value) {
    	if (name != null && value != null) {
	        if (urlParams == null) {
	            urlParams = new HashMap<>();
	        }
	        urlParams.put(name, value);
    	}
        return (C) this;
    }

    /**
     * URL参数：拼接在URL后的参数
     * @param name 参数名
     * @param value 参数值
     * @return HttpTask 实例
     **/
    public C addUrlParam(String name, Number value) {
    	if (value != null) {
    		addUrlParam(name, value.toString());
    	}
    	return (C) this;
    }

    /**
     * URL参数：拼接在URL后的参数
     * @param params 参数集合
     * @return HttpTask 实例
     **/
    public C addUrlParam(Map<String, ?> params) {
        if (params != null) {
            if (urlParams == null) {
                urlParams = new HashMap<>();
            }
            params.forEach((String name, Object value) -> {
            	if (name != null && value != null) {
            		urlParams.put(name, value.toString());
            	}
            });
        }
        return (C) this;
    }

    /**
     * Body参数：放在Body里的参数
     * @param name 参数名
     * @param value 参数值
     * @return HttpTask 实例
     **/
    public C addBodyParam(String name, String value) {
    	if (name != null && value != null) {
	        if (bodyParams == null) {
	            bodyParams = new HashMap<>();
	        }
	        bodyParams.put(name, value);
    	}
        return (C) this;
    }

    /**
     * Body参数：放在Body里的参数
     * @param name 参数名
     * @param value 参数值
     * @return HttpTask 实例
     **/
    public C addBodyParam(String name, Number value) {
    	if (value != null) {
    		addBodyParam(name, value.toString());
    	}
        return (C) this;
    }

    /**
     * Body参数：放在Body里的参数
     * @param params 参数集合
     * @return HttpTask 实例
     **/
    public C addBodyParam(Map<String, ?> params) {
    	if (params != null) {
            if (bodyParams == null) {
            	bodyParams = new HashMap<>();
            }
            params.forEach((String name, Object value) -> {
            	if (name != null && value != null) {
            		bodyParams.put(name, value.toString());
            	}
            });
        }
        return (C) this;
    }

    /**
     * Json参数：请求体为Json，只支持单层Json
     * 若请求json为多层结构，请使用setRequestJson方法
     * @param name JSON键名
     * @param value JSON键值
     * @return HttpTask 实例
     */
    public C addJsonParam(String name, String value) {
    	if (name != null && value != null) {
	        if (jsonStrParams == null) {
	            jsonStrParams = new HashMap<>();
	        }
	        jsonStrParams.put(name, value);
    	}
        return (C) this;
    }

    /**
     * Json参数：请求体为Json，只支持单层Json
     * 若请求json为多层结构，请使用setRequestJson方法
     * @param name JSON键名
     * @param value JSON键值
     * @return HttpTask 实例
     */
    public C addJsonParam(String name, Number value) {
    	if (value != null) {
    		addJsonParam(name, value.toString());
    	}
    	return (C) this;
    }
    
    /**
     * Json参数：请求体为Json，只支持单层Json
     * 若请求json为多层结构，请使用setRequestJson方法
     * @param params JSON键值集合
     * @return HttpTask 实例
     */
    public C addJsonParam(Map<String, Object> params) {
    	if (params != null) {
            if (jsonStrParams == null) {
            	jsonStrParams = new HashMap<>();
            }
            params.forEach((String name, Object value) -> {
            	if (name != null && value != null) {
            		jsonStrParams.put(name, value.toString());
            	}
            });
        }
        return (C) this;
    }

    /**
     * 请求体为json
     * @param json JSON字符串
     * @return HttpTask 实例
     **/
    public C setRequestJson(String json) {
        if (json != null) {
            requestJson = json;
        }
        return (C) this;
    }

    /**
     * 请求体为json
     * @param bean Java对象，将依据 bean的get方法序列化为 json 字符串
     * @return HttpTask 实例
     **/
    public C setRequestJson(Object bean) {
        if (bean != null) {
            requestJson = JSON.toJSONString(bean);
        }
        return (C) this;
    }

    /**
     * 请求体为json
     * @param bean Java对象，将跟换 bean的get方法序列化程 json 字符串
     * @param dateFormat 序列化json时对日期类型字段的处理格式
     * @return HttpTask 实例
     **/
    public C setRequestJson(Object bean, String dateFormat) {
        if (bean != null) {
            requestJson = JSON.toJSONStringWithDateFormat(bean, dateFormat);
        }
        return (C) this;
    }
    
    /**
     * 添加文件参数
     * @param name 参数名
     * @param file 文件
     * @return HttpTask 实例
     */
    public C addFileParam(String name, File file) {
        if (name != null && file != null && file.exists()) {
            String filename = file.getName();
            String type = filename.substring(filename.lastIndexOf(".") + 1);
            try {
				addFileParam(name, type, filename, new FileInputStream(file));
			} catch (FileNotFoundException e) {
				throw new HttpException("上传的文件不存在！", e);
			}
        }
        return (C) this;
    }

    /**
     * 添加文件参数
     * @param name 参数名
     * @param type 文件类型: 如 png、jpg、jpeg 等
     * @param inputStream 文件输入流
     * @return HttpTask 实例
     */
    public C addFileParam(String name, String type, InputStream inputStream) {
    	String fileName = System.currentTimeMillis() + "." + type;
    	return addFileParam(name, type, fileName, inputStream);
    }
    
    /**
     * 添加文件参数
     * @param name 参数名
     * @param type 文件类型: 如 png、jpg、jpeg 等
     * @param fileName 文件名
     * @param input 文件输入流
     * @return HttpTask 实例
     */
    public C addFileParam(String name, String type, String fileName, InputStream input) {
        if (name != null && input != null) {
            byte[] content = null;
			try {
				Buffer buffer = new Buffer();
				content = buffer.readFrom(input).readByteArray();
				buffer.close();
			} catch (IOException e) {
				throw new HttpException("读取文件输入流出错：", e);
			}
            addFileParam(name, type, fileName, content);
        }
        return (C) this;
    }
    
    /**
     * 添加文件参数
     * @param name 参数名
     * @param type 文件类型: 如 png、jpg、jpeg 等
     * @param content 文件内容
     * @return HttpTask 实例
     */
    public C addFileParam(String name, String type, byte[] content) {
    	String fileName = System.currentTimeMillis() + "." + type;
    	return addFileParam(name, type, fileName, content);
    }
    
    /**
     * 添加文件参数
     * @param name 参数名
     * @param type 文件类型: 如 png、jpg、jpeg 等
     * @param fileName 文件名
     * @param content 文件内容
     * @return HttpTask 实例
     */
    public C addFileParam(String name, String type, String fileName, byte[] content) {
        if (name != null && content != null) {
            if (files == null) {
                files = new HashMap<>();
            }
            files.put(name, new FilePara(type, fileName, content));
        }
        return (C) this;
    }

    class FilePara {
    	
    	String type;
    	String fileName;
    	byte[] content;

    	FilePara(String type, String fileName, byte[] content) {
			this.type = type;
			this.fileName = fileName;
			this.content = content;
		}
    	
    }
    
    protected Call prepareCall(String method) {
    	assertNotConflict("GET".equals(method));
        Request.Builder builder = new Request.Builder()
        		.url(buildUrlPath());
        buildHeaders(builder);
		switch (method) {
        case "GET":
        	builder.get();
        	break;
        case "POST":
        	builder.post(buildRequestBody());
        	break;
        case "PUT":
        	builder.put(buildRequestBody());
        	break;
        case "DELETE":
        	builder.delete(buildRequestBody());
        	break;
        }
		return httpClient.callRequest(builder.build());
	}
   
    
    private void buildHeaders(Request.Builder builder) {
        if (headers != null) {
            for (String name : headers.keySet()) {
                String value = headers.get(name);
                if (value != null) {
                    builder.addHeader(name, value);
                }
            }
        }
    }

    
    protected String toString(Response response) throws IOException {
		String body = null;
		ResponseBody rbody = response.body();
		if (rbody != null) {
			body = rbody.string();
		}
		return body;
	}

	protected State toState(Exception e) {
		if (e instanceof SocketTimeoutException) {
		    return State.TIMEOUT;
		} else if (e instanceof UnknownHostException || e instanceof ConnectException) {
			return State.NETWORK_ERROR;
		} else if ("Canceled".equals(e.getMessage())) {
			return State.CANCELED;
		}
		return State.EXCEPTION;
	}

	
    private RequestBody buildRequestBody() {
        if (jsonStrParams != null || jsonIntParams != null) {
            requestJson = buildRequestJson();
        }
        if (files != null) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            if (bodyParams != null) {
                for (String name : bodyParams.keySet()) {
                    String value = bodyParams.get(name);
                    builder.addFormDataPart(name, value);
                }
            }
            for (String name : files.keySet()) {
                FilePara file = files.get(name);
                MediaType type = httpClient.getMediaType(file.type);
                builder.addFormDataPart(name, file.fileName, RequestBody.create(type, file.content));
            }
            return builder.build();
        } else if (requestJson != null) {
            return RequestBody.create(TYPE_JSON, requestJson);
        } else {
            FormBody.Builder builder = new FormBody.Builder();
            if (bodyParams != null) {
	            for (String name : bodyParams.keySet()) {
	                String value = bodyParams.get(name);
	                builder.add(name, value);
	            }
            }
            return builder.build();
        }
    }

    private String buildRequestJson() {
        String json = "{";
        if (jsonStrParams != null) {
            for (String name : jsonStrParams.keySet()) {
                String value = jsonStrParams.get(name);
                if (value != null) {
                    json += "\"" + name + "\":\"" + value + "\",";
                } else {
                    json += "\"" + name + "\":null,";
                }
            }
        }
        if (jsonIntParams != null) {
            for (String name : jsonIntParams.keySet()) {
                json += "\"" + name + "\":" + jsonIntParams.get(name) + ",";
            }
        }
        return json.substring(0, json.length() - 1) + "}";
    }

    private String buildUrlPath() {
    	String url = urlPath;
        if (url == null || url.trim().isEmpty()) {
            throw new HttpException("url 不能为空！");
        }
        if (pathParams != null) {
            for (String name : pathParams.keySet()) {
                String target = "{" + name + "}";
                if (url.contains(target)) {
                    url = url.replace(target, pathParams.get(name));
                } else {
                    throw new HttpException("pathParameter [ " + name + " ] 不存在于 url [ " + urlPath + " ]");
                }
            }
        }
        if (url.matches(PATH_PARAM_REGEX)) {
            throw new HttpException("url 里有 pathParameter 没有设置，你必须先调用 addPathParam 为其设置！");
        }
        if (urlParams != null) {
            if (url.contains("?")) {
                if (!url.endsWith("?")) {
                    url = url.trim();
                    if (url.lastIndexOf("=") < url.lastIndexOf("?") + 2) {
                        throw new HttpException("url 格式错误，'？' 后没有发现 '='");
                    }
                    if (!url.endsWith("&")) {
                        url += "&";
                    }
                }
            } else {
                url += "?";
            }
            for (String name : urlParams.keySet()) {
                url += name + "=" + urlParams.get(name) + "&";
            }
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }


    protected void assertNotConflict(boolean isGetRequest) {
        if (isGetRequest) {
            if (requestJson != null) {
                throw new HttpException("GET 请求 不能调用 setRequestJson 方法！");
            }
            if (jsonStrParams != null || jsonIntParams != null) {
                throw new HttpException("GET 请求 不能调用 addJsonParam 方法！");
            }
            if (bodyParams != null) {
                throw new HttpException("GET 请求 不能调用 addBodyParam 方法！");
            }
            if (files != null) {
                throw new HttpException("GET 请求 不能调用 addFileParam 方法！");
            }
        }
        if (requestJson != null) {
            if (jsonStrParams != null || jsonIntParams != null) {
                throw new HttpException("方法 addJsonParam 与 setRequestJson 不能同时调用！");
            }
            if (bodyParams != null) {
                throw new HttpException("方法 addBodyParam 与 setRequestJson 不能同时调用！");
            }
            if (files != null) {
                throw new HttpException("方法 addFileParam 与 setRequestJson 不能同时调用！");
            }
        }
        if (jsonStrParams != null || jsonIntParams != null) {
            if (bodyParams != null) {
                throw new HttpException("方法 addBodyParam 与 addJsonParam 不能同时调用！");
            }
            if (files != null) {
                throw new HttpException("方法 addFileParam 与 addJsonParam 不能同时调用！");
            }
        }
    }

}