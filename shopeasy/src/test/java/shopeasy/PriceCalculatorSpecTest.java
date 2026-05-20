package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class PriceCalculatorSpecTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // zero base price — no matter the rates, result must be 0
    @Test
    void zeroPriceAlwaysReturnsZero() {
        assertThat(calculator.calculate(0, 20, 10)).isEqualTo(0.0);
    }

    // same check with maximum rates to be sure
    @Test
    void zeroPriceWithMaxRatesStillReturnsZero() {
        assertThat(calculator.calculate(0, 100, 100)).isEqualTo(0.0);
    }

    // lower bound of discountRate — nothing should be deducted
    @Test
    void discountRateZeroMeansNoDiscount() {
        double result = calculator.calculate(100, 0, 0);
        assertThat(result).isEqualTo(100.0);
    }

    // upper bound of discountRate — full discount wipes the price
    @Test
    void discountRateHundredWipesPrice() {
        double result = calculator.calculate(100, 100, 0);
        assertThat(result).isEqualTo(0.0);
    }

    // lower bound of taxRate — price stays unchanged
    @Test
    void taxRateZeroMeansNoTax() {
        double result = calculator.calculate(200, 0, 0);
        assertThat(result).isEqualTo(200.0);
    }

    // upper bound of taxRate — price doubles since tax equals base
    @Test
    void taxRateHundredDoublesPriceAfterDiscount() {
        double result = calculator.calculate(100, 0, 100);
        assertThat(result).isEqualTo(200.0);
    }

    // off-point just above zero discount — tiny reduction applied
    @Test
    void discountRateOnePercent() {
        double result = calculator.calculate(100, 1, 0);
        assertThat(result).isCloseTo(99.0, within(0.001));
    }

    // off-point just below 100% discount — almost everything deducted
    @Test
    void discountRateNinetyNinePercent() {
        double result = calculator.calculate(100, 99, 0);
        assertThat(result).isCloseTo(1.0, within(0.001));
    }

    // typical inputs — checks the formula works correctly end to end
    @ParameterizedTest(name = "base={0}, disc={1}%, tax={2}% => {3}")
    @CsvSource({
        "100.0,  10.0,  20.0,  108.0",
        "200.0,   0.0,  10.0,  220.0",
        "150.0,  50.0,   0.0,   75.0",
        "250.0,  20.0,  10.0,  220.0",
        "100.0, 100.0, 100.0,    0.0",
    })
    void typicalValues(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax))
            .isCloseTo(expected, within(0.001));
    }

    // very large base price — checks for floating point precision
    @Test
    void veryLargeBasePrice() {
        double result = calculator.calculate(1_000_000, 0, 0);
        assertThat(result).isEqualTo(1_000_000.0);
    }

    // applyDiscountOnly is just a wrapper, should give same result
    @Test
    void applyDiscountOnlyMatchesCalculate() {
        assertThat(calculator.applyDiscountOnly(100, 25))
            .isCloseTo(calculator.calculate(100, 25, 0), within(0.001));
    }

    // applyTaxOnly is just a wrapper, should give same result
    @Test
    void applyTaxOnlyMatchesCalculate() {
        assertThat(calculator.applyTaxOnly(100, 15))
            .isCloseTo(calculator.calculate(100, 0, 15), within(0.001));
    }
}