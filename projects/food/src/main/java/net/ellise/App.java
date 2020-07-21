package net.ellise;

import net.ellise.fetcher.*;

import java.util.ArrayList;
import java.util.List;

public class App 
{
    public static void main( String[] args ) throws Exception
    {
        boolean isTesco = false;
        boolean isWaitrose = false;
        String query = null;
        for (int i = 0; i < args.length; i++) {
            System.out.println(String.format("%1$d) %2$s", i, args[i]));
            if ("-s".equals(args[i])) {
                String value = args[++i].toLowerCase();
                if ("tesco".equals(value)) {
                    isTesco = true;
                } else if ("waitrose".equals(value)) {
                    isWaitrose = true;
                } else if ("all".equals(value)) {
                    isTesco = true;
                    isWaitrose = true;
                }
            } else if ("-q".equals(args[i])) {
                query = args[++i];
            }
        }

        List<OfferFetcher> fetchers = new ArrayList<>();
        if (isTesco) {
            fetchers.add(new TescoFetcher(new TescoOfferParser()));
        }
        if (isWaitrose) {
            fetchers.add(new WaitroseFetcher(new WaitroseOfferParser()));
        }

        for (OfferFetcher fetcher : fetchers) {
            List<Offer> offers = fetcher.fetch(query);
            List<Offer> nonChicken = new ArrayList<>();
            int unavailable = 0;
            int available = 0;
            for (Offer offer : offers) {
                if (!offer.getName().toLowerCase().contains(query)) {
                    nonChicken.add(offer);
                } else if (offer.isAvailable()) {
                    System.out.println(offer);
                    available++;
                } else {
                    unavailable++;
                }
            }
            System.out.println(String.format("Available: %1$d\tUnavailable: %2$d\tNon-chicken: %3$d", available, unavailable, nonChicken.size()));
            for (Offer other : nonChicken) {
                System.out.println(other);
            }
        }
    }
}
