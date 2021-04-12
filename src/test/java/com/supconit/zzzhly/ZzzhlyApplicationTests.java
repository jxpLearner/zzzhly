package com.supconit.zzzhly;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ZzzhlyApplicationTests {

    @Test
    public void testCalendar(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse("2021-04-01 00:23:00");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        calendar.add(Calendar.HOUR_OF_DAY,-1);//上一个小时,要考虑到当日0点需要统计前一日23点的数据
        String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        Integer hour = calendar.get(Calendar.HOUR_OF_DAY);

        System.out.println("test");
    }


}
