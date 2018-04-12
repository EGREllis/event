package net.ellise;

import net.ellise.fetcher.Offer;
import net.ellise.fetcher.TescoFetcher;

import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        TescoFetcher fetcher = new TescoFetcher();
        List<Offer> offers = fetcher.fetch("Chicken");
        int unavailable = 0;
        int available = 0;
        for (Offer offer : offers) {
            if (offer.isAvailable()) {
                System.out.println(offer);
                available++;
            } else {
                unavailable++;
            }
        }
        System.out.println(String.format("Available: %1$d\tUnavailable: %2$d", available, unavailable));
    }
}
