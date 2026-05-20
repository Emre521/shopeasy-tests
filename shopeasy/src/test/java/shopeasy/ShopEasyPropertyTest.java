package shopeasy;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

class ShopEasyPropertyTest {

    // -----------------------------------------------------------------------
    // Custom provider: generates valid Product instances
    // Used by cart commutativity and other cart-based properties
    // -----------------------------------------------------------------------

    @Provide
    Arbitrary<Product> validProducts() {
        return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
            Arbitraries.doubles().between(0.01, 500.0)
        ).as((name, price) -> new Product("P-" + name, name, price, 100));
    }

    // Property 1: Identity
    // If I apply no discount and no tax, I should get back exactly what I put in.
    // Catches bugs where the formula accidentally modifies the price even with zero rates.

    @Property
    void identity_zeroDiscountAndZeroTaxReturnBasePrice(
            @ForAll @DoubleRange(min = 0, max = 10_000) double base) {

        PriceCalculator calc = new PriceCalculator();
        assertThat(calc.calculate(base, 0, 0)).isEqualTo(base);
    }

    // Property 2: Boundedness  
    // The result should never go below 0, and should never exceed double the base price
    // (since max tax is 100%). Catches cases where discount/tax order is messed up.


    @Property
    void boundedness_finalPriceIsWithinExpectedRange(
            @ForAll @DoubleRange(min = 0, max = 10_000) double base,
            @ForAll @DoubleRange(min = 0, max = 100)   double discount,
            @ForAll @DoubleRange(min = 0, max = 100)   double tax) {

        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(base, discount, tax);
        assertThat(result).isGreaterThanOrEqualTo(0.0);
        assertThat(result).isLessThanOrEqualTo(base * 2 + 0.001);
    }

    // Property 3: Monotonicity
    // A bigger discount should always give a cheaper or equal price, never more expensive.
    // Catches if someone accidentally adds the discount instead of subtracting it.


    @Property
    void monotonicity_higherDiscountNeverIncreasesPrice(
            @ForAll @DoubleRange(min = 0, max = 10_000) double base,
            @ForAll @DoubleRange(min = 0, max = 100)    double tax,
            @ForAll @DoubleRange(min = 0, max = 99)     double lowerDiscount,
            @ForAll @DoubleRange(min = 0, max = 1)      double delta) {

        double higherDiscount = Math.min(lowerDiscount + delta, 100.0);
        PriceCalculator calc = new PriceCalculator();
        double priceWithLower  = calc.calculate(base, lowerDiscount, tax);
        double priceWithHigher = calc.calculate(base, higherDiscount, tax);
        assertThat(priceWithHigher).isLessThanOrEqualTo(priceWithLower + 0.0001);
    }

    // Property 4: Cart commutativity
    // It shouldn't matter if I add apple first or banana first — the total should be the same.
    // Catches order-dependent bugs in how the cart accumulates totals.

    @Property
    void cartCommutativity_addOrderDoesNotAffectTotal(
            @ForAll("validProducts") Product productA,
            @ForAll("validProducts") Product productB,
            @ForAll @IntRange(min = 1, max = 50) int qtyA,
            @ForAll @IntRange(min = 1, max = 50) int qtyB) {

        // Ensure distinct product IDs to avoid merging
        Product a = new Product("PA", productA.getName(), productA.getUnitPrice(), 100);
        Product b = new Product("PB", productB.getName(), productB.getUnitPrice(), 100);

        ShoppingCart cart1 = new ShoppingCart();
        cart1.addItem(a, qtyA);
        cart1.addItem(b, qtyB);

        ShoppingCart cart2 = new ShoppingCart();
        cart2.addItem(b, qtyB);
        cart2.addItem(a, qtyA);

        assertThat(cart1.total()).isCloseTo(cart2.total(), within(0.0001));
    }

    // Property 5: Full discount zeroes the price
    // 100% off means free, no matter what the tax rate is.
    // Catches bugs where tax gets applied before the discount wipes the price out.

    @Property
    void fullDiscount_alwaysResultsInZero(
            @ForAll @DoubleRange(min = 0, max = 10_000) double base,
            @ForAll @DoubleRange(min = 0, max = 100)    double tax) {

        PriceCalculator calc = new PriceCalculator();
        assertThat(calc.calculate(base, 100, tax)).isEqualTo(0.0);
    }
}