package com.supconit.zzzhly.park.domain;

import lombok.Data;

/**
 * @auther: jxp
 * @date: 2021/3/17 17:24
 * @description: 停车场基础信息
 */
@Data
public class ParkData {

    private String name;    //停车场名称
    private String code;    //停车场编码
    private String scenicCode;    //所属景区编码
    private String address; //地址
    private Integer total;  //总停车位
    private Integer surplus;//剩余停车位
}
