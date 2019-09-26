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
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> linksPools = new ArrayList();
        Set<String> handledLink = new HashSet<>();
        String beginLink = "https://sina.cn";
        linksPools.add(beginLink);
        while (true) {
            if (linksPools.isEmpty()) {
                break;
            }
            String link = linksPools.remove(linksPools.size() - 1);
            if (handledLink.contains(link)) {
                continue;
            }

            if ((link.contains("news.sina.cn") || link.equals(beginLink)) && !link.contains("passport.sina.cn") && !link.contains("hotnews.sina.cn")) {
                if(link.startsWith("//")) {
                    link = "https:" + link;
                }
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet(link);
                try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
                    System.out.println(response1.getStatusLine());
                    System.out.println(link);
                    HttpEntity entity1 = response1.getEntity();
                    Document document = Jsoup.parse(EntityUtils.toString(entity1));
                    Elements links = document.select("a");
                    for (Element aTag : links) {
                        linksPools.add(aTag.attr("href"));
                    }
                    ArrayList<Element> articleTags = document.select("article");
                    if(!articleTags.isEmpty()) {
                        for(Element acticleTag: articleTags ) {
                            System.out.println(acticleTag.child(0).text());
                        }
                    }
                    handledLink.add(link);
                }
            }
        }
    }
}
