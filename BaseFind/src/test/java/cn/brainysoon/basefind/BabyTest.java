package cn.brainysoon.basefind;

import cn.brainysoon.basefind.Model.Baby;
import cn.brainysoon.basefind.config.BaseFindWebAppInitializer;
import cn.brainysoon.basefind.dao.BabyRepository;
import cn.brainysoon.basefind.util.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;

/**
 * Created by brainy on 17-6-2.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BaseFindWebAppInitializer.class})
public class BabyTest {

    @Autowired
    private BabyRepository babyRepository;

    @Test
    public void insertBabyTest() {

        Baby baby = new Baby();

        //准备数据
        baby.setBabyId(RandomUtils.randomId20());
        baby.setBabyName("宝贝" + RandomUtils.random.nextInt());
        baby.setBabyClass("电子");
        baby.setBabyFindTime(new Date());
        baby.setBabyLookLike("baby.jpg");
        baby.setBabyLong(Math.abs(RandomUtils.random.nextDouble()));
        baby.setBabyLat(Math.abs(RandomUtils.random.nextDouble()));
        baby.setBabyPassSecret("回答问题？");
        baby.setBabySecretKey("123");
        baby.setBabyUpdateTime(new Date());
        baby.setBabyFindMark(-1);
        baby.setBabyMark(1);
        baby.setBabySlead(1);

        int result = babyRepository.insertBaby(baby);

        Assert.assertEquals(result, 1);
    }
}
