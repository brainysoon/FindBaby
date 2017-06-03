package cn.brainysoon.basefind.dao.impl;

import cn.brainysoon.basefind.Model.Baby;
import cn.brainysoon.basefind.dao.BabyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by brainy on 17-6-2.
 */
@Repository
public class BabyRepositoryImpl implements BabyRepository, RowMapper<Baby> {

    private static final String BABY_ID = "BabyId";
    private static final String BABY_NAME = "BabyName";
    private static final String BABY_CLASS = "BabyClass";
    private static final String BABY_FIND_TIME = "BabyFindTime";
    private static final String BABY_LOOK_LIKE = "BabyLookLike";
    private static final String BABY_LONG = "BabyLong";
    private static final String BABY_LAT = "BabyLat";
    private static final String BABY_PASS_SECRET = "BabyPassSecret";
    private static final String BABY_SECRET_KEY = "BabySecretKey";
    private static final String BABY_UPDATE_TIME = "BabyUpdateTime";
    private static final String BABY_FIND_MARK = "BabyFindMark";
    private static final String BABY_MARK = "BabyMark";
    private static final String BABY_SLEAD = "BabySlead";
    private static final String BABY_FIND_PHONE = "BabyFindPhone";

    @Autowired
    private JdbcOperations jdbcOperations;

    @Override
    public Baby mapRow(ResultSet rs, int rowNum) throws SQLException {

        Baby baby = new Baby();

        //map
        baby.setBabyId(rs.getString(BABY_ID));
        baby.setBabyName(rs.getString(BABY_NAME));
        baby.setBabyClass(rs.getString(BABY_CLASS));
        baby.setBabyFindTime(rs.getDate(BABY_FIND_TIME));
        baby.setBabyLookLike(rs.getString(BABY_LOOK_LIKE));
        baby.setBabyLong(rs.getDouble(BABY_LONG));
        baby.setBabyLat(rs.getDouble(BABY_LAT));
        baby.setBabyPassSecret(rs.getString(BABY_PASS_SECRET));
        baby.setBabySecretKey(rs.getString(BABY_SECRET_KEY));
        baby.setBabyUpdateTime(rs.getDate(BABY_UPDATE_TIME));
        baby.setBabyFindMark(rs.getInt(BABY_FIND_MARK));
        baby.setBabyMark(rs.getInt(BABY_MARK));
        baby.setBabySlead(rs.getInt(BABY_SLEAD));
        baby.setBabyFindPhone(rs.getString(BABY_FIND_PHONE));

        return baby;
    }

    public int insertBaby(Baby baby) {

        return jdbcOperations.update(INSERT_BABY,
                baby.getBabyId(),
                baby.getBabyName(),
                baby.getBabyClass(),
                baby.getBabyFindTime(),
                baby.getBabyLookLike(),
                baby.getBabyLong(),
                baby.getBabyLat(),
                baby.getBabyPassSecret(),
                baby.getBabySecretKey(),
                baby.getBabyUpdateTime(),
                baby.getBabyFindMark(),
                baby.getBabyMark(),
                baby.getBabySlead(),
                baby.getBabyFindPhone());
    }

    public List<Baby> queryBaby() {

        return jdbcOperations.query(QUERY_BABY_BASE_LONG_LAT_CIRCLE, this);
    }
}
