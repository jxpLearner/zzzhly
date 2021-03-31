package com.supconit.zzzhly.park.services.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @auther: jxp
 * @date: 2021/3/24 11:36
 * @description:
 */

public interface ParkService {
    public Map<String, String> getParkScenicCodeMap();

    public Map<String, String> getParkScenicCodeMap2();
}
