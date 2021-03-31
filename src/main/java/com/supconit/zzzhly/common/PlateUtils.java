package com.supconit.zzzhly.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @auther: jxp
 * @date: 2021/3/8 15:48
 * @description:
 */
public class PlateUtils {

    //全国
    String COUNTRY_PREFIX_ARRAY[] = {"京", "津", "冀", "晋", "蒙", "辽", "吉", "黑", "沪", "苏", "浙", "皖", "闽", "赣", "鲁", "豫", "鄂", "湘", "粤", "桂", "琼", "渝", "川", "贵", "云", "藏", "陕", "甘", "青", "宁", "新", "台",};
//    String PROVINCE_ARRAY[] = {"北京市", "天津", "河北", "山西", "内蒙古", "辽宁", "吉林", "黑龙江", "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东", "河南", "湖北", "湖南", "广东", "广西", "海南", "重庆", "四川", "贵州", "云南", "西藏", "陕西", "甘肃", "青海", "宁夏", "新疆", "台湾"};
    String PROVINCE_ARRAY[] = {"北京市","天津市","河北省","山西省","内蒙古自治区","辽宁省","吉林省","黑龙江省","上海市","江苏省","浙江省","安徽省","福建省","江西省","山东省","河南省","湖北省","湖南省","广东省","广西壮族自治区","海南省","重庆市","四川省","贵州省","云南省","西藏自治区","陕西省","甘肃省","青海省","宁夏回族自治区","新疆维吾尔自治区","台湾省","香港","澳门"};

    //福建省
    String CITY[] = {"福州市","莆田市","泉州市","厦门市","漳州市","龙岩市","三明市","南平市","宁德市"};
    String CITYCODE[] = {"闽A","闽B","闽C","闽D","闽E","闽F","闽G","闽H","闽J"};

    Map<String,String> provinceMap = new HashMap<>();
    Map<String,String> cityMap = new HashMap<>();
    public void createMap(){
        for(int i=0;i<COUNTRY_PREFIX_ARRAY.length;i++){
            provinceMap.put(COUNTRY_PREFIX_ARRAY[i],PROVINCE_ARRAY[i]);
        }
        for(int i=0;i<CITY.length;i++){
            cityMap.put(CITYCODE[i],CITY[i]);
        }
    }

    public String getProvinceName(String plate){
        createMap();
        return provinceMap.get(plate);
    }

    public String getCityName(String plate){
        createMap();
        return null==cityMap.get(plate)?"":cityMap.get(plate);
    }


}
