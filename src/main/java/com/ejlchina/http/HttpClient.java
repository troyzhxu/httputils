package com.ejlchina.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.alibaba.fastjson.JSON;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


/**
 * Created by 周旭（Troy.Zhou） on 2020/3/11.
 * @since 0.3.4
 * 
 * @param <S> 请求成功时返回的数据类型
 * @param <F> 请求失败时返回的数据类型
 */
@SuppressWarnings("unchecked")
public abstract class HttpClient<S, F, C extends HttpClient<S, F, ?>> {

	private static final Type STR_TYPE = String.class;
    private static final MediaType TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static Map<String, String> mediaTypes;
    private static String PATH_PARAM_REGEX = "[A-Za-z0-9_\\-/]*\\{[A-Za-z0-9_\\-]+\\}[A-Za-z0-9_\\-/]*";
    private static OkHttpClient httpClient;
    private static String baseUrl;

    private String urlPath;
    protected Type okType;
    protected Type failType;
    private Map<String, String> headers;
    private Map<String, String> pathParams;
    private Map<String, String> urlParams;
    private Map<String, String> bodyParams;
    private Map<String, String> jsonStrParams;
    private Map<String, Integer> jsonIntParams;
    private Map<String, FilePara> files;
    private String requestJson;
    protected boolean nothrow;

    public HttpClient(String urlPath, Type okType, Type failType) {
    	this.urlPath = baseUrl != null ? baseUrl + urlPath : urlPath;
    	this.okType = okType;
    	this.failType = failType;
        if (httpClient == null) {
            httpClient = new Builder().build();
        }
        if (mediaTypes == null) {
        	mediaTypes = new HashMap<>();
        	mediaTypes.put("*", "application/octet-stream");
        	mediaTypes.put("png", "image/png");
        	mediaTypes.put("jpg", "image/jpeg");
        	mediaTypes.put("jpeg", "image/jpeg");
        	mediaTypes.put("wav", "audio/wav");
        	mediaTypes.put("mp3", "audio/mp3");
        	mediaTypes.put("mp4", "video/mpeg4");
        	mediaTypes.put("txt", "text/plain");
        	mediaTypes.put("xls", "application/x-xls");
        	mediaTypes.put("xml", "text/xml");
        	mediaTypes.put("apk", "application/vnd.android.package-archive");
        	mediaTypes.put("doc", "application/msword");
        	mediaTypes.put("pdf", "application/pdf");
        	mediaTypes.put("html", "text/html");
        }
    }

    /**
     * 全局配置
     */
    public static void config(Configurator configurator) {
    	Builder builder = new Builder();
    	HttpClient.baseUrl = configurator.config(builder);
        HttpClient.httpClient = builder.build();
    }
    
    /**
     * Http 配置器
     *
     */
    public static interface Configurator {
    	
    	/**
    	 * 使用 builder 配置 HttpClient
    	 * @return BaseUrl
    	 */
    	String config(Builder builder);
    	
    }
    
    /**
     * 发生异常时不抛出
     */
    public C nothrow() {
    	this.nothrow = true;
		return (C) this;
    }
    
    /**
     * 添加请求头
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
     **/
    public C addPathParam(String name, Number value) {
    	if (value != null) {
    		addPathParam(name, value.toString());
    	}
    	return (C) this;
    }

    /**
     * 路径参数：替换URL里的{name}
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
     **/
    public C addUrlParam(String name, Number value) {
    	if (value != null) {
    		addUrlParam(name, value.toString());
    	}
    	return (C) this;
    }

    /**
     * URL参数：拼接在URL后的参数
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
     **/
    public C addBodyParam(String name, Number value) {
    	if (value != null) {
    		addBodyParam(name, value.toString());
    	}
        return (C) this;
    }

    /**
     * Body参数：放在Body里的参数
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
     **/
    public C setRequestJson(String json) {
        if (json != null) {
            requestJson = json;
        }
        return (C) this;
    }

    /**
     * 请求体为json
     **/
    public C setRequestJson(Object json) {
        if (json != null) {
            requestJson = JSON.toJSONString(json);
        }
        return (C) this;
    }

    /**
     * 请求体为json
     **/
    public C setRequestJson(Object json, String dateFormat) {
        if (json != null) {
            requestJson = JSON.toJSONStringWithDateFormat(json, dateFormat);
        }
        return (C) this;
    }
    
    /**
     * 添加文件参数
     * @param name 参数名
     * @param file 文件
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
     * @param inputStream 文件输入流
     */
    public C addFileParam(String name, String type, String fileName, InputStream inputStream) {
        if (name != null && inputStream != null) {
            byte[] content = null;
			try {
				content = IOUtils.toByteArray(inputStream);
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
     * @param fileName 文件名
     * @param content 文件内容
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
        return httpClient.newCall(builder.build());
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

	protected int toState(Exception e) {
		int state = OnComplete.EXCEPTION;
		if (e instanceof SocketTimeoutException) {
		    state = OnComplete.TIMEOUT;
		} else if (e instanceof UnknownHostException || e instanceof ConnectException) {
		    state = OnComplete.NETWORK_ERROR;
		} else if ("Canceled".equals(e.getMessage())) {
		    state = OnComplete.CANCELED;
		}
		return state;
	}


    protected Object parseObject(String body, Type type) throws Exception {
        Object result = body;
        if (type != null && !type.equals(STR_TYPE) && body != null) {
            result =JSON.parseObject(body, type);
        }
        return result;
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
                MediaType type = parseFileMediaType(file.type);
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

    protected MediaType parseFileMediaType(String suffix) {
        String type = mediaTypes.get(suffix);
        if (type != null) {
            return MediaType.parse(type);
        }
        return MediaType.parse("application/octet-stream");
    }


}
