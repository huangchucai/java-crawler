package cn.huangchucai;

import java.sql.SQLException;

public interface CrawlerDao {
    String getNextLinkThenDelete() throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    void insertNewsIntoDatabase(String title, String content, String link) throws SQLException;

    void insertLinkIntoAlreadyProcessed(String link) throws SQLException;

    void insertLinkToBeProcessed(String href) throws SQLException;
}
