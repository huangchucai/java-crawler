package cn.huangchucai;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Crawler extends Thread {
    CrawlerDao dao;

    public Crawler(CrawlerDao dao) {
        this.dao = dao;
    }

    public void run() {
        try {
            String link;
            while ((link = dao.getNextLinkThenDelete()) != null) {
                if (dao.isLinkProcessed(link)) {
                    continue;
                }
                if (isInterestedLink(link)) {
                    System.out.println(link);
                    Document document = httpGetAndParseHtml(link);
                    parseUrlsFromPageAndStoreIntoDatabase(document);
                    storeIntoDBIfItIsNewsPage(link, document);
                    dao.insertLinkIntoAlreadyProcessed(link);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void parseUrlsFromPageAndStoreIntoDatabase(Document document) throws SQLException {
        for (Element aTag : document.select("a")) {
            String href = aTag.attr("href");

            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (!href.equals("") && !href.startsWith("javascript") && !href.startsWith("#")) {
                dao.insertLinkToBeProcessed(href);
            }
        }
    }


    private void storeIntoDBIfItIsNewsPage(String link, Document document) throws SQLException {
        ArrayList<Element> articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            for (Element acticleTag : articleTags) {
                String title = acticleTag.child(0).text();

                String content = acticleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));

                dao.insertNewsIntoDatabase(title, content, link);
            }
        }
    }


    private boolean isInterestedLink(String link) {
        return (link.contains("news.sina.cn") || link.equals("https://sina.cn")) && !link.contains("passport.sina.cn") && !link.contains("hotnews.sina.cn");
    }

    private Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            return Jsoup.parse(html);
        }
    }
}
