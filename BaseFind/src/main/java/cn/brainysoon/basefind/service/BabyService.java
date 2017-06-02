package cn.brainysoon.basefind.service;

import cn.brainysoon.basefind.Model.Baby;

import java.util.List;

/**
 * Created by brainy on 17-6-2.
 */
public interface BabyService {

    /**
     * @param Long
     * @param Lat
     * @param Circle
     * @return
     */
    List<Baby> getBabysByLongLatAndCircle(Double Long, Double Lat, Double Circle);

    int addBaby(String babyname, String babyclass, String passsecret, String secretkey, String findphone, Double babylong, Double babylat);
}
