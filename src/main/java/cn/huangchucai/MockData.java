package cn.huangchucai;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockData {


    public static void mockHowManyData(SqlSessionFactory sqlSessionFactory, Integer manyCount) {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> newsList = session.selectList("cn.huangchucai.mockDataMapper.selectNews");
            int count = manyCount - newsList.size();
            Random random = new Random();

            try {
                while (count-- > 0) {
                    System.out.println("剩余多少条: " + count);
                    if(count % 2000 == 0) {
                        session.flushStatements();
                    }
                    int index = random.nextInt(newsList.size());
                    News newsToBeInsert = new News(newsList.get(index));

                    Instant currentTime = newsToBeInsert.getCreatedAt();
                    currentTime = currentTime.minusSeconds(random.nextInt(3600 * 24 * 365));
                    newsToBeInsert.setCreatedAt(currentTime);
                    newsToBeInsert.setModifiedAt(currentTime);

                    session.insert("cn.huangchucai.mockDataMapper.insertNews", newsToBeInsert);
                }
                session.commit();
            } catch (Exception e){
                session.rollback();
                throw new RuntimeException(e);
            }
        }
    }
    public static void main(String[] args) {
        String resource = "db/mybatis/config.xml";
        InputStream inputStream = null;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

        mockHowManyData(sqlSessionFactory, 100_0000);

    }
}
