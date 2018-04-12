package net.ellise.fetcher;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TescoFetcher {
    private static final String TESCO_BASE = "https://www.tesco.com/groceries/en-GB/search?query=%1$s&page=%2$d";
    public List<Offer> fetch(String query) throws Exception {
        System.setProperty("webdriver.chrome.driver", "/Users/edward/Downloads/chromedriver");
        WebDriver webDriver = new ChromeDriver();

        Pattern showingPattern = Pattern.compile(".*Showing\\D*(\\d+)-(\\d+).*(\\d+) items.*");
        List<Offer> offers = new ArrayList<>();
        for (int page = 1; page < 30; page++) {
            System.out.println(String.format("Fetching webpage %1$d at %2$s... ", page, new Date()));
            webDriver.get(String.format(TESCO_BASE, query, page));
            String source = webDriver.getPageSource();
            System.out.println(String.format("Fetched webpage %1$d at %2$s... ", page, new Date()));
            source = source.replaceAll("&quot;", "\"");

            Matcher showingMatcher = showingPattern.matcher(source);
            if (showingMatcher.matches()) {
                System.out.println(String.format("\tParsed %1$s - %2$s of %3$s", showingMatcher.group(1), showingMatcher.group(2), showingMatcher.group(3)));
            } else {
                System.out.println("\tShowing did not match.");
            }

            try (Writer chicken = new FileWriter(String.format("./chicken_page_%1$d.html", page))) {
                chicken.write(source);
            }
            System.out.flush();

            parse(offers, source);
        }
        //webDriver.close();
        //webDriver.quit();
        return offers;
    }

    public void parse(List<Offer> offers, String source) {
        List<String> offerSources = new ArrayList<>();
        addOfferSource(offerSources, source);
        for (String offerSource : offerSources) {
            offers.addAll(parseOffer(offerSource));
        }
    }

    private List<Offer> parseOffer(String offerSource) {
        List<Offer> offers = new ArrayList<>();
        try {
            Pattern singleOfferPattern = Pattern.compile(".*\"product\":\\{([^\\}]+),\"catchWeightList\":null,([^\\}]*)\\}.*");
            Pattern multiOfferPattern = Pattern.compile(".*\"product\":\\{([^\\}]+),\"catchWeightList\":\\[([^\\]]+)\\],([^\\}]*)\\}.*");
            Matcher singleMatcher = singleOfferPattern.matcher(offerSource);
            Matcher multiMatcher = multiOfferPattern.matcher(offerSource);
            Offer offer;
            if (singleMatcher.matches()) {
                String[] pairs = singleMatcher.group(1).concat(singleMatcher.group(2)).split(",");
                String name = null;
                Double price = null;
                Double unitPrice = null;
                Boolean available = null;
                for (String pairSource : pairs) {
                    String[] pair = pairSource.split(":");
                    if ("\"title\"".equals(pair[0])) {
                        name = pair[1];
                    }
                    if ("\"price\"".equals(pair[0])) {
                        price = Double.parseDouble(pair[1]);
                    }
                    if ("\"unitPrice\"".equals(pair[0])) {
                        unitPrice = Double.parseDouble(pair[1]);
                    }
                    if ("\"status\"".equals(pair[0])) {
                        available = "\"AvailableForSale\"".equals(pair[1]);
                    }
                }
                if (available) {
                    Double weight = price / unitPrice;
                    offer = new Offer(name, price, weight, available);
                    offers.add(offer);
                } else {
                    offer = new Offer(name, null, null, available);
                    offers.add(offer);
                }
            } else if (multiMatcher.matches()) {
                String pairs[] = multiMatcher.group(1).concat(multiMatcher.group(3)).split(",");
                String name = null;
                Boolean available = null;
                for (String pairSource : pairs) {
                    String[] pair = pairSource.split(":");
                    if ("\"title\"".equals(pair[0])) {
                        name = pair[1];
                    }
                    if ("\"status\"".equals(pair[0])) {
                        available = "\"AvailableForSale\"".equals(pair[1]);
                    }
                }
                for (String entry : multiMatcher.group(2).split("\\},\\{")) {
                    Pattern entryPattern = Pattern.compile(".*\"price\":([^,]+),\"weight\":([0-9\\.]+)\\}*");
                    Matcher entryMatcher = entryPattern.matcher(entry);
                    if (entryMatcher.matches()) {
                        Double price = Double.parseDouble(entryMatcher.group(1));
                        Double weight = Double.parseDouble(entryMatcher.group(2));
                        Offer multiOffer = new Offer(name, price, weight, available);
                        offers.add(multiOffer);
                    }
                }
            } else {
                System.err.println("SOURCE NOT RECOGNISED: " + offerSource);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            System.err.println("OFFENDING SOURCE WAS:\n"+offerSource+"\n");
            System.err.flush();
        }
        return offers;
    }

    private void addOfferSource(List<String> offerSources, String source) {
        int sourceIndex = source.indexOf("\"productItems\":[");
        int sourceStart = source.indexOf('[', sourceIndex);
        int sourceStop = matchingCloseBracketIndex(source, sourceStart);
        String offersOnly = source.substring(sourceStart, sourceStop+1);


        int startIndex = offersOnly.indexOf("{") - 1;
        while ( (startIndex = offersOnly.indexOf("\"product\":{", startIndex + 1)) > 0 ) {
            String thisSource = offersOnly.substring(startIndex);
            int stopIndex = matchingCloseBracketIndex(thisSource, 0);
            String offer = thisSource.substring(0, stopIndex);
            offerSources.add(offer);
        }
    }

    private int matchingCloseBracketIndex(String source, int start) {
        int count = 1;
        int index = 0;
        while (count > 0 && source.length() > start + index + 1) {
            char c = source.charAt(start + ++index);
            switch(c) {
                case '<':
                case '{':
                case '[':
                    count++;
                    break;
                case '>':
                case '}':
                case ']':
                    count--;
                    break;
                default:
                    //Do nothing
                    break;
            }
        }
        switch(source.charAt(start+index)) {
            case ']':
            case '}':
            case ')':
                return start+index;
            default:
                return -1;
        }
    }
}
