package cn.brainysoon.basefind.service.impl;

import cn.brainysoon.basefind.Model.Baby;
import cn.brainysoon.basefind.dao.BabyRepository;
import cn.brainysoon.basefind.service.BabyService;
import cn.brainysoon.basefind.util.DistanceUtils;
import cn.brainysoon.basefind.util.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by brainy on 17-6-2.
 */
@Service
public class BabyServiceImpl implements BabyService {

    @Autowired
    private BabyRepository babyRepository;

    @Override
    public List<Baby> getBabysByLongLatAndCircle(Double Long, Double Lat, Double Circle) {
        List<Baby> babies = babyRepository.queryBaby();

        List<Baby> result = new ArrayList<>();

        for (Baby baby : babies) {

            Float distance = DistanceUtils.calculateLineDistance(Long, Lat, baby.getBabyLong(), baby.getBabyLat());

            //在范围内
            if (distance <= Circle) {

                result.add(baby);
            }
        }

        return result;
    }

    @Override
    public int addBaby(String babyname, String babyclass, String passsecret, String secretkey, String findphone, Double babylong, Double babylat) {

        Baby baby = new Baby();

        baby.setBabyId(RandomUtils.randomId20());
        baby.setBabyName(babyname);
        baby.setBabyClass(babyclass);
        baby.setBabyFindTime(new Date());
        baby.setBabyLookLike("baby.jpg");
        baby.setBabyLong(babylong);
        baby.setBabyLat(babylat);
        baby.setBabyPassSecret(passsecret);
        baby.setBabySecretKey(secretkey);
        baby.setBabyUpdateTime(new Date());
        baby.setBabyFindMark(-1);
        baby.setBabyMark(1);
        baby.setBabySlead(1);
        baby.setBabyFindPhone(findphone);

        return babyRepository.insertBaby(baby);
    }
}
