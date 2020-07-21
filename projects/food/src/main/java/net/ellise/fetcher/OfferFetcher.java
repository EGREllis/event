package net.ellise.fetcher;

import java.util.List;

public interface OfferFetcher {
    List<Offer> fetch(String query) throws Exception;
}
