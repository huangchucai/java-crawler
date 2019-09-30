package cn.huangchucai;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:file:/Users/huangchucai/temp/java/java-crawler/news", "root", "root");
        String link;
        while ((link = getNextLinkThenDelete(connection)) != null) {
            if (isLinkProcessed(connection, link)) {
                continue;
            }
            if (isInterestedLink(link)) {
                Document document = httpGetAndParseHtml(link);
                parseUrlsFromPageAndStoreIntoDatabase(connection, document);
                storeIntoDBIfItIsNewsPage(document);
                updateDatabase(connection, link, "insert into LINKS_ALREADY_PROCESSED(link) values (?)");
            }
        }
    }

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String link = loadByUrlFromDatabase(connection, "select link from LINKS_TO_BE_PROCESSED LIMIT 1");
        if (link != null) {
            updateDatabase(connection, link, "delete from LINKS_TO_BE_PROCESSED where link = ?");
        }
        return link;
    }

    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document document) throws SQLException {
        for (Element aTag : document.select("a")) {
            String href = aTag.attr("href");
            updateDatabase(connection, href, "insert into LINKS_TO_BE_PROCESSED(link) values (?)");
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
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

    private static void updateDatabase(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }

    }

    private static String loadByUrlFromDatabase(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
            return null;
        }
    }

    private static void storeIntoDBIfItIsNewsPage(Document document) {
        ArrayList<Element> articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            for (Element acticleTag : articleTags) {
                System.out.println(acticleTag.child(0).text());
            }
        }
    }


    private static boolean isInterestedLink(String link) {
        return (link.contains("news.sina.cn") || link.equals("https://sina.cn")) && !link.contains("passport.sina.cn") && !link.contains("hotnews.sina.cn");
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        if (link.startsWith("//")) {
            link = "https:" + link;
        }
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }
}
