package com.supconit.zzzhly.park.services.impl;

import com.supconit.mc.api.ConfigClient;
import com.supconit.mc.entity.SearchCondition;
import com.supconit.zzzhly.common.ConfigClientLocal;
import com.supconit.zzzhly.park.services.service.ParkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @auther: jxp
 * @date: 2021/3/24 11:37
 * @description:
 */
@Service
public class ParkServiceImpl implements ParkService {

    @Value("${parkDataId}")
    private String parkDataId;
    @Autowired
    private ConfigClientLocal client;
//    private ConfigClient client;


    public Map<String, String> getParkScenicCodeMap() {
        Map<String, String> model = new HashMap<>();
        List<SearchCondition> parkParams = new ArrayList<>();
        Map<String, Object> parkDataMap = (Map<String, Object>) client.list(parkDataId, true, "", false, parkParams);
        List<Map<String,Object>> parkDataList = (List<Map<String, Object>>) parkDataMap.get("obj");
        parkDataList.forEach(stringObjectMap -> {
            model.put(stringObjectMap.get("name").toString(),stringObjectMap.get("scenicCode").toString());
        });
        return model;
    }

    public Map<String, String> getParkScenicCodeMap2() {
        Map<String, String> model = new HashMap<>();
        List<SearchCondition> parkParams = new ArrayList<>();
        Map<String, Object> parkDataMap = (Map<String, Object>) client.list(parkDataId, true, "", false, parkParams);
        List<Map<String,Object>> parkDataList = (List<Map<String, Object>>) parkDataMap.get("obj");
        parkDataList.forEach(stringObjectMap -> {
            model.put(stringObjectMap.get("scenicCode").toString(),stringObjectMap.get("name").toString());
        });
        return model;
    }

}
