package com.jcl.map_demo.Util;

public class DistanceUtil {

    private static final double EARTH_RADIUS = 6378.137;

    public static double getDistances(double longitude1, double latitude1,
                               double longitude2, double latitude2) {
        double Lat1 = rad(latitude1);
        double Lat2 = rad(latitude2);
        double a = Lat1 - Lat2;
        double b = rad(longitude1) - rad(longitude2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(Lat1) * Math.cos(Lat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;

        s = Math.round(s * 10000d) / 10000d;
        s = s * 1000;//单位：米
//        s = Math.round(s/10d) /100d   ;//单位：千米 保留两位小数
        s = Math.round(s / 100d) / 10d;//单位：千米 保留一位小数
        return s;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }
}
