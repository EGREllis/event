package net.ellise.fetcher;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SainsburysFetcher implements OfferFetcher {
    private static final String WAITROSE_BASE = "https://www.waitrose.com/ecom/shop/search?defaultSearch=None&searchTerm=chicken";

    private final OfferParser offerParser;

    public SainsburysFetcher(OfferParser offerParser) {
        this.offerParser = offerParser;
    }

    public List<Offer> fetch(String query) throws Exception {
        System.setProperty("webdriver.chrome.driver", "/Users/edward/Downloads/chromedriver");
        WebDriver webDriver = new ChromeDriver();

        List<Offer> offers = new ArrayList<>();
        List<Offer> current = new ArrayList<>();

        int page = 1;
        do {
            current.clear();
            String url = String.format(WAITROSE_BASE, query, page);
            System.out.println(String.format("Fetching webpage %1$d at %2$s... ", page, new Date()));
            webDriver.get(url);
            String source = webDriver.getPageSource();
            System.out.println(String.format("Fetched webpage %1$d at %2$s... ", page, new Date()));
            source = source.replaceAll("&quot;", "\"");

            try (Writer chicken = new FileWriter(String.format("./waitrose_chicken_page_%1$d.html", page))) {
                chicken.write(source);
            }
            System.out.flush();

            offerParser.parse(current, source);
            offers.addAll(current);
            page++;
        } while (current.size() > 0);

        webDriver.close();
        webDriver.quit();
        return offers;
    }
}
