package com.supconit.zzzhly.park.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 费用查询参数
 *
 * @author daishaokun
 * @date 2020/3/5
 */
@Data
@ApiModel("费用查询参数")
public class FeeQueryReq extends BaseReq {
    @ApiModelProperty("车牌号")
    private String plateNo;
    @ApiModelProperty("入场取票号/无牌车入场的卡号")
    private String cardNo;
    @ApiModelProperty("设备编码(出口) 设备编码仅支持出口有停放车辆,并识别正确的情况不支持无牌车")
    private String deviceCode;
    @ApiModelProperty("免费时长（秒），云停用")
    private Long freeMoney;
    @ApiModelProperty("免费金额（单位:分），云停用")
    private Integer freeTime;
}