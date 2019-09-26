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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> linksPools = new ArrayList();
        Set<String> handledLinks = new HashSet<>();
        linksPools.add("https://sina.cn");
        while (true) {
            if (linksPools.isEmpty()) {
                break;
            }
            String link = linksPools.remove(linksPools.size() - 1);
            if (handledLinks.contains(link)) {
                continue;
            }
            if (isInterestedLink(link)) {
                Document document = httpGetAndParseHtml(link);
                document.select("a").stream().map(aTag -> aTag.attr("href")).forEach(linksPools::add);
                storeIntoDBIfItIsNewsPage(document);
                handledLinks.add(link);
            }
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

    private static Document httpGetAndParseHtml(String link) {
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
