package cn.huangchucai;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcCrawlerDao implements CrawlerDao{
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "root";


    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection("jdbc:h2:file:/Users/huangchucai/temp/java/java-crawler/news", USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNextLinkFromDatabase( String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
            return null;
        }
    }

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        String link = getNextLinkFromDatabase( "select link from LINKS_TO_BE_PROCESSED LIMIT 1");
        if (link != null) {
            updateDatabase( link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
        }
        return link;
    }


    private void updateDatabase( String link, String sql) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }

    }

    @Override
    public boolean isLinkProcessed( String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where LINK = ?")) {
            preparedStatement.setString(1, link);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }


    @Override
    public void insertNewsIntoDatabase(String title, String content, String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into news(TITLE, CONTENT, URL, CREATED_AT, MODIFIED_AT) values ( ?,?,?, now(), now() )")) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, content);
            preparedStatement.setString(3, link);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void insertLinkIntoAlreadyProcessed(String link) throws SQLException {
        updateDatabase(link, "insert into LINKS_ALREADY_PROCESSED(link) values (?)");
    }

    @Override
    public void insertLinkToBeProcessed(String href) throws SQLException {
        updateDatabase(href, "insert into LINKS_TO_BE_PROCESSED(link) values (?)");
    }

}
