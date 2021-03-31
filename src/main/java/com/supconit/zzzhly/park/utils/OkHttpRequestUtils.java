package com.supconit.zzzhly.park.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

/**
 * okhttp工具封装
 *
 * @author daishaokun
 * @date 2019-10-12
 */
public class OkHttpRequestUtils {
    // 定义请求客户端
    private final static OkHttpClient client = new OkHttpClient();

    /**
     * get 请求
     *
     * @param url 请求URL
     * @return
     * @throws Exception
     */
    public static String doGet(String url) {
        try {
            return doGet(url, Maps.newHashMap());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * get 请求
     *
     * @param url   请求URL
     * @param query 携带参数参数
     * @return
     * @throws Exception
     */
    public static String doGet(String url, Map<String, Object> query) {

        try {
            return doGet(url, Maps.newHashMap(), query);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }

    /**
     * get 请求
     *
     * @param url    url
     * @param header 请求头参数
     * @param query  参数
     * @return
     */
    public static String doGet(String url, Map<String, Object> header, Map<String, Object> query) {

        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 创建一个 request
        Request request = builder.url(url).build();

        // 创建一个 HttpUrl.Builder
        HttpUrl.Builder urlBuilder = request.url().newBuilder();
        // 创建一个 Headers.Builder
        Headers.Builder headerBuilder = request.headers().newBuilder();

        // 装载请求头参数
        Iterator<Map.Entry<String, Object>> headerIterator = header.entrySet().iterator();
        headerIterator.forEachRemaining(e -> {
            headerBuilder.add(e.getKey(), e.getValue().toString());
        });

        // 装载请求的参数
        Iterator<Map.Entry<String, Object>> queryIterator = query.entrySet().iterator();
        queryIterator.forEachRemaining(e -> {
            urlBuilder.addQueryParameter(e.getKey(), e.getValue().toString());
        });

        // 设置自定义的 builder
        builder.url(urlBuilder.build()).headers(headerBuilder.build());

        try (Response execute = client.newCall(builder.build()).execute()) {
            return execute.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }

    /**
     * post 请求， 请求参数 并且 携带文件上传
     *
     * @param url
     * @param header
     * @param parameter
     * @param file
     * @return
     * @throws Exception
     */
    public static String doPost(String url, Map<String, String> header, Map<String, Object> parameter, File file) throws Exception {

        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 创建一个 request
        Request request = builder.url(url).build();

        // 创建一个 Headers.Builder
        Headers.Builder headerBuilder = request.headers().newBuilder();

        // 装载请求头参数
        Iterator<Map.Entry<String, String>> headerIterator = header.entrySet().iterator();
        headerIterator.forEachRemaining(e -> {
            headerBuilder.add(e.getKey(), e.getValue());
        });

        builder.headers(headerBuilder.build());


        if (null != file) {
            MultipartBody.Builder requestBuilder = new MultipartBody.Builder();

            // 状态请求参数
            Iterator<Map.Entry<String, Object>> queryIterator = parameter.entrySet().iterator();
            queryIterator.forEachRemaining(e -> {
                requestBuilder.addFormDataPart(e.getKey(), (String) e.getValue());
            });
            // application/octet-stream
            requestBuilder.addFormDataPart("uploadFiles", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
            // 设置自定义的 builder
            builder.post(requestBuilder.build());
        } else {
            FormBody.Builder requestBuilder = new FormBody.Builder();

            //// 状态请求参数
            Iterator<Map.Entry<String, Object>> queryIterator = parameter.entrySet().iterator();
            queryIterator.forEachRemaining(e -> {
                requestBuilder.add(e.getKey(), (String) e.getValue());
            });

            builder.post(requestBuilder.build());
        }


        // 然后再 build 一下
        try (Response execute = client.newCall(builder.build()).execute()) {
            return execute.body().string();
        }
    }

    /**
     * post请求，application/json请求头
     *
     * @param url
     * @param header
     * @param parameter
     * @param file
     * @return
     * @throws Exception
     */
    public static String postJSON(String url, Map<String, String> header, Map<String, Object> parameter, File file) throws Exception {

        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 创建一个 request
        Request request = builder.url(url).build();

        // 创建一个 Headers.Builder
        Headers.Builder headerBuilder = request.headers().newBuilder();

        // 装载请求头参数
        Iterator<Map.Entry<String, String>> headerIterator = header.entrySet().iterator();
        headerIterator.forEachRemaining(e -> {
            headerBuilder.add(e.getKey(), e.getValue());
        });

        builder.headers(headerBuilder.build());


        if (null != file) {
            MultipartBody.Builder requestBuilder = new MultipartBody.Builder();

            // 状态请求参数
            Iterator<Map.Entry<String, Object>> queryIterator = parameter.entrySet().iterator();
            queryIterator.forEachRemaining(e -> {
                requestBuilder.addFormDataPart(e.getKey(), (String) e.getValue());
            });
            // application/octet-stream
            requestBuilder.addFormDataPart("uploadFiles", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
            // 设置自定义的 builder
            builder.post(requestBuilder.build());
        } else {
            RequestBody body = RequestBody.create(MediaType.parse("application/json"), JSON.toJSONString(parameter));

            builder.post(body);
        }


        // 然后再 build 一下
        try (Response execute = client.newCall(builder.build()).execute()) {
            return execute.body().string();
        }
    }

    /**
     * post请求，application/json请求头
     *
     * @param url
     * @param header
     * @param body
     * @return
     * @throws Exception
     */
    public static String postJSON(String url, Map<String, String> header, String body) {

        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 创建一个 request
        Request request = builder.url(url).build();

        // 创建一个 Headers.Builder
        Headers.Builder headerBuilder = request.headers().newBuilder();

        // 装载请求头参数
        Iterator<Map.Entry<String, String>> headerIterator = header.entrySet().iterator();
        headerIterator.forEachRemaining(e -> {
            headerBuilder.add(e.getKey(), e.getValue());
        });

        builder.headers(headerBuilder.build());


        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);

        builder.post(requestBody);

        // 然后再 build 一下
        try (Response execute = client.newCall(builder.build()).execute()) {
            return execute.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }

    /**
     * post请求，application/json请求头
     *
     * @param url
     * @param body
     * @return
     * @throws Exception
     */
    public static String postJSON(String url, String body) {

        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 创建一个 request
        Request request = builder.url(url).build();

        // 创建一个 Headers.Builder
        Headers.Builder headerBuilder = request.headers().newBuilder();

        // 装载请求头参数
        builder.headers(headerBuilder.build());


        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);

        builder.post(requestBody);

        // 然后再 build 一下
        try (Response execute = client.newCall(builder.build()).execute()) {
            return execute.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 调试时候使用，打印日志
     *
     * @param url
     * @param body
     * @return
     */
    public static String postJSONDebug(String url, Map<String, String> header, String body) {
        System.out.println("请求路径：" + url);
        System.out.println("请求头：" + JSON.toJSONString(header));
        System.out.println("请求参数：" + body);

        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 创建一个 request
        Request request = builder.url(url).build();

        // 创建一个 Headers.Builder
        Headers.Builder headerBuilder = request.headers().newBuilder();

        // 装载请求头参数
        Iterator<Map.Entry<String, String>> headerIterator = header.entrySet().iterator();
        headerIterator.forEachRemaining(e -> {
            headerBuilder.add(e.getKey(), e.getValue());
        });

        builder.headers(headerBuilder.build());


        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), body);

        builder.post(requestBody);

        // 然后再 build 一下
        try (Response execute = client.newCall(builder.build()).execute()) {
            return execute.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return StringUtils.EMPTY;
    }

    /**
     * post 请求， 请求参数 并且 携带文件上传二进制流
     *
     * @param url
     * @param header
     * @param parameter
     * @param fileName  文件名
     * @param fileByte  文件的二进制流
     * @return
     * @throws Exception
     */
    public static String doPost(String url, Map<String, Object> header, Map<String, Object> parameter, String fileName, byte[] fileByte) throws Exception {
        // 创建一个请求 Builder
        Request.Builder builder = new Request.Builder();
        // 创建一个 request
        Request request = builder.url(url).build();

        // 创建一个 Headers.Builder
        Headers.Builder headerBuilder = request.headers().newBuilder();

        // 装载请求头参数
        Iterator<Map.Entry<String, Object>> headerIterator = header.entrySet().iterator();
        headerIterator.forEachRemaining(e -> {
            headerBuilder.add(e.getKey(), (String) e.getValue());
        });

        MultipartBody.Builder requestBuilder = new MultipartBody.Builder();

        // 状态请求参数
        Iterator<Map.Entry<String, Object>> queryIterator = parameter.entrySet().iterator();
        queryIterator.forEachRemaining(e -> {
            requestBuilder.addFormDataPart(e.getKey(), (String) e.getValue());
        });

        if (fileByte.length > 0) {
            // application/octet-stream
            requestBuilder.addFormDataPart("uploadFiles", fileName, RequestBody.create(MediaType.parse("application/octet-stream"), fileByte));
        }

        // 设置自定义的 builder
        builder.headers(headerBuilder.build()).post(requestBuilder.build());

        try (Response execute = client.newCall(builder.build()).execute()) {
            return execute.body().string();
        }
    }


    /**
     * post 请求  携带文件上传
     *
     * @param url
     * @param file
     * @return
     * @throws Exception
     */
    public static String doPost(String url, File file) throws Exception {
        return doPost(url, Maps.newHashMap(), Maps.newHashMap(), file);
    }

    /**
     * post 请求
     *
     * @param url
     * @param header    请求头
     * @param parameter 参数
     * @return
     * @throws Exception
     */
    public static String doPost(String url, Map<String, String> header, Map<String, Object> parameter) throws Exception {
        return doPost(url, header, parameter, null);
    }

    /**
     * post 请求
     *
     * @param url
     * @param parameter 参数
     * @return
     * @throws Exception
     */
    public static String doPost(String url, Map<String, Object> parameter) throws Exception {
        return doPost(url, Maps.newHashMap(), parameter, null);
    }
}