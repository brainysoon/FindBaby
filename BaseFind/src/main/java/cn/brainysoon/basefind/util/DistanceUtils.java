package cn.brainysoon.basefind.util;

/**
 * Created by brainy on 17-6-3.
 */
public class DistanceUtils {

    /**
     * 返回两坐标的距离 单位位米
     *
     * @param Long1
     * @param Lat1
     * @param Long2
     * @param Lat2
     * @return
     */
    public static float calculateLineDistance(Double Long1, Double Lat1, Double Long2, Double Lat2) {
        double var2 = 0.01745329251994329D;
        double var4 = Long1;
        double var6 = Lat1;
        double var8 = Long2;
        double var10 = Lat2;
        var4 *= 0.01745329251994329D;
        var6 *= 0.01745329251994329D;
        var8 *= 0.01745329251994329D;
        var10 *= 0.01745329251994329D;
        double var12 = Math.sin(var4);
        double var14 = Math.sin(var6);
        double var16 = Math.cos(var4);
        double var18 = Math.cos(var6);
        double var20 = Math.sin(var8);
        double var22 = Math.sin(var10);
        double var24 = Math.cos(var8);
        double var26 = Math.cos(var10);
        double[] var28 = new double[3];
        double[] var29 = new double[3];
        var28[0] = var18 * var16;
        var28[1] = var18 * var12;
        var28[2] = var14;
        var29[0] = var26 * var24;
        var29[1] = var26 * var20;
        var29[2] = var22;
        double var30 = Math.sqrt((var28[0] - var29[0]) * (var28[0] - var29[0]) + (var28[1] - var29[1]) * (var28[1] - var29[1]) + (var28[2] - var29[2]) * (var28[2] - var29[2]));
        return (float) (Math.asin(var30 / 2.0D) * 1.27420015798544E7D);
    }
}
