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

    // -----------------------------------------------------------------------
    // PARTITION: basePrice = 0 → sonuç her zaman 0 olmalı, oranlar ne olursa olsun
    // -----------------------------------------------------------------------

    @Test
    void zeroPriceAlwaysReturnsZero() {
        assertThat(calculator.calculate(0, 20, 10)).isEqualTo(0.0);
    }

    @Test
    void zeroPriceWithMaxRatesStillReturnsZero() {
        assertThat(calculator.calculate(0, 100, 100)).isEqualTo(0.0);
    }

    // -----------------------------------------------------------------------
    // BOUNDARY: discountRate = 0 (alt sınır) → indirim uygulanmamalı
    // -----------------------------------------------------------------------

    @Test
    void discountRateZeroMeansNoDiscount() {
        double result = calculator.calculate(100, 0, 0);
        assertThat(result).isEqualTo(100.0);
    }

    // -----------------------------------------------------------------------
    // BOUNDARY: discountRate = 100 (üst sınır) → fiyat tamamen sıfırlanmalı
    // -----------------------------------------------------------------------

    @Test
    void discountRateHundredWipesPrice() {
        double result = calculator.calculate(100, 100, 0);
        assertThat(result).isEqualTo(0.0);
    }

    // -----------------------------------------------------------------------
    // BOUNDARY: taxRate = 0 (alt sınır) → vergi eklenmemeli
    // -----------------------------------------------------------------------

    @Test
    void taxRateZeroMeansNoTax() {
        double result = calculator.calculate(200, 0, 0);
        assertThat(result).isEqualTo(200.0);
    }

    // -----------------------------------------------------------------------
    // BOUNDARY: taxRate = 100 (üst sınır) → fiyat iki katına çıkmalı
    // -----------------------------------------------------------------------

    @Test
    void taxRateHundredDoublesPriceAfterDiscount() {
        // base=100, disc=0 → discounted=100 → tax=100% → 200
        double result = calculator.calculate(100, 0, 100);
        assertThat(result).isEqualTo(200.0);
    }

    // -----------------------------------------------------------------------
    // BOUNDARY: discountRate = 1 (off-point, alt sınırın hemen üstü)
    // -----------------------------------------------------------------------

    @Test
    void discountRateOnePercent() {
        // 100 * (1 - 0.01) * (1 + 0) = 99.0
        double result = calculator.calculate(100, 1, 0);
        assertThat(result).isCloseTo(99.0, within(0.001));
    }

    // -----------------------------------------------------------------------
    // BOUNDARY: discountRate = 99 (off-point, üst sınırın hemen altı)
    // -----------------------------------------------------------------------

    @Test
    void discountRateNinetyNinePercent() {
        // 100 * (1 - 0.99) = 1.0
        double result = calculator.calculate(100, 99, 0);
        assertThat(result).isCloseTo(1.0, within(0.001));
    }

    // -----------------------------------------------------------------------
    // PARTITION: tipik değerler — formula doğruluğunu kontrol eder
    // base * (1 - disc/100) * (1 + tax/100)
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "base={0}, disc={1}%, tax={2}% => {3}")
    @CsvSource({
        "100.0,  10.0,  20.0,  108.0",   // 100 * 0.90 * 1.20
        "200.0,   0.0,  10.0,  220.0",   // 200 * 1.00 * 1.10
        "150.0,  50.0,   0.0,   75.0",   // 150 * 0.50 * 1.00
        "250.0,  20.0,  10.0,  220.0",   // 250 * 0.80 * 1.10
        "100.0, 100.0, 100.0,    0.0",   // tam indirim → vergi ne olursa 0
    })
    void typicalValues(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax))
            .isCloseTo(expected, within(0.001));
    }

    // -----------------------------------------------------------------------
    // PARTITION: çok büyük basePrice — taşma / hassasiyet kontrolü
    // -----------------------------------------------------------------------

    @Test
    void veryLargeBasePrice() {
        double result = calculator.calculate(1_000_000, 0, 0);
        assertThat(result).isEqualTo(1_000_000.0);
    }

    // -----------------------------------------------------------------------
    // convenience metod: applyDiscountOnly ve applyTaxOnly smoke testleri
    // -----------------------------------------------------------------------

    @Test
    void applyDiscountOnlyMatchesCalculate() {
        assertThat(calculator.applyDiscountOnly(100, 25))
            .isCloseTo(calculator.calculate(100, 25, 0), within(0.001));
    }

    @Test
    void applyTaxOnlyMatchesCalculate() {
        assertThat(calculator.applyTaxOnly(100, 15))
            .isCloseTo(calculator.calculate(100, 0, 15), within(0.001));
    }
}