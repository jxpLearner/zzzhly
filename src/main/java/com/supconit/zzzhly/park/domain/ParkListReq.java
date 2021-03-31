package com.supconit.zzzhly.park.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @auther: jxp
 * @date: 2021/3/19 16:59
 * @description:
 */
@Data
@ApiModel("停车场列表")
public class ParkListReq extends BaseReq {
    @ApiModelProperty("页数")
    private Integer pageIndex;
    @ApiModelProperty("每页数量")
    private Integer pageSize;
}