package com.supconit.zzzhly.common.httpclient.iml;

import com.alibaba.fastjson.JSONObject;
import com.supconit.zzzhly.common.httpclient.HttpClientService;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Service
public class HttpClientServiceImpl implements HttpClientService {

	private static final Logger logger = LoggerFactory.getLogger(HttpClientServiceImpl.class);
	@Autowired
	private CloseableHttpClient httpClient;

	@Autowired
	private RequestConfig config;

	/**
	 * 不带参数的get请求，如果状态码为200，则返回body，如果不为200，则返回null
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	@Override
	public String doGet(String url) throws Exception {
		logger.info("获取数据url:" + url);
		// 声明 http get 请求
		HttpGet httpGet = new HttpGet(url);
		// 装载配置信息
		httpGet.setConfig(config);
		httpGet.addHeader("Accept","application/json");
		httpGet.addHeader("Content-Type","application/json;charset=UTF-8");

		// 发起请求
		CloseableHttpResponse response = this.httpClient.execute(httpGet);

		logger.info("getStatusCode():" +response.getStatusLine().getStatusCode()+"");
		// 判断状态码是否为200
		if (response.getStatusLine().getStatusCode() == 200) {
			// 返回响应体的内容
			return EntityUtils.toString(response.getEntity(), "UTF-8");
		}
		return null;
	}

	/**
	 * 带参数的get请求，如果状态码为200，则返回body，如果不为200，则返回null
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	@Override
	public String doGet(String url, Map<String, Object> map) throws Exception {
		URIBuilder uriBuilder = new URIBuilder(url);

		if (map != null) {
			// 遍历map,拼接请求参数
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				uriBuilder.setParameter(entry.getKey(), entry.getValue().toString());
			}
		}

		// 调用不带参数的get请求
		return this.doGet(uriBuilder.build().toString());

	}

	/**
	 * 带参数的post请求
	 * 
	 * @param url
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@Override
	public String doPost(String url, Map<String, Object> map) throws Exception {
		// 声明httpPost请求
		HttpPost httpPost = new HttpPost(url);
		// 加入配置信息
		httpPost.setConfig(config);

		// 判断map是否为空，不为空则进行遍历，封装from表单对象
		if (map != null) {
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				list.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
			}
			// 构造from表单对象
			UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(list, "UTF-8");

			// 把表单放到post里
			httpPost.setEntity(urlEncodedFormEntity);
		}

		// 发起请求
		CloseableHttpResponse response = this.httpClient.execute(httpPost);
		return EntityUtils.toString(response.getEntity(), "UTF-8");
	}

	/**
	 * 不带参数post请求
	 *
	 *
	 *
	 * @param url
	 * @return
	 * @throws Exception
	 */
	@Override
	public String doPost(String url) throws Exception {
//		return this.doPost(url, null);
		return this.doPost(url, "");
	}

	/**
	 * 带参数和body的post请求
	 *
	 * @param url
	 * @param
	 * @return
	 * @throws Exception
	 */
	@Override
	public String doPost(String url, String json) throws Exception {
		// 声明httpPost请求
		HttpPost httpPost = new HttpPost(url);
		// 加入配置信息
		httpPost.setConfig(config);

		//设置请求头header参数
		httpPost.setHeader("Content-Type","application/json;charset=utf-8");

		//请求对象参数
		StringEntity requestEntiry = new StringEntity(json,ContentType.APPLICATION_JSON);
		httpPost.setEntity(requestEntiry);

		// 发起请求
		CloseableHttpResponse response = this.httpClient.execute(httpPost);
		return EntityUtils.toString(response.getEntity(), "UTF-8");
	}


	/**
	 * 带参数和body的post请求
	 *
	 * @param url
	 * @param
	 * @return
	 * @throws Exception
	 */
	@Override
	public String doPost(String url, String json,String token) throws Exception {
		// 声明httpPost请求
		HttpPost httpPost = new HttpPost(url);
		// 加入配置信息
		httpPost.setConfig(config);

		//设置请求头header参数
		httpPost.setHeader("Content-Type","application/json;charset=utf-8");
		httpPost.setHeader("token",token);

		//请求对象参数
		StringEntity requestEntiry = new StringEntity(json,ContentType.APPLICATION_JSON);
		httpPost.setEntity(requestEntiry);

		// 发起请求
		CloseableHttpResponse response = this.httpClient.execute(httpPost);
		return EntityUtils.toString(response.getEntity(), "UTF-8");
	}

	/**
	 * https取消ssl认证
	 */
	private static void trustAllHosts() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
			}
			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
			}
		} };
		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL", "SunJSSE");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(NoopHostnameVerifier.INSTANCE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String doPostOfHttps2(String url,String token,String jsonObject){

		try{
			trustAllHosts();
			URL realUrl = new URL(url);
			// 打开和URL之间的连接，线上做了代理用http,线下直接访问外网https
			HttpsURLConnection con = (HttpsURLConnection)realUrl.openConnection();
//			HttpURLConnection con = (HttpURLConnection)realUrl.openConnection();

			con.setRequestMethod("POST");//请求post方式
			con.setUseCaches(false); // Post请求不能使用缓存
			con.setDoInput(true);// 设置是否从HttpURLConnection输入，默认值为 true
			con.setDoOutput(true);// 设置是否使用HttpURLConnection进行输出，默认值为 false

			//设置header内的参数 connection.setRequestProperty("健, "值");
			con.setRequestProperty("token",token);
			con.setRequestProperty("host","30.37.32.251");
			con.setRequestProperty("Content-Type","application/json");
			con.setRequestProperty("X-Requested-With","XMLHttpRequest");
			con.setRequestProperty("Accept-Encoding","gzip,deflate,br");

			// 建立实际的连接
			con.connect();
			// 得到请求的输出流对象
			OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream(),"UTF-8");
			writer.write(jsonObject);
			writer.flush();
			// 获取服务端响应，通过输入流来读取URL的响应
			String encoding = con.getContentEncoding();
			InputStream is = con.getInputStream();
			if(encoding!=null && encoding.contains("gzip")){
				is = new GZIPInputStream(con.getInputStream());
			}
//			InputStream is = new GZIPInputStream(con.getInputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuffer sbf = new StringBuffer();
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
				sbf.append("\r\n");
			}
			reader.close();
			// 关闭连接
			con.disconnect();
			// 打印读到的响应结果
			System.out.println("运行结束："+JSONObject.parseObject(sbf.toString()).toJSONString());
			logger.info("获取的AP数据："+JSONObject.parseObject(sbf.toString()).toJSONString());

			return JSONObject.parseObject(sbf.toString()).toJSONString();

		}catch (Exception e){
			System.out.println(e.getMessage());
			logger.info("----------------获取数据失败-------------------");
		}

		return "";
	}

	@Override
	public String doPostOfHttps(String url,String jsonObject){

//		trustAllHosts();
		URL realUrl = null;
		try {
			realUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			logger.info("----------url错误-------");
		}

		// 打开和URL之间的连接,线上做了代理用http,线下直接访问外网https
//		HttpsURLConnection con = null;
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection)realUrl.openConnection();
//			con = (HttpsURLConnection)realUrl.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("--------打开连接失败----------");
		}
		try {
			con.setRequestMethod("POST");//请求post方式
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
		con.setUseCaches(false); // Post请求不能使用缓存
		con.setDoInput(true);// 设置是否从HttpURLConnection输入，默认值为 true
		con.setDoOutput(true);// 设置是否使用HttpURLConnection进行输出，默认值为 false
		con.setConnectTimeout(10000);
		con.setReadTimeout(10000);

		//设置header内的参数 connection.setRequestProperty("健, "值");
//		con.setRequestProperty("token",token);
		con.setRequestProperty("host","30.37.32.251");
		con.setRequestProperty("Content-Type","application/json");
		con.setRequestProperty("X-Requested-With","XMLHttpRequest");
		con.setRequestProperty("Accept-Encoding","gzip,deflate,br");
		con.setRequestProperty("Content-Ecoding","application/octet-stream");

		// 建立实际的连接
		try {
			con.connect();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("--------连接失败----------");
		}
		// 得到请求的输出流对象
		OutputStreamWriter writer = null;
		StringBuffer sbf = new StringBuffer();
		try {
			writer = new OutputStreamWriter(con.getOutputStream(),"UTF-8");
			writer.write(jsonObject);
			writer.flush();
			// 获取服务端响应，通过输入流来读取URL的响应
			String encoding = con.getContentEncoding();
			InputStream is = con.getInputStream();
			if(encoding!=null && encoding.contains("gzip")){
				is = new GZIPInputStream(con.getInputStream());
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
				sbf.append("\r\n");
			}
			reader.close();
			// 关闭连接
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("--------读取数据失败----------");
		}
		con.disconnect();
		// 打印读到的响应结果
//		System.out.println("运行结束："+JSONObject.parseObject(sbf.toString()).toJSONString());
//		logger.info("获取的AP数据："+JSONObject.parseObject(sbf.toString()).toJSONString());
		logger.info("获取的AP数据 success");

		return JSONObject.parseObject(sbf.toString()).toJSONString();
	}

	@Override
	public String doGetOfHttps(String url){

		try{
			trustAllHosts();
			URL realUrl = new URL(url);
			// 打开和URL之间的连接，线上做了代理用http,线下直接访问外网https
			HttpsURLConnection con = (HttpsURLConnection)realUrl.openConnection();

			con.setRequestMethod("GET");//请求post方式
//			con.setUseCaches(false); // Post请求不能使用缓存
//			con.setDoInput(true);// 设置是否从HttpURLConnection输入，默认值为 true
//			con.setDoOutput(true);// 设置是否使用HttpURLConnection进行输出，默认值为 false
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);

			//设置header内的参数 connection.setRequestProperty("健, "值");
			con.setRequestProperty("Content-Type","application/json");

			// 建立实际的连接
			con.connect();
			// 获取服务端响应，通过输入流来读取URL的响应
			InputStream is = con.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			StringBuffer sbf = new StringBuffer();
			String strRead = null;
			while ((strRead = reader.readLine()) != null) {
				sbf.append(strRead);
				sbf.append("\r\n");
			}
			reader.close();
			// 关闭连接
			con.disconnect();
			// 打印读到的响应结果
//			System.out.println("运行结束："+JSONObject.parseObject(sbf.toString()).toJSONString());
			logger.info("获取数据成功");

			return JSONObject.parseObject(sbf.toString()).toJSONString();

		}catch (Exception e){
			System.out.println(e.getMessage());
			logger.info("----------------获取数据失败-------------------");
		}

		return "";
	}


}
