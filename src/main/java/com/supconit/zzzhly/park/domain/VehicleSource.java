package com.supconit.zzzhly.park.domain;

import lombok.Data;

import java.util.Objects;

/**
 * @auther: jxp
 * @date: 2021/3/24 16:19
 * @description: 停车场车辆来源地
 */
@Data
public class VehicleSource {

    private String _id;
    private String name;
    private String scenicCode;
    private String sourceName;
    private Integer vehicleNum;//上报的来源地数据
    private Integer frequency;//省内0，省外1
    private String dateStr;
    private String monthStr;
    private String yearStr;
    private Integer vehicleNum2;//根据过车记录统计的车辆数据

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VehicleSource that = (VehicleSource) o;
        return
//                Objects.equals(name, that.name) &&
                Objects.equals(scenicCode, that.scenicCode) &&
                Objects.equals(sourceName, that.sourceName) &&
                Objects.equals(frequency, that.frequency) &&
                Objects.equals(dateStr, that.dateStr);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name,scenicCode, sourceName, frequency, dateStr);
    }
}
