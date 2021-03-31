package com.supconit.zzzhly.park.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.supconit.mc.api.ConfigClient;
import com.supconit.zzzhly.common.ConfigClientLocal;
import com.supconit.zzzhly.park.domain.BaseReq;
import com.supconit.zzzhly.park.domain.FeeQueryReq;
import com.supconit.zzzhly.park.domain.ParkListReq;
import com.supconit.zzzhly.park.utils.OkHttpRequestUtils;
import com.supconit.zzzhly.park.utils.SignUtils;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.spring.web.json.Json;

import java.util.*;

/**
 * @auther: jxp
 * @date: 2021/3/19 14:33
 * @description: 三坪景区停车场数据接入
 */

@Controller
@RequestMapping(value = "parkData")
@Api(tags = "三平景区停车场数据接入")
public class ParkDataController {

    Logger logger = LoggerFactory.getLogger(ParkDataController.class);

    @Autowired
//    private ConfigClientLocal client;
    private ConfigClient client;

    /*@Value("$(sanping.appId)")
    private Integer appId;
    @Value("$(sanping.appSecret)")
    private String appSecret;
    @Value("$(sanping.ipAddressUrl)")
    private String ipAddressUrl;
    @Value("$(sanping.parkListUri)")
    private String parkListUri;
*/
    private static List<Map<String,Object>> parkList = new ArrayList<>();

    private final int appId = 10031;
    private final String appSecret = "9d682649d9f64faeb5e4477a8e27858e";
    private final String ipAddressUrl = "https://tsktapps.keytop.cn/unite-api/api";//接口地址

//    private final String parkListUri = "/config/platform/GetParkingLotList";//停车场列表

    @GetMapping("getSanPingAllParks")
    public void getSanPingAllParkList() {
        String uri = "/config/platform/GetParkingLotList";
        JSONObject param = new JSONObject();

        param.put("appId", appId);
        param.put("ts", System.currentTimeMillis());
        param.put("reqId", UUID.randomUUID().toString());
        param.put("serviceCode", "getParkingLotList");
        param.put("pageIndex", 1);
        param.put("pageSize", 10);
        param.put("key",SignUtils.paramsSign((JSONObject) JSON.toJSON(param), appSecret));

        Map<String, String> header = Maps.newHashMap();
        header.put("version", "1.0.0");
        String resultStr = OkHttpRequestUtils.postJSONDebug(ipAddressUrl + uri, header, JSON.toJSONString(param));
        System.out.println("请求结果：" + resultStr);

        JSONObject result = JSONObject.parseObject(resultStr);
        if(!"0".equals(result.get("resCode"))){
            logger.error("false,获取数据失败");
            return;
        }
        JSONObject jsonResult = JSONObject.parseObject(JSON.toJSONString(result.get("data")));
        int totalCount = Integer.valueOf(JSON.toJSONString(jsonResult.get("totalCount")));
        parkList = (List<Map<String,Object>>) jsonResult.get("detailList");

        List<Map> saveParkList = new ArrayList<>();
        Map<String,Object> map = new HashMap<>();
        parkList.forEach(stringObjectMap -> {
            map.put("name",stringObjectMap.get("parkName"));
            map.put("ssjq","spjq");//此处接入的停车场数据全部为三平景区的停车场
            map.put("code",stringObjectMap.get("parkId"));
            map.put("total",stringObjectMap.get("totalSpaceNum"));
            map.put("address",stringObjectMap.get("addr"));
            saveParkList.add(map);
        });

        client.save("parkDataId",true,saveParkList);
    }

    @GetMapping("getSanPingAllParks2")
    public void getSanPingAllParkList2() {
        String uri = "/config/platform/GetParkingLotList";
        ParkListReq param = new ParkListReq();
        param.setAppId(appId);
        param.setTs(System.currentTimeMillis());
        param.setReqId(UUID.randomUUID().toString());
        param.setServiceCode("getParkingLotList");
        param.setPageIndex(1);
        param.setPageSize(1000);
        param.setKey(SignUtils.paramsSign((JSONObject) JSON.toJSON(param), appSecret));

        Map<String, String> header = Maps.newHashMap();
        header.put("version", "1.0.0");
        String result = OkHttpRequestUtils.postJSONDebug(ipAddressUrl + uri, header, JSON.toJSONString(param));
        System.out.println("请求结果：" + result);
    }

    private final String parkId = "1039";

    public void queryFee() {
        String uri = "/wec/GetParkingLotInfo";
        JSONObject param = new JSONObject();
        param.put("appId", appId);
        param.put("parkId",parkId);
        param.put("serviceCode", "getParkingLotInfo");
        param.put("ts", System.currentTimeMillis());
        param.put("reqId", UUID.randomUUID().toString());
        param.put("key",SignUtils.paramsSign(param, appSecret));

        Map<String, String> header = Maps.newHashMap();
        header.put("version", "1.0.0");
        String result = OkHttpRequestUtils.postJSONDebug(ipAddressUrl + uri, header, JSON.toJSONString(param));
        System.out.println("请求结果：" + result);
    }

    /**
     * 进出记录
     */
    public void saveCarInOutInfo(){
        parkList.forEach(map -> {queryCarInOutInfo(Integer.valueOf(map.get("parkId").toString()));});
    }
    public void queryCarInOutInfo(Integer parkId) {

        String uri = "/wec/GetCarInoutInfo";
        JSONObject param = new JSONObject();
        param.put("appId", appId);
        param.put("parkId",parkId);
        param.put("ts", System.currentTimeMillis());
        param.put("reqId", UUID.randomUUID().toString());
        param.put("serviceCode", "getCarInoutInfo");
        int pageIndex = 1;
        param.put("pageIndex", pageIndex);
        param.put("pageSize", 1000);
        param.put("key",SignUtils.paramsSign(param, appSecret));

        Map<String, String> header = Maps.newHashMap();
        header.put("version", "1.0.0");
        String resultStr = OkHttpRequestUtils.postJSONDebug(ipAddressUrl + uri, header, JSON.toJSONString(param));
        System.out.println("请求结果：" + resultStr);

        JSONObject result = JSONObject.parseObject(resultStr);
        if(!"0".equals(result.get("resCode"))){
            logger.error("false,获取数据失败");
            return;
        }
        JSONObject jsonResult = JSONObject.parseObject(JSON.toJSONString(result.get("data")));
        int totalCount = Integer.valueOf(JSON.toJSONString(jsonResult.get("totalCount")));
        List<Map<String,Object>> resultList = (List<Map<String,Object>>) jsonResult.get("detailList");

        while (totalCount>(resultList.size()*pageIndex)){
            pageIndex++;
            param.put("ts",System.currentTimeMillis());
            param.put("reqId",UUID.randomUUID().toString());
            param.put("pageIndex",pageIndex);
            param.put("key",SignUtils.paramsSign(param, appSecret));
            resultStr = OkHttpRequestUtils.postJSONDebug(ipAddressUrl + uri, header, JSON.toJSONString(param));
            JSONObject result_ = JSONObject.parseObject(resultStr);
            if(!"0".equals(result.get("resCode"))){
                logger.error("false,获取数据失败");
                return;
            }
            JSONObject jsonResult_ = JSONObject.parseObject(JSON.toJSONString(result_.get("data")));
            resultList.addAll((List<Map<String,Object>>) jsonResult_.get("detailList"));
        }
        List<Map> list = new ArrayList<>();
        Map<String,Object> data = new HashMap<>();
        resultList.forEach(stringObjectMap -> {
            data.put("vehicleNum",stringObjectMap.get("plateNo"));
            data.put("passingTime",stringObjectMap.get("capTime"));
            //第三方接口，进场为0，出场为1；而我们定义的入场为1，出场为0
            int inOrOut = Integer.valueOf(stringObjectMap.get("capFlag").toString())==0?1:0;
            data.put("inOrOut",inOrOut);
        });
        client.save("parkDataDetail",true,list);



    }

    /**
     * 空余车位
     */
    public void getFreeSpaceNum() {
        String uri = "/wec/GetFreeSpaceNum";
        JSONObject param = new JSONObject();
        param.put("appId", appId);
        param.put("parkId",parkId);
        param.put("ts", System.currentTimeMillis());
        param.put("reqId", UUID.randomUUID().toString());
        param.put("serviceCode", "getFreeSpaceNum");
        param.put("key",SignUtils.paramsSign((JSONObject) JSON.toJSON(param), appSecret));

        Map<String, String> header = Maps.newHashMap();
        header.put("version", "1.0.0");
        String result = OkHttpRequestUtils.postJSONDebug(ipAddressUrl + uri, header, JSON.toJSONString(param));

        System.out.println("请求结果：" + result);
    }

    public static void main(String[] args){
        ParkDataController parkData = new ParkDataController();
//        parkData.queryFee();
//        parkData.queryFee2();
        parkData.getSanPingAllParkList();
        parkData.getSanPingAllParkList2();
//        parkData.queryCarInOutInfo();
//        parkData.getFreeSpaceNum();
    }


}
