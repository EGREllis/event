package net.ellise.fetcher;

import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TescoFetcherTest {
    private static String TEST_PATH = "./src/test/resources/";

    @Test
    public void test_canParseTescoProductSource() {
        try (Reader fileReader = new FileReader(TEST_PATH+"chicken.html")) {
            BufferedReader input = new BufferedReader(fileReader);
            String source = input.readLine();
            String nextLine;
            while ( (nextLine = input.readLine()) != null ) {
                source = source+nextLine;
            }

            List<Offer> offers = new ArrayList<>();
            TescoFetcher fetcher = new TescoFetcher();
            fetcher.parse(offers, source);

            assert offers.size() != 0 : "No offers";
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
