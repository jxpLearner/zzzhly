package com.supconit.zzzhly.park.controllers;

import com.alibaba.fastjson.JSONObject;
import com.supconit.mc.api.ConfigClient;
import com.supconit.mc.entity.SearchCondition;
import com.supconit.zzzhly.common.ConfigClientLocal;
import com.supconit.zzzhly.common.PlateUtils;
import com.supconit.zzzhly.common.httpclient.HttpClientService;
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
    private HttpClientService httpClient;

    @Autowired
    private ConfigClient client;
//    private ConfigClientLocal client;

    /**
     * 根据停车场的过车记录，统计分析车流量以及车辆来源地数据
     *
     * @param date       查询的日期，年/月
     * @param scenicName 景区名称，默认为空，查询全部
     * @return
     */
    @GetMapping("getParkAnalysisData")
    @ResponseBody
    @ApiOperation(value = "获取车流量的统计信息", tags = "", notes = "获取车流量的统计信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "date", value = "年/月", required = true),
            @ApiImplicitParam(name = "scenicName", value = "景区名称")
    })
    public Map<String, Object> parkDataAnalysis(String date, String scenicName) {
        Map<String, Object> model = new HashMap<>();

        //获取所有的停车场
        List<SearchCondition> parkParams = new ArrayList<>();
        Map<String, Object> parklist = (Map<String, Object>) client.list(parkDataId, true, "", false, parkParams);
//        Map<String,Object> parklist = (Map<String,Object>)client.list("PARK",true,"",false,parkParams);


        return model;
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
