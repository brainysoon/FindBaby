package cn.brainysoon.basefind.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by brainy on 17-6-2.
 */
public class RandomUtils {

    /**
     * 17位的日期前缀
     */
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    public static final Random random = new Random();

    public static String randomId20() {

        String pre = sdf.format(new Date());

        StringBuffer subBuffer = new StringBuffer(pre);

        for (int i = 0; i < 3; i++) {

            int num = Math.abs(random.nextInt()) % 10;

            subBuffer.append(num);
        }

        return subBuffer.toString();
    }
}
