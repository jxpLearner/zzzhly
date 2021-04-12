package com.supconit.zzzhly.park.controllers;

import com.alibaba.fastjson.JSONObject;
import com.supconit.mc.api.ConfigClient;
import com.supconit.mc.entity.SearchCondition;
import com.supconit.zzzhly.common.CommonUtils;
import com.supconit.zzzhly.common.ConfigClientLocal;
import com.supconit.zzzhly.park.domain.VehicleDetail;
import com.supconit.zzzhly.park.domain.VehicleSource;
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

/**
 * @auther: jxp
 * @date: 2021/3/26 8:38
 * @description:
 */
@Controller
@RequestMapping(value = "uploadData")
@Api(tags = "上传车流量以及来源地属性数据")
public class UploadVehicleDataController {

    @Value("${vehicleDetail}")
    private String vehicleDetailId;
    @Value("${uploadVehicleSource}")
    private String uploadVehicleSource;

    @Autowired
    private ParkService parkService;
    @Autowired
    private ConfigClientLocal client;
//    private ConfigClient client;


    @ResponseBody
    @PostMapping(value = "uploadVehicleSourceData")
    @ApiOperation(value = "上报车辆来源地的数据", tags = "", notes = "上报车辆来源地的数据")
    @ApiImplicitParam(name = "jsonStr", value = "jsonStr", required = true)
    public Map<String,Object> saveUploadVehicleSourceData(@RequestBody String jsonStr){
        Map<String,Object> result = new HashMap<>();
        Map<String,String> scenicCode2ParkName = parkService.getParkScenicCodeMap2();
//        jsonStr = "{\"date\":\"2021-03-27\",\"scenicCode\":\"SPJQ\",\"frequency\":1,\"sourceData\":[{\"sourceName\":\"福建省\",\"value\":24},{\"sourceName\":\"浙江省\",\"value\":24},{\"sourceName\":\"河南省\",\"value\":24}]}";
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        List<Map<String,Object>> sourceDataList = (List<Map<String,Object>>) jsonObject.get("sourceData");
        Integer frequency = jsonObject.getInteger("frequency");
        String scenicCode = jsonObject.getString("scenicCode");
        String date = jsonObject.getString("date");
        //查看是否已有数据
        List<Map<String, Object>> mapdata = getVehicleSourceData(date,scenicCode,frequency);
        List<Map> list = new ArrayList<>();
        if(mapdata.size()<1){
            //新增
            sourceDataList.forEach(map ->{
                        VehicleSource vehicleSource = new VehicleSource();
                        vehicleSource.setName(scenicCode2ParkName.get(scenicCode));
                        vehicleSource.setScenicCode(scenicCode);
                        vehicleSource.setFrequency(frequency);
                        vehicleSource.setDateStr(date);
                        vehicleSource.setMonthStr(date.substring(0,7));
                        vehicleSource.setYearStr(date.substring(0,4));
                        vehicleSource.setSourceName(map.get("sourceName").toString());
                        vehicleSource.setVehicleNum(Integer.valueOf(map.get("value").toString()));
                        list.add(CommonUtils.convertToMap(vehicleSource));
                    }
            );
        }else {
            List<Map<String,Object>> olddata = new ArrayList<>();
            //修改更新旧数据并新增原来来源地没有的数据
            mapdata.forEach(stringObjectMap -> {
                sourceDataList.forEach(map -> {
                    //修改更新
                    if(map.get("sourceName").equals(stringObjectMap.get("sourceName"))){
                        stringObjectMap.put("vehicleNum",map.get("value"));
                        list.add(stringObjectMap);
                        olddata.add(map);
                    }
                });
            });

            if(olddata.size()>0){
                //删除已更新的，后保存新增
                sourceDataList.removeAll(olddata);
                sourceDataList.forEach(map -> {
                    VehicleSource vehicleSource = new VehicleSource();
                    vehicleSource.setName(scenicCode2ParkName.get(scenicCode));
                    vehicleSource.setScenicCode(scenicCode);
                    vehicleSource.setFrequency(frequency);
                    vehicleSource.setDateStr(date);
                    vehicleSource.setMonthStr(date.substring(0,7));
                    vehicleSource.setYearStr(date.substring(0,4));
                    vehicleSource.setSourceName(map.get("sourceName").toString());
                    vehicleSource.setVehicleNum(Integer.valueOf(map.get("value").toString()));
                    list.add(CommonUtils.convertToMap(vehicleSource));
                });
            }
        }

        client.save(uploadVehicleSource,true,list);
        result.put("success","success");
        return  result;
    }

    @ResponseBody
    @GetMapping(value = "getUploadVihicleSourceData")
    @ApiOperation(value = "获取上报车辆来源地的数据", tags = "", notes = "获取上报车辆来源地的数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "scenicCode", value = "scenicCode", required = true),
            @ApiImplicitParam(name = "date", value = "date", required = true),
    })
    public Map<String,Object> getUploadVihicleSourceData(String scenicCode,String date,Integer frequency){
        Map<String,Object> result = new HashMap<>();
        List<Map<String, Object>> mapdata = getVehicleSourceData(date,scenicCode,frequency);
        if(mapdata.size()<1){
            result.put("false","没有数据");
            return result;
        }
        List<Map<String,Object>>  sourceData = new ArrayList<>();
        result.put("date",date);
        result.put("scenicCode",scenicCode);
        result.put("frequency",frequency);
        mapdata.forEach(map->{
            Map<String,Object> sourceMap = new HashMap<>();
            sourceMap.put("sourceName",map.get("sourceName"));
            sourceMap.put("value",Integer.valueOf(map.get("vehicleNum").toString().split("\\.")[0]));
            sourceData.add(sourceMap);
        });
        result.put("sourceData",sourceData);
        return result;
    }

    /**
     *  上报系统-整小时点的车辆数据，如果没有小时点的数有一天总的数据也可以
     * @param jsonStr
     * @return
     */
    @ResponseBody
    @PostMapping(value = "uploadVihicleDetailData")
    @ApiOperation(value = "上报车流量的数据", tags = "", notes = "上报车流量的数据")
    @ApiImplicitParam(name = "jsonStr", value = "jsonStr", required = true)
    public Map<String,Object> saveUploadVehicleDetail(@RequestBody String jsonStr){
        Map<String,Object> result = new HashMap<>();
        Map<String,String> scenicCode2ParkName = parkService.getParkScenicCodeMap2();
//        jsonStr = "{\"date\":\"2021-03-30\",\"scenicCode\":\"SPJQ\",\"total\":250,\"hourData\":[{\"name\":\"00\",\"value\":24},{\"name\":\"01\",\"value\":24}]}";
        //可能没有小时的数据，只有一天总的数据
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        List<Map<String,Object>> hourDataList = (List<Map<String,Object>>) jsonObject.get("hourData");
        Integer total = jsonObject.getInteger("total");
        String scenicCode = jsonObject.getString("scenicCode");
        String date = jsonObject.getString("date");
        //判断数据库是否已有数据
        List<Map<String, Object>> data2 = getVehicleDetailData(date,scenicCode);
        if(data2.size()<1){
            //数据库没有数据--新增
            result = saveVehicleDetailData(hourDataList,total,date,scenicCode);
        }else{
            //数据库有数据---修改
            List<Map> list = new ArrayList<>();
            List<Map<String,Object>> olddata = new ArrayList<>();
            data2.forEach(stringObjectMap -> {
                // 只有天的数据,当只有天的总数据时，hourStr字段为空
                if(data2.size()==1 && null == stringObjectMap.get("hourStr")){

                    //如果修改的时候还是只有总的数据
                    if(hourDataList.size()<1 && null!=total && total!=0){
                        stringObjectMap.put("vehicleNum",total);
                        list.add(stringObjectMap);
                    }
                    //如果修改的时候有具体小时的数据，就是新插入具体小时的数据了
                    if(hourDataList.size()>0){
                        hourDataList.forEach(map -> {
                                    VehicleDetail vehicleDetail = new VehicleDetail();
                                    vehicleDetail.setName(scenicCode2ParkName.get(scenicCode));
                                    vehicleDetail.setScenicCode(scenicCode);
                                    vehicleDetail.setDateStr(date);
                                    vehicleDetail.setHourStr(map.get("name").toString());
                                    vehicleDetail.setMonthStr(date.substring(0,7));
                                    vehicleDetail.setYearStr(date.substring(0,4));
                                    vehicleDetail.setVehicleNum(Integer.valueOf(map.get("value").toString()));
                                    list.add(CommonUtils.convertToMap(vehicleDetail));
                                }
                        );
                        //删除旧的一条总的数据
                        client.removeById(vehicleDetailId,true,stringObjectMap.get("_id").toString());
                    }
                }else {
                    //修改已经保存的小时的数据，同时传过来的数据中有新增的小时的数据
                    hourDataList.forEach(map -> {
                        if(map.get("name").equals(stringObjectMap.get("hourStr"))){
                            stringObjectMap.put("vehicleNum",map.get("value"));
                            list.add(stringObjectMap);
                            olddata.add(map);
                        }
                    });

                }
            });

            //修改数据时，新上报的有新增的具体小时的数据
            if(olddata.size()>0){
                //去除查询到的要修改的数据中
                hourDataList.removeAll(olddata);
                hourDataList.forEach(map -> {
                            VehicleDetail vehicleDetail = new VehicleDetail();
                            vehicleDetail.setName(scenicCode2ParkName.get(scenicCode));
                            vehicleDetail.setScenicCode(scenicCode);
                            vehicleDetail.setDateStr(date);
                            vehicleDetail.setHourStr(map.get("name").toString());
                            vehicleDetail.setMonthStr(date.substring(0,7));
                            vehicleDetail.setYearStr(date.substring(0,4));
                            vehicleDetail.setVehicleNum(Integer.valueOf(map.get("value").toString()));
                            list.add(CommonUtils.convertToMap(vehicleDetail));
                        }
                );
            }
            client.save(vehicleDetailId,true,list);
            result.put("success","success");

        }
        return result;
    }

    @ResponseBody
    @GetMapping(value = "getUploadVihicleDetailData")
    @ApiOperation(value = "获取上报的车流量的数据", tags = "", notes = "获取上报的车流量的数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "scenicCode", value = "scenicCode", required = true),
            @ApiImplicitParam(name = "date", value = "date", required = true),
    })
    public Map<String,Object> getUploadVehicleDetail(String scenicCode,String date){
        Map<String,Object> result = new HashMap<>();
        List<Map<String, Object>> data2 = getVehicleDetailData(date,scenicCode);
        if(data2.size()<1){
            result.put("false","false,没有数据");
            return result;
        }
        List<Map<String,Object>>  hourData = new ArrayList<>();
        data2.forEach(map->{
            result.put("date",date);
            result.put("scenicCode",scenicCode);
            //没有具体到小时的数据，只有一个总的数据
            if(data2.size() == 1 && null==map.get("hourStr")){
                result.put("total",map.get("vehicleNum"));
                result.put("hourData",new ArrayList());
                return;
            }
            Map<String,Object> hourMap = new HashMap<>();
            hourMap.put("name",map.get("hourStr"));
            hourMap.put("value",Integer.valueOf(map.get("vehicleNum").toString().split("\\.")[0]));
            hourData.add(hourMap);
        });
        result.put("hourData",hourData);
        return result;
    }

    private List<Map<String, Object>> getVehicleDetailData(String date,String scenicCode){
        SearchCondition param = new SearchCondition();
        param.setPointId("dateStr");
        param.setParam(date);
        param.setQueryType("wildcardQuery");
        param.setMatchType("must");
        SearchCondition param1 = new SearchCondition();
        param1.setPointId("scenicCode");
        param1.setMatchType("must");
        param1.setQueryType("wildcardQuery");
        param1.setParam(scenicCode);
        List<SearchCondition> params = new ArrayList<>();
        params.add(param);
        params.add(param1);
        Map<String,Object> responseMap = (Map<String,Object>) client.list(vehicleDetailId,true,"",true,params);

        return (List<Map<String, Object>>) responseMap.get("obj");
    }
    private List<Map<String, Object>> getVehicleSourceData(String date,String scenicCode,Integer frequency){
        SearchCondition param = new SearchCondition();
        param.setPointId("dateStr");
        param.setParam(date);
        param.setQueryType("wildcardQuery");
        param.setMatchType("must");
        SearchCondition param1 = new SearchCondition();
        param1.setPointId("scenicCode");
        param1.setMatchType("must");
        param1.setQueryType("wildcardQuery");
        param1.setParam(scenicCode);
        SearchCondition param2 = new SearchCondition();
        param2.setPointId("frequency");
        param2.setMatchType("must");
        param2.setQueryType("wildcardQuery");
        param2.setParam(frequency.toString());
        List<SearchCondition> params = new ArrayList<>();
        params.add(param);
        params.add(param1);
        params.add(param2);
        Map<String,Object> responseMap = (Map<String,Object>) client.list(uploadVehicleSource,true,"",true,params);

        return (List<Map<String, Object>>) responseMap.get("obj");
    }

    private Map<String,Object> saveVehicleDetailData(List<Map<String,Object>> hourDataList,Integer total,String date,String scenicCode){
        Map<String,Object> result = new HashMap<>();
        Map<String,String> scenicCode2ParkName = parkService.getParkScenicCodeMap2();
        //如果只有总的数据，就只存入天的信息
        if(hourDataList.size()<1 && null!=total && total!=0){
            VehicleDetail vehicleDetail = new VehicleDetail();
            vehicleDetail.setName(scenicCode2ParkName.get(scenicCode));
            vehicleDetail.setScenicCode(scenicCode);
            vehicleDetail.setDateStr(date);
            vehicleDetail.setMonthStr(date.substring(0,7));
            vehicleDetail.setYearStr(date.substring(0,4));
            vehicleDetail.setVehicleNum(total);
            List<Map> list = new ArrayList<>();
            list.add(CommonUtils.convertToMap(vehicleDetail));
            client.save(vehicleDetailId,true,list);
            result.put("success","success");
            return result;
        }
        //有具体小时的数据
        if(hourDataList.size()>0){
            List<Map> list = new ArrayList<>();
            hourDataList.forEach(map -> {
                        VehicleDetail vehicleDetail = new VehicleDetail();
                        vehicleDetail.setName(scenicCode2ParkName.get(scenicCode));
                        vehicleDetail.setScenicCode(scenicCode);
                        vehicleDetail.setDateStr(date);
                        vehicleDetail.setHourStr(map.get("name").toString());
                        vehicleDetail.setMonthStr(date.substring(0,7));
                        vehicleDetail.setYearStr(date.substring(0,4));
                        vehicleDetail.setVehicleNum(Integer.valueOf(map.get("value").toString()));
                        list.add(CommonUtils.convertToMap(vehicleDetail));
                    }
            );
            client.save(vehicleDetailId,true,list);
            result.put("success","success");
            return result;
        }
        result.put("false","false");
        return result;
    }


}
