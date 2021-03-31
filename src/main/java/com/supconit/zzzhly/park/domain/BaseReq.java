package com.supconit.zzzhly.park.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础的请求参数
 *
 * @author daishaokun
 * @date 2020/3/5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("基础的请求参数")
public class BaseReq {
    @ApiModelProperty("授权码")
    private Integer appId;
    @ApiModelProperty("验证码")
    private String key;
    @ApiModelProperty("车场id")
    private String parkId;
    @ApiModelProperty("业务代码(?)")
    private String serviceCode;
    @ApiModelProperty("请求时间")
    private Long ts;
    @ApiModelProperty("每次请求唯一标识")
    private String reqId;
}