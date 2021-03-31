package com.supconit.zzzhly.park.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.supconit.mc.api.ConfigClient;
import com.supconit.mc.entity.SearchCondition;
import com.supconit.zzzhly.common.CommonUtils;
import com.supconit.zzzhly.common.ConfigClientLocal;
import com.supconit.zzzhly.park.domain.ParkData;
import com.supconit.zzzhly.park.domain.ParkVehicleDataDetail;
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

    @Value("${parkDataId}")
    private String parkDataId;
    @Value("${parkDataDetail}")
    private String parkDataDetail;
    @Value("${uploadVehicleSource}")
    private String uploadVehicleSource;
    @Value("${vehicleSource}")
    private String vehicleSource;

    @Autowired
    private ParkService parkService;

    @Autowired
//    private ConfigClientLocal client;
    private ConfigClient client;

    /**
     * 每小时更新停车场的剩余停车位信息
     */
//    @Scheduled(cron = "0 0 */1 * * ?")
    public void updateParkData() {
        updateData();
    }

    @GetMapping(value = "updateParkData")
    public void updateData() {

        //停车场基础信息
        List<Map<String, String>> parkList = (List<Map<String, String>>) client.list(parkDataId, true, "", false, new ArrayList<>());
        List<ParkData> parkDataList = new ArrayList<>();
        parkList.forEach(stringStringMap -> parkDataList.add(JSONObject.parseObject(JSON.toJSONString(stringStringMap), ParkData.class)));


    }

    /**
     * 根据停车场过车记录+上报系统数据
     * 次日更新昨日的车流量数据
     */
//    @Scheduled(cron = "* 30 1 * * ?")
    public void updateYesterdayVehicleDetailData(){

        updateYesterdayVehicleDetail();
    }

    @GetMapping(value = "testUpdateVehicleDetail")
    @ApiOperation(value = "更新昨日的车流量数据", tags = "", notes = "更新车流量数据")
    public void updateYesterdayVehicleDetail(){

    }

    /**
     * 根据停车场过车记录+上报系统数据
     * 次日更新昨日的车辆来源地数据
     */
//    @Scheduled(cron = "* 30 1 * * ?")
    public void updateYesterdayVehicleSourceData(){

        updateYesterDayVehicleSource();
    }

    @GetMapping(value = "testUpdateVehicleSource")
    @ApiOperation(value = "获取车辆的来源地数据", tags = "", notes = "获取车辆的来源地数据")
    public void updateYesterDayVehicleSource(){

        //昨日过车记录数据--入场
        SearchCondition param = new SearchCondition();
        param.setPointId("date");//匹配属性字段名称
        param.setMatchType("must");//匹配模式
        param.setQueryType("wildcardQuery");//查询类型
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH,-1);
        Date yesterday = calendar.getTime();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(yesterday);
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

        //昨日上报的车辆来源地数据
        List<SearchCondition> params2 = new ArrayList<>();
        SearchCondition param2 = new SearchCondition();
        param2.setPointId("date");//匹配属性字段名称
        param2.setMatchType("must");//匹配模式
        param2.setQueryType("wildcardQuery");//查询类型
        param2.setParam(date);
        params2.add(param2);
        Map<String,Object> dataMap2 = (Map<String, Object>) client.list(uploadVehicleSource,true,"",false,params2);
        List<Map<String, Object>> data2 = (List<Map<String, Object>>) dataMap2.get("obj");
        //根据过车记录统计车辆来源地数据
        List<VehicleSource> vehicleSourceList = tongjiVehicleSourceData(vehicleDataDetails,date);

        List<VehicleSource> uploadVehicleSourceList = new ArrayList<>();
        if(data2.size()!=0){
            data2.forEach(stringObjectMap ->
                uploadVehicleSourceList.add(JSONObject.parseObject(JSON.toJSONString(stringObjectMap),VehicleSource.class))
            );

            //合并相同的数据
            //单独保存，统计数据中上传的数据没有的，
            List<VehicleSource> tmpList = new ArrayList<>();
            for(VehicleSource v1:vehicleSourceList){
                for(VehicleSource v2:uploadVehicleSourceList){
                    if(v2.equals(v1)){
                        v2.setVehicleNum2(v1.getVehicleNum2());
                        tmpList.add(v1);
                    }
                }
            }
            //去重后合并
            vehicleSourceList.removeAll(tmpList);
            uploadVehicleSourceList.addAll(vehicleSourceList);
        }
        System.out.println("test");
        List<Map> mapList = uploadVehicleSourceList.stream().map(obj -> CommonUtils.convertToMap(obj)).collect(Collectors.toList());
        client.save(uploadVehicleSource,true,mapList);
    }

    public List<VehicleSource> tongjiVehicleSourceData(List<ParkVehicleDataDetail> vehicleDataDetails,String date){
        List<VehicleSource> list = new ArrayList<>();
        Map<String,String> parkNameScenicCodeMap = parkService.getParkScenicCodeMap2();
        //按照景区分组车辆
        Map<String,List<ParkVehicleDataDetail>> scenicVehicleData = new HashMap<>();
        vehicleDataDetails.forEach(parkVehicleDataDetail ->{
            if(null!=scenicVehicleData.get(parkVehicleDataDetail.getScenicCode())){
                scenicVehicleData.get(parkVehicleDataDetail.getScenicCode()).add(parkVehicleDataDetail);
            }else{
                scenicVehicleData.put(parkVehicleDataDetail.getScenicCode(),new ArrayList<>());
                scenicVehicleData.get(parkVehicleDataDetail.getScenicCode()).add(parkVehicleDataDetail);
            }
        });

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
                        vehicleSource.setDateStr(date);
                        vehicleSource.setMonthStr(date.substring(0,7));//2021-03-25
                        vehicleSource.setYearStr(date.substring(0,4));
                        list.add(vehicleSource1);
                    });
                }

            });
        }
        return  list;

    }





}
