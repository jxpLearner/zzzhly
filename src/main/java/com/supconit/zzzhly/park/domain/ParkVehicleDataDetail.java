package com.supconit.zzzhly.park.domain;

import lombok.Data;

/**
 * @auther: jxp
 * @date: 2021/3/16 15:16
 * @description: 停车场过车记录详情
 */
@Data
public class ParkVehicleDataDetail {

    private String name;//停车场名称
    private String code;//停车场编码
    private String vehicleNum;//车牌号
    private String passingTime;//过车时间
    private Integer inOrOut;//车辆进出场 1 进场 0 出场
    private String  date;
    private String hour;
    private String scenicCode;//所属景区编码

    //辅助数据字段
    private String vehicleNum_1;//车牌号前一位
    private String vehicleNum_1_name;//车辆对应省份
    private String vehicleNum_2;//车牌号前两位
    private String vehicleNum_2_name;//省内车辆对应市

}
