package net.ellise.fetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TescoOfferParser implements OfferParser {
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
                String weightUnit = null;
                for (String pairSource : pairs) {
                    String[] pair = pairSource.split(":");
                    if ("\"title\"".equals(pair[0])) {
                        name = pair[1];
                        if (name == null) {
                            throw new NullPointerException();
                        }
                    }
                    if ("\"price\"".equals(pair[0])) {
                        price = Double.parseDouble(pair[1]);
                        if (price == null) {
                            throw new NullPointerException();
                        }
                    }
                    if ("\"unitPrice\"".equals(pair[0])) {
                        unitPrice = Double.parseDouble(pair[1]);
                        if (unitPrice == null) {
                            throw new NullPointerException();
                        }
                    }
                    if ("\"status\"".equals(pair[0])) {
                        available = "\"AvailableForSale\"".equals(pair[1]);
                        if (available == null) {
                            throw new NullPointerException();
                        }
                    }
                    if ("\"unitOfMeasure\"".equals(pair[0])) {
                        weightUnit = pair[1];
                    }
                }
                if (available) {
                    if (unitPrice == null) {
                        throw new NullPointerException();
                    }
                    if (price == null) {
                        throw new NullPointerException();
                    }
                    Double weight = price / unitPrice;
                    offer = new Offer(name, price, weight, weightUnit, available);
                    offers.add(offer);
                } else {
                    offer = new Offer(name, null, null, weightUnit, available);
                    offers.add(offer);
                }
            } else if (multiMatcher.matches()) {
                String pairs[] = multiMatcher.group(1).concat(multiMatcher.group(3)).split(",");
                String name = null;
                Boolean available = null;
                String weightUnit = null;
                for (String pairSource : pairs) {
                    String[] pair = pairSource.split(":");
                    if ("\"title\"".equals(pair[0])) {
                        name = pair[1];
                    }
                    if ("\"status\"".equals(pair[0])) {
                        available = "\"AvailableForSale\"".equals(pair[1]);
                    }
                    if ("\"unitOfMeasure\"".equals(pair[0])) {
                        weightUnit = pair[1];
                    }
                }
                for (String entry : multiMatcher.group(2).split("\\},\\{")) {
                    Pattern entryPattern = Pattern.compile(".*\"price\":([^,]+),\"weight\":([0-9\\.]+)\\}*");
                    Matcher entryMatcher = entryPattern.matcher(entry);
                    if (entryMatcher.matches()) {
                        Double price = Double.parseDouble(entryMatcher.group(1));
                        Double weight = Double.parseDouble(entryMatcher.group(2));
                        Offer multiOffer = new Offer(name, price, weight, weightUnit, available);
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
