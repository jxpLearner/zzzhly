package com.supconit.zzzhly.video.controllers;

import com.alibaba.fastjson.JSONObject;
import com.supconit.mc.api.ConfigClient;
import com.supconit.mc.entity.AggregationCondition;
import com.supconit.zzzhly.common.ConfigClientLocal;
import com.supconit.zzzhly.common.httpclient.HttpClientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @auther: jxp
 * @date: 2021/3/12 15:47
 * @description:
 */

@RestController
@RequestMapping("video")
@Api(tags = "视频监控")
public class VideoController {

    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    @Value("${aggregation.videocode}")
    private String videocode;
    @Value("${aggregation.getToken}")
    private String getTokenUrl;
    @Value("${aggregation.getDataUrl}")
    private String getDataUrl;
    @Value("${aggregation.username}")
    private String userName;
    @Value("${aggregation.password}")
    private String password;

    @Autowired
    private HttpClientService httpClient;
    @Autowired
//    private ConfigClient client;
    private ConfigClientLocal client;

    @GetMapping(value = "getVideoById")
    @ResponseBody
    @ApiOperation(value = "根据视频的id获取视频信息", notes = "根据视频的id获取视频信息")
    @ApiImplicitParam(name = "id", value = "视频id", required = true)
    public Map<String, Object> getVideoById(String id) {
        Map<String, Object> map = new HashMap<>();
        String params = "{\"code\": \"" + videocode + "\",\"params\": {\"id\": \"" + id + "\"},\"result\": {\"additionalProp1\": \"\"},\"orders\": []}";
        AggregationCondition aa = JSONObject.parseObject(params, AggregationCondition.class);
        Object object = client.aggregation(aa);

        logger.error("object:" + object.toString());

        map.put("data", object);
        return map;
    }
/*

    @GetMapping(value = "getVideoById2")
    @ResponseBody
    @ApiOperation(value = "根据视频的id获取视频信息",notes = "根据视频的id获取视频信息")
    @ApiImplicitParam(name = "id",value = "视频id",required = true)
    public Map<String,Object> getVideoById2(String id){
        Map<String,Object> map = new HashMap<>();
        String token = getToken();
        if (null == token) {
            map.put("false","获取token失败");
        }
        String url = getDataUrl+token;
        String params = "{\"code\": "+videocode+",\"params\": {\"id\": "+id+"},\"result\": {\"additionalProp1\": \"\"},\"orders\": []}";
        String result = null;
        try {
            result = httpclient.doPost(url,params);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("根据id获取视频数据失败");
        }
        if(null!=result){
            JSONObject jsonObject = JSONObject.parseObject(result);
            if(1==Integer.valueOf(jsonObject.get("code").toString())){
                map.put("success",jsonObject);
            }else if(-1==Integer.valueOf(jsonObject.get("code").toString()) && jsonObject.get("msg").toString().contains("token已失效")){
                getVideoById(id);
            }
        }

        return map;
    }


    public String getToken(){
        String url = getTokenUrl+"?username="+userName+"&password="+password;
        String token = null;
        try {
            token = httpclient.doGet(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }
*/


}
