package nz.ac.canterbury.seng302.homehelper.data;

import java.util.stream.Stream;

public class PriceTestData {


    /**
     * Prices that are invalid for an expense
     *
     * @return a stream of invalid prices for an expense
     */
    static Stream<String> invalidPriceInputs() {
        return Stream.of(
                "-1",
                "3.222",
                "-1.555"
        );
    }

    /**
     * Prices that are too big for an expense.
     *
     * @return Stream of long price values
     */
    static Stream<String> longInputs() {
        return Stream.of(
                "1000000000000",
                "10000000",
                "1000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
        );
    }

    /**
     * Prices that are valid for an expense
     *
     * @return a stream of valid prices
     */
    static Stream<String> validPriceInputs() {
        return Stream.of(
                "0",
                "0.1",
                "0.11",
                "9999999",
                "9999999.99"
        );
    }

}
