package com.supconit.zzzhly.common.httpclient;

import java.util.Map;

public interface HttpClientService {

	String doGet(String url) throws Exception;

	String doGet(String url, Map<String, Object> map) throws Exception;

	String doPost(String url, Map<String, Object> map) throws Exception;

	String doPost(String url, String json) throws Exception;;

	String doPost(String url, String json, String token) throws Exception;

	String doPost(String url) throws Exception;

	String doPostOfHttps(String url, String jsonObject);

	public String doGetOfHttps(String url);

	String doPostOfHttps2(String url, String token, String jsonObject);

}
