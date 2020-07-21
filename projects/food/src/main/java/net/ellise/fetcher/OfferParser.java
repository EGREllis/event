package net.ellise.fetcher;

import java.util.List;

public interface OfferParser {
    void parse(List<Offer> offers, String source);
}
