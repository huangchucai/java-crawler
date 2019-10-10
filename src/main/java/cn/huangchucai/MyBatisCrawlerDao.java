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
    private final String resource = "db/mybatis/config.xml";
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
    public String getNextLinkThenDelete() {
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
            if (count != 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void insertNewsIntoDatabase(String title, String content, String link) throws SQLException {
        try(SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("cn.huangchucai.mybatisMapper.insertNews", new News(title, content, link));
        }

    }

    @Override
    public void insertLinkIntoAlreadyProcessed(String link) throws SQLException {
        HashMap hashMap = new HashMap();
        hashMap.put("tableName", "LINKS_ALREADY_PROCESSED");
        hashMap.put("link", link);
        try(SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("cn.huangchucai.mybatisMapper.insertLink", hashMap);
        }
    }

    @Override
    public void insertLinkToBeProcessed(String href) throws SQLException {
        HashMap hashMap = new HashMap();
        hashMap.put("tableName", "LINKS_TO_BE_PROCESSED");
        hashMap.put("link", href);
        try(SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("cn.huangchucai.mybatisMapper.insertLink", hashMap);
        }
    }
}
