package com.supconit.zzzhly.park.utils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.io.BaseEncoding;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * 签名工具
 *
 * @author keytop
 * @date 2020/3/6
 */
@Slf4j
public class SignUtils {
    /**
     * 参数签名
     * <p>
     * appId:4001
     * appSercert:85d15350778b11e9bbaa506b4b2f6421
     * <p>
     * 示例：
     * URL拼接：amount=100&freeMoney=100&freeTime=0&orderNo=闽C12345&outOrderNo=T20200306124536001&parkId=1000001&payMethod=6&paySource=4001&payTime=2020-03-06 10:57:22&payType=1006&payableAmount=200&reqId=891bc1480ee4414db3fafa7a63850f22&ts=1585018335847&85d15350778b11e9bbaa506b4b2f6421
     * 签名结果：8D148976BEEF3CEA96C955F438EAA388
     *
     * @param requestBody 参数对象
     * @param appSercert  秘钥
     * @return
     */
    public static String paramsSign(JSONObject requestBody, String appSercert) {
        TreeMap<String, String> params = new TreeMap<>();
        //过滤掉key，appId字段，空属性及Map或List等复杂对象
        requestBody.entrySet().stream().filter(p -> !"key".equals(p.getKey()) && !"appId".equals(p.getKey()) && p.getValue() != null && !"".equals(p.getValue()) && !(p.getValue() instanceof Map) && !(p.getValue() instanceof Iterable)).forEach(p -> params.put(p.getKey(), p.getValue().toString()));
        //拼接appSercert
        String temp = Joiner.on("&").withKeyValueSeparator("=").join(params).concat("&").concat(appSercert);
        log.info("URL拼接：" + temp);
        return md5(temp).toUpperCase();
    }

    /**
     * 请求头AUTH签名，已作废
     * 示例：
     *
     * @param appId      用户id
     * @param appSercert 秘钥
     * @param timestamp  时间戳
     * @return
     */
    public static String headerAuth(Integer appId, String appSercert, long timestamp) {
        String stringAuthTemp = appId + ":" + timestamp;
        return aesEcbPkcs5paddingEncrypt(stringAuthTemp, appSercert);
    }


    private static String aesEcbPkcs5paddingEncrypt(String plainText, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return BaseEncoding.base64Url().encode(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对文本执行 md5 摘要加密, 此算法与 mysql,JavaScript生成的md5摘要进行过一致性对比.
     *
     * @param plainText
     * @return 返回值中的字母为小写
     */
    private static String md5(String plainText) {
        if (null == plainText) {
            plainText = "";
        }
        String mD5Str = null;
        try {
            // JDK 支持以下6种消息摘要算法，不区分大小写
            // md5,sha(sha-1),md2,sha-256,sha-384,sha-512
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] b = md.digest();
            int i;
            StringBuilder builder = new StringBuilder(32);
            for (byte value : b) {
                i = value;
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    builder.append("0");
                }
                builder.append(Integer.toHexString(i));
            }
            mD5Str = builder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return mD5Str;
    }
}
