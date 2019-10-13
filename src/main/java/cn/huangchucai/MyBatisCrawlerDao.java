package cn.huangchucai;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;

public class MyBatisCrawlerDao implements CrawlerDao {
    private static final String resource = "db/mybatis/config.xml";
    private SqlSessionFactory sqlSessionFactory;

    public MyBatisCrawlerDao() {
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    @Override
    public synchronized String getNextLinkThenDelete() {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String link = session.selectOne("cn.huangchucai.mybatisMapper.selectNextAvailable");
            if (link != null) {
                session.delete("cn.huangchucai.mybatisMapper.deleteAlreadyLink", link);
                return link;
            }
            return null;
        }
    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Integer count = session.selectOne("cn.huangchucai.mybatisMapper.isLinkProcessed");
            return count != 0;
        }
    }

    @Override
    public void insertNewsIntoDatabase(String title, String content, String link) throws SQLException {
        try(SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("cn.huangchucai.mybatisMapper.insertNews", new News(title, content, link));
        }

    }

    @Override
    public void insertLinkIntoAlreadyProcessed(String link) {
        HashMap<String, String> params = new HashMap<>();
        params.put("tableName", "LINKS_ALREADY_PROCESSED");
        params.put("link", link);
        try(SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("cn.huangchucai.mybatisMapper.insertLink", params);
        }
    }

    @Override
    public void insertLinkToBeProcessed(String href) {
        HashMap<String, String> params = new HashMap<>();
        params.put("tableName", "LINKS_TO_BE_PROCESSED");
        params.put("link", href);
        try(SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("cn.huangchucai.mybatisMapper.insertLink", params);
        }
    }
}
