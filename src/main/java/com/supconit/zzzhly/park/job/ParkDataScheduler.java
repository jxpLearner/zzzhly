package com.supconit.zzzhly.park.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.supconit.mc.api.ConfigClient;
import com.supconit.mc.entity.SearchCondition;
import com.supconit.zzzhly.common.CommonUtils;
import com.supconit.zzzhly.common.ConfigClientLocal;
import com.supconit.zzzhly.park.domain.ParkData;
import com.supconit.zzzhly.park.domain.ParkVehicleDataDetail;
import com.supconit.zzzhly.park.domain.VehicleDetail;
import com.supconit.zzzhly.park.domain.VehicleSource;
import com.supconit.zzzhly.park.services.service.ParkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @auther: jxp
 * @date: 2021/3/17 16:34
 * @description: 停车场job
 */
@Controller
@RequestMapping(value = "job/park")
@Api(tags = "停车场数据统计job")
public class ParkDataScheduler {

    //停车场
    @Value("${parkDataId}")
    private String parkDataId;
    //过车记录
    @Value("${parkDataDetail}")
    private String parkDataDetail;
    //上报的车辆来源地数据
    @Value("${uploadVehicleSource}")
    private String uploadVehicleSource;
    //上报的车流量数据
    @Value("${vehicleDetail}")
    private String vehicleDetail;

    @Autowired
    private ParkService parkService;
    @Autowired
    private ConfigClientLocal client;
//    private ConfigClient client;

    /**
     * 根据停车场过车记录+上报系统车辆来源地数据
     * 每小时更新过车记录的数据到上报的车辆来源地数据表中
     *   如：date=2021-04-06
     *      01:30分第一次更新 date 的数据到车辆来源地数据表中
     *      次日00:30分最后一次更新 date 的数据到车辆来源地数据表中
     */
//    @Scheduled(cron = "0 30 * * * ?")
    public void updateVehicleDetailDataHours(){
        updateVehicleDetail1Hour();
    }
    /**
     * 根据停车场过车记录+上报系统数据：
     *      每小时更新过车记录的数据到上报的车辆来源地数据表中
     */
//    @Scheduled(cron = "0 30 * * * ?")
    public void updateVehicleSourceDataHours(){
        updateVehicleSource1Hour();
    }
    /**
     * 每小时更新停车场的剩余停车位信息：
     *      景区停车场数据是上报的，查询当天总车辆，计算余位信息；
     *      有过车记录的详情的景区，可以通过接口获取停车场余位信息。
     */
//    @Scheduled(cron = "0 30 * * * ?")
    public void updateParkDataHours() {
        updateParkData();
    }

    @GetMapping(value = "updateParkData")
    @ApiOperation(value = "更新停车场余位数据",tags = "",notes = "更新停车场余位数据")
    public void updateParkData() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY,-1);
        String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        //停车场基础信息
        Map<String,Object> parkMap = (Map<String, Object>) client.list(parkDataId, true, "", false, new ArrayList<>());
        List<Map<String, Object>> parkList = (List<Map<String, Object>>) parkMap.get("obj");
        List<ParkData> parkDataList = new ArrayList<>();
        parkList.forEach(stringStringMap -> parkDataList.add(JSONObject.parseObject(JSON.toJSONString(stringStringMap), ParkData.class)));
        //查询上报数据当日的车流量数据总和
        parkDataList.forEach(parkData -> {
            SearchCondition param = new SearchCondition();
            param.setQueryType("wildcardQuery");
            param.setMatchType("must");
            param.setPointId("scenicCode");
            param.setParam(parkData.getScenicCode());
            SearchCondition param1 = new SearchCondition();
            param1.setQueryType("wildcardQuery");
            param1.setMatchType("must");
            param1.setPointId("dateStr");
            param1.setParam(date);
            List<SearchCondition> params = new ArrayList<>();
            params.add(param);
            params.add(param1);
            Map<String,Object> dataMap = (Map<String, Object>) client.list(vehicleDetail,true,"",false,params);
            List<Map<String, Object>> data = (List<Map<String, Object>>) dataMap.get("obj");
            Integer daytotal = 0;
            if(data.size()>0){
                for(Map<String,Object> map:data){
                    daytotal += Integer.valueOf(map.get("vehicleNum").toString().split("\\.")[0]);
                }
            }
            parkData.setSurplus(parkData.getTotal()-daytotal); //对于上报的车流量，空余停车位=总车位-当天上报的车辆总数
        });
        List<Map> parkdatamap =  parkDataList.stream().map(obj -> CommonUtils.convertToMap(obj)).collect(Collectors.toList());
        client.save(parkDataId,true,parkdatamap);
    }

    @GetMapping(value = "testUpdateVehicleDetail")
    @ApiOperation(value = "更新过车记录的数据到上报的车流量数据",tags = "",notes = "更新过车记录的数据到上报的车流量数据")
    public void updateVehicleDetail1Hour(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY,-1);//每小时更新，如00:30更新上一日23:30总的数据
        String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        //当日的过车记录数据
        List<ParkVehicleDataDetail> vehicleDataDetails = getParkVehicleDataDetail(date);
        //根据当日的过车记录统计车流量数据
        List<VehicleDetail> vehicleDetailList = tongjiVehicleDetailData(vehicleDataDetails,date);
        //当日上报的车流量数据
        List<SearchCondition> params2 = new ArrayList<>();
        SearchCondition param3 = new SearchCondition();
        param3.setPointId("dateStr");//匹配属性字段名称
        param3.setMatchType("must");//匹配模式
        param3.setQueryType("wildcardQuery");//查询类型
        param3.setParam(date);
        params2.add(param3);
        Map<String,Object> dataMap2 = (Map<String, Object>) client.list(vehicleDetail,true,"",false,params2);
        List<Map<String, Object>> data2 = (List<Map<String, Object>>) dataMap2.get("obj");

        List<VehicleDetail> vehicleDetails = new ArrayList<>();
        if(data2.size()>0){
            data2.forEach(stringObjectMap -> vehicleDetails.add(JSONObject.parseObject(JSON.toJSONString(stringObjectMap),VehicleDetail.class)));
            //合并相同的数据
            //单独保存，过车记录统计后的车流量数据中上传的车流量数据没有的
            List<VehicleDetail> tmpList = new ArrayList<>();
            for(VehicleDetail v1:vehicleDetailList){
                for(VehicleDetail v2:vehicleDetails){
                    if(v2.equals(v1)){//重写了equals方法
                        v2.setVehicleNum2(v1.getVehicleNum2());
                        tmpList.add(v1);
                    }
                }
            }
            //去重后合并
            vehicleDetailList.removeAll(tmpList);
            vehicleDetails.addAll(vehicleDetailList);
        }
        List<Map> mapList = vehicleDetails.stream().map(obj -> CommonUtils.convertToMap(obj)).collect(Collectors.toList());
        client.save(vehicleDetail,true,mapList);
    }

    @GetMapping(value = "testUpdateVehicleSource")
    @ApiOperation(value = "更新过车记录数据到上报的车辆来源地数据", tags = "", notes = "更新过车记录数据到上报的车辆来源地数据")
    public void updateVehicleSource1Hour(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY,-1);//每小时更新，如00:30更新上一日总的数据
        String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        //当日的过车记录数据
        List<ParkVehicleDataDetail> vehicleDataDetails = getParkVehicleDataDetail(date);
        //根据过车记录统计车辆来源地数据
        List<VehicleSource> vehicleSourceList = tongjiVehicleSourceData(vehicleDataDetails,date);
        //当日上报的车辆来源地数据
        List<SearchCondition> params2 = new ArrayList<>();
        SearchCondition param3 = new SearchCondition();
        param3.setPointId("dateStr");//匹配属性字段名称
        param3.setMatchType("must");//匹配模式
        param3.setQueryType("wildcardQuery");//查询类型
        param3.setParam(date);
        params2.add(param3);
        Map<String,Object> dataMap2 = (Map<String, Object>) client.list(uploadVehicleSource,true,"",false,params2);
        List<Map<String, Object>> data2 = (List<Map<String, Object>>) dataMap2.get("obj");

        List<VehicleSource> uploadVehicleSourceList = new ArrayList<>();
        if(data2.size()!=0){
            data2.forEach(stringObjectMap -> uploadVehicleSourceList.add(JSONObject.parseObject(JSON.toJSONString(stringObjectMap),VehicleSource.class)));
            //合并相同的数据
            //单独保存，过车记录统计后的车辆来源地数据中上传的来源地数据没有的，
            List<VehicleSource> tmpList = new ArrayList<>();
            for(VehicleSource v1:vehicleSourceList){
                for(VehicleSource v2:uploadVehicleSourceList){
                    if(v2.equals(v1)){//重写了equals方法
                        v2.setVehicleNum2(v1.getVehicleNum2());
                        tmpList.add(v1);
                    }
                }
            }
            //去重后合并
            vehicleSourceList.removeAll(tmpList);
            uploadVehicleSourceList.addAll(vehicleSourceList);
        }
        List<Map> mapList = uploadVehicleSourceList.stream().map(obj -> CommonUtils.convertToMap(obj)).collect(Collectors.toList());
        client.save(uploadVehicleSource,true,mapList);
    }

    /**
     *  获取当日的过车记录数据详情
     * @param date
     * @return
     */
    public List<ParkVehicleDataDetail> getParkVehicleDataDetail(String date){
        SearchCondition param = new SearchCondition();
        param.setPointId("dateStr");//匹配属性字段名称
        param.setMatchType("must");//匹配模式
        param.setQueryType("wildcardQuery");//查询类型
        param.setParam(date);
        SearchCondition param1 = new SearchCondition();
        param1.setPointId("inOrOut");
        param1.setMatchType("must");
        param1.setQueryType("wildcardQuery");
        param1.setParam("1");
        List<SearchCondition> params = new ArrayList<>();
        params.add(param);
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
        return vehicleDataDetails;
    }

    /**
     * 将过车记录数据转为统计后的来源地数据
     * @param vehicleDataDetails
     * @param date
     * @return
     */
    public List<VehicleSource> tongjiVehicleSourceData(List<ParkVehicleDataDetail> vehicleDataDetails,String date){
        List<VehicleSource> list = new ArrayList<>();
        Map<String,String> parkNameScenicCodeMap = parkService.getParkScenicCodeMap2();
        //按照景区分组车辆
        Map<String,List<ParkVehicleDataDetail>> scenicVehicleData = vehicleDataDetails.stream()
                .collect(Collectors.groupingBy(ParkVehicleDataDetail::getScenicCode));
//        Map<String,List<ParkVehicleDataDetail>> scenicVehicleData = new HashMap<>();
//        vehicleDataDetails.forEach(parkVehicleDataDetail ->{
//            if(null!=scenicVehicleData.get(parkVehicleDataDetail.getScenicCode())){
//                scenicVehicleData.get(parkVehicleDataDetail.getScenicCode()).add(parkVehicleDataDetail);
//            }else{
//                scenicVehicleData.put(parkVehicleDataDetail.getScenicCode(),new ArrayList<>());
//                scenicVehicleData.get(parkVehicleDataDetail.getScenicCode()).add(parkVehicleDataDetail);
//            }
//        });

        for(Map.Entry<String,List<ParkVehicleDataDetail>> entry:scenicVehicleData.entrySet()){

            List<ParkVehicleDataDetail> vehicleList = entry.getValue();
            Map<String,List<ParkVehicleDataDetail>> provinceMap = vehicleList.stream()
                    .collect(Collectors.groupingBy(ParkVehicleDataDetail::getVehicleNum_1_name));

            provinceMap.forEach((key,value)->{
                //省外
                VehicleSource vehicleSource = new VehicleSource();
                vehicleSource.setScenicCode(entry.getKey());
                vehicleSource.setName(parkNameScenicCodeMap.get(entry.getKey()));
                vehicleSource.setSourceName(key);
                vehicleSource.setVehicleNum2(value.size());
                vehicleSource.setFrequency(1);
                vehicleSource.setDateStr(date);
                vehicleSource.setMonthStr(date.substring(0,7));//2021-03-25
                vehicleSource.setYearStr(date.substring(0,4));
                list.add(vehicleSource);
                //省内
                if(key.contains("福建")){
                    Map<String,List<ParkVehicleDataDetail>> cityMap = value.stream()
                            .collect(Collectors.groupingBy(ParkVehicleDataDetail::getVehicleNum_2_name));
                    cityMap.forEach((key1,value1)->{
                        VehicleSource vehicleSource1 = new VehicleSource();
                        vehicleSource1.setScenicCode(entry.getKey());
                        vehicleSource1.setName(parkNameScenicCodeMap.get(entry.getKey()));
                        vehicleSource1.setSourceName(key1);
                        vehicleSource1.setVehicleNum2(value1.size());
                        vehicleSource1.setFrequency(0);
                        vehicleSource1.setDateStr(date);
                        vehicleSource1.setMonthStr(date.substring(0,7));//2021-03-25
                        vehicleSource1.setYearStr(date.substring(0,4));
                        list.add(vehicleSource1);
                    });
                }

            });
        }
        return  list;
    }

    /**
     * 将过车记录数据转为统计后的车流量数据
     * @param vehicleDataDetails
     * @param date
     * @return
     */
    public List<VehicleDetail> tongjiVehicleDetailData(List<ParkVehicleDataDetail> vehicleDataDetails,String date){
        List<VehicleDetail> list = new ArrayList<>();
        Map<String,String> parkNameScenicCodeMap = parkService.getParkScenicCodeMap2();
        //按照景区分组车辆
        Map<String,List<ParkVehicleDataDetail>> scenicVehicleData = vehicleDataDetails.stream()
                .collect(Collectors.groupingBy(ParkVehicleDataDetail::getScenicCode));
        for(Map.Entry<String,List<ParkVehicleDataDetail>> entry:scenicVehicleData.entrySet()){
            List<ParkVehicleDataDetail> vehicleList = entry.getValue();
            //单个景区中，按小时分组
            vehicleList.stream().collect(Collectors.groupingBy(ParkVehicleDataDetail::getHourStr)).forEach((key,value)->{
                VehicleDetail vehicleDetail = new VehicleDetail();
                vehicleDetail.setName(parkNameScenicCodeMap.get(entry.getKey()));
                vehicleDetail.setScenicCode(entry.getKey());
                vehicleDetail.setDateStr(date);
                vehicleDetail.setHourStr(key);
                vehicleDetail.setMonthStr(date.substring(0,7));
                vehicleDetail.setYearStr(date.substring(0,4));
                vehicleDetail.setVehicleNum2(value.size());
                list.add(vehicleDetail);
            });
            /*Map<String,List<ParkVehicleDataDetail>> hourMap = vehicleList.stream().collect(Collectors.groupingBy(ParkVehicleDataDetail::getHourStr));
            hourMap.forEach((key,value)->{
                VehicleDetail vehicleDetail = new VehicleDetail();
                vehicleDetail.setName(parkNameScenicCodeMap.get(entry.getKey()));
                vehicleDetail.setScenicCode(entry.getKey());
                vehicleDetail.setDateStr(date);
                vehicleDetail.setHourStr(key);
                vehicleDetail.setMonthStr(date.substring(0,7));
                vehicleDetail.setYearStr(date.substring(0,4));
                vehicleDetail.setVehicleNum2(value.size());
                list.add(vehicleDetail);
            });*/
        }
        return  list;
    }





}
