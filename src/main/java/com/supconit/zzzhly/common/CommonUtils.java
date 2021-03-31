package com.supconit.zzzhly.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CommonUtils {

    public static Map<String, Object> convertToMap(Object obj) {
        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(obj),
                new TypeReference<Map<String, Object>>() {
                });
        return map;
    }

    /**
     * 将Object对象里面的属性和值转化成Map对象
     *
     * @param obj
     * @return
     **/
    public static Map<String, Object> objectToMap(Object obj)  {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = null;
            try {
                if(null==field.get(obj) || "".equals(field.get(obj).toString()))continue;
                value = field.get(obj).toString();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            map.put(fieldName, value);
        }
        return map;
    }
}

