package com.supconit.zzzhly.park.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.supconit.mc.api.ConfigClient;
import com.supconit.mc.entity.SearchCondition;
import com.supconit.zzzhly.common.ConfigClientLocal;
import com.supconit.zzzhly.common.PlateUtils;
import com.supconit.zzzhly.common.httpclient.HttpClientService;
import com.supconit.zzzhly.park.domain.ParkVehicleDataDetail;
import com.supconit.zzzhly.park.services.service.ParkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @auther: jxp
 * @date: 2021/3/5 12:48
 * @description: 漳州停车场过车数据统计车辆来源地数据
 */

@Controller
@RequestMapping("park")
@Api(tags = "停车场及车辆来源地")
public class ParkController {

    @Value("${parkDataDetail}")
    private String parkDataDetail;
    @Value("${parkDataId}")
    private String parkDataId;

    @Autowired
    private ParkService parkService;
    @Autowired
//    private ConfigClient client;
    private ConfigClientLocal client;

    /**
     * 根据停车场的过车记录，统计分析车辆的忠诚度
     * @param startDate 查询的日期，年/月
     * @param endDate 查询的日期，年/月
     * @param scenicCode 景区名称，默认为空，查询全部
     * @return
     */
    @GetMapping("getParkAnalysisData")
    @ResponseBody
    @ApiOperation(value = "获取车流量的统计信息", tags = "", notes = "获取车流量的统计信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "年/月/日期（节假日首日）"),
            @ApiImplicitParam(name = "endDate", value = "日期，节假日时不为空"),
            @ApiImplicitParam(name = "scenicCode", value = "景区名称")
    })
    public List<Map<String,Object>> parkDataAnalysis(String startDate,String endDate, String scenicCode) {
        List<SearchCondition> params = new ArrayList<>();
        // 节假日，endDate不为空
        if(null!=startDate && null != endDate){
            SearchCondition param = new SearchCondition();
            param.setMatchType("must");//匹配模式
            param.setQueryType("gte");//查询类型，大于或等于
            param.setPointId("dateStr");//匹配属性字段名称
            param.setParam(startDate);
            params.add(param);
            SearchCondition param0 = new SearchCondition();
            param0.setMatchType("must");//匹配模式
            param0.setQueryType("lte");//查询类型,小于或等于
            param0.setPointId("dateStr");//匹配属性字段名称
            param0.setParam(endDate);
            params.add(param0);
        //非节假日，年/月，endDate为空
        }else if(null!=startDate && startDate.length()>4){//月,2021-04
            SearchCondition param = new SearchCondition();
            param.setMatchType("must");//匹配模式
            param.setQueryType("wildcardQuery");//查询类型
            param.setPointId("monthStr");//匹配属性字段名称
            param.setParam(startDate);
            params.add(param);
        }else if(null!=startDate && startDate.length()<5){ //年，2021
            SearchCondition param = new SearchCondition();
            param.setMatchType("must");//匹配模式
            param.setQueryType("wildcardQuery");//查询类型
            param.setPointId("yearStr");//匹配属性字段名称
            param.setParam(startDate);
            params.add(param);
        }
        //具体景区
        if(null!=scenicCode){
            SearchCondition param2 = new SearchCondition();
            param2.setMatchType("must");//匹配模式
            param2.setQueryType("wildcardQuery");//查询类型
            param2.setPointId("scenicCode");//匹配属性字段名称
            param2.setParam(scenicCode);
            params.add(param2);
        }
        SearchCondition param1 = new SearchCondition();
        param1.setPointId("inOrOut");
        param1.setMatchType("must");
        param1.setQueryType("wildcardQuery");
        param1.setParam("1");
        params.add(param1);

        Map<String,Object> dataMap = (Map<String, Object>) client.list(parkDataDetail,true,"",false,params);
        List<Map<String, Object>> data = (List<Map<String, Object>>) dataMap.get("obj");

        List<ParkVehicleDataDetail> vehicleDataDetails = new ArrayList<>();
        data.forEach(stringObjectMap -> {
            ParkVehicleDataDetail vehicleData = JSONObject.parseObject(JSON.toJSONString(stringObjectMap),ParkVehicleDataDetail.class);
            vehicleDataDetails.add(vehicleData);
        });
        //过车记录数据中停车场名称关联景区code
        Map<String,String> parkNameScenicCodeMap = parkService.getParkScenicCodeMap();
        vehicleDataDetails.forEach(parkVehicleDataDetail ->
                parkVehicleDataDetail.setScenicCode(parkNameScenicCodeMap.get(parkVehicleDataDetail.getName()))
        );
        Map<String,List<ParkVehicleDataDetail>> mapData = vehicleDataDetails.stream().collect(Collectors.groupingBy(ParkVehicleDataDetail::getVehicleNum));
        List<Map<String,Object>> vehicleTimes = new ArrayList<>();
        Map<String,Object> map0 = new HashMap<>();
        map0.put("name","两次");
        map0.put("value",0);
        Map<String,Object> map1 = new HashMap<>();
        map1.put("name","三次");
        map1.put("value",0);
        Map<String,Object> map2 = new HashMap<>();
        map2.put("name","三次以上");
        map2.put("value",0);
        mapData.forEach((key,value)->{
            Integer aa;
            //key车牌号，value对应的车辆数列表
            if(value.size()==2){
                aa = null==map0.get("value")?0:Integer.valueOf(map0.get("value").toString());
                map0.put("value",++aa);
            }else if(value.size()==3){
                aa = null==map1.get("value")?0:Integer.valueOf(map1.get("value").toString());
                map1.put("value",++aa);
            }else if(value.size()>3){
                aa = null==map2.get("value")?0:Integer.valueOf(map2.get("value").toString());
                map2.put("value",++aa);
            }
        });
        vehicleTimes.add(map0);
        vehicleTimes.add(map1);
        vehicleTimes.add(map2);

        return vehicleTimes;
    }

    /**
     * 过车记录对外接口
     *
     * @param data
     */
    @ResponseBody
    @PostMapping(value = "saveVehicleDetail")
    @ApiOperation(value = "保存停车场的过车记录", tags = "", notes = "保存停车场的过车记录")
    @ApiImplicitParam(name = "data", value = "data", required = true)
    public String parkDataSave(@RequestBody List<Map<String, String>> data) {
        Map<String, String> map = new HashMap<>();
        List<Map> list = new ArrayList<>();
        if (data.size() < 1) {
            map.put("false", "传输数据为空");
        }

        PlateUtils plateUtils = new PlateUtils();
        for (Map<String, String> body : data) {
            try {
                Map<String, String> datamap = new HashMap<>();
                datamap.put("name", body.get("name"));
                datamap.put("code", body.get("code"));
                datamap.put("vehicleNum", body.get("vehicleNum"));
                datamap.put("passingTime", body.get("passingTime"));
                datamap.put("inOrOut", body.get("inOrOut"));

                String dateStr = body.get("passingTime");
                datamap.put("dateStr", dateStr.substring(0, 10));
                datamap.put("hourStr", dateStr.substring(11, 13));
                datamap.put("monthStr",dateStr.substring(0, 7));
                datamap.put("yearStr",dateStr.substring(0, 4));
                datamap.put("vehicleNum_1",body.get("vehicleNum").substring(0,1));
                datamap.put("vehicleNum_2",body.get("vehicleNum").substring(0,2));
                datamap.put("vehicleNum_1_name",plateUtils.getProvinceName(body.get("vehicleNum").substring(0,1)));
                datamap.put("vehicleNum_2_name",plateUtils.getCityName(body.get("vehicleNum").substring(0,2)));
                list.add(datamap);
            } catch (Exception e) {
                e.printStackTrace();
                map.put("false", "传输数据格式错误");
            }
        }

        client.save(parkDataDetail, true, list);
        map.put("success", "success");
        return JSONObject.toJSONString(map);

    }



}
