package com.supconit.zzzhly.park.domain;

import lombok.Data;

import java.util.Objects;

/**
 * @auther: jxp
 * @date: 2021/3/29 9:53
 * @description: 车流量数据
 */
@Data
public class VehicleDetail {

    private String _id;   //数据的id，更新修改数据时需要加上
    private String name;        //停车场名称
    private String scenicCode;  //景区编码
    private String dateStr;     //日期
    private String hourStr;     //小时
    private String monthStr;    //月份
    private String yearStr;     //年
    private Integer vehicleNum;  //上报的车辆数据
    private Integer vehicleNum2; //根据过车记录统计的车辆数据

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleDetail that = (VehicleDetail) o;
        return Objects.equals(scenicCode, that.scenicCode) &&
                Objects.equals(dateStr, that.dateStr) &&
                Objects.equals(hourStr, that.hourStr);
    }

    @Override
    public int hashCode() {

        return Objects.hash(scenicCode, dateStr, hourStr);
    }
}
