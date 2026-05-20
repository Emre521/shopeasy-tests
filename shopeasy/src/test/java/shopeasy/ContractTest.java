package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ContractTest {

    private ShoppingCart cart;
    private PriceCalculator calculator;
    private Product product;

    @BeforeEach
    void setUp() {
        cart       = new ShoppingCart();
        calculator = new PriceCalculator();
        product    = new Product("P001", "Widget", 10.0, 50);
    }

    // -----------------------------------------------------------------------
    // ShoppingCart.addItem — pre-condition: product != null
    // -----------------------------------------------------------------------

    @Test
    void addItem_nullProduct_violatesPreCondition() {
        assertThatThrownBy(() -> cart.addItem(null, 1))
            .isInstanceOf(AssertionError.class);
    }

    // pre-condition: quantity > 0
    @Test
    void addItem_zeroQuantity_violatesPreCondition() {
        assertThatThrownBy(() -> cart.addItem(product, 0))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void addItem_negativeQuantity_violatesPreCondition() {
        assertThatThrownBy(() -> cart.addItem(product, -5))
            .isInstanceOf(AssertionError.class);
    }

    // valid input — contracts hold
    @Test
    void addItem_validInput_contractHolds() {
        assertThatCode(() -> cart.addItem(product, 3)).doesNotThrowAnyException();
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isGreaterThanOrEqualTo(0);
    }

    // -----------------------------------------------------------------------
    // ShoppingCart.applyDiscount — pre-condition: 0 <= rate <= 100
    // -----------------------------------------------------------------------

    @Test
    void applyDiscount_negativeRate_violatesPreCondition() {
        cart.addItem(product, 1);
        assertThatThrownBy(() -> cart.applyDiscount(-1))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void applyDiscount_rateOver100_violatesPreCondition() {
        cart.addItem(product, 1);
        assertThatThrownBy(() -> cart.applyDiscount(101))
            .isInstanceOf(AssertionError.class);
    }

    // post-condition: result <= total when rate > 0
    @Test
    void applyDiscount_validRate_postConditionHolds() {
        cart.addItem(product, 2); // total = 20.0
        double discounted = cart.applyDiscount(10);
        assertThat(discounted).isLessThanOrEqualTo(cart.total());
        assertThat(discounted).isCloseTo(18.0, within(0.001));
    }

    // -----------------------------------------------------------------------
    // ShoppingCart invariant: total() >= 0 after any operation
    // -----------------------------------------------------------------------

    @Test
    void invariant_totalAlwaysNonNegative_afterAddAndRemove() {
        cart.addItem(product, 3);
        assertThat(cart.total()).isGreaterThanOrEqualTo(0);
        cart.removeItem("P001");
        assertThat(cart.total()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void invariant_totalAlwaysNonNegative_afterClear() {
        cart.addItem(product, 5);
        cart.clear();
        assertThat(cart.total()).isGreaterThanOrEqualTo(0);
    }

    // -----------------------------------------------------------------------
    // PriceCalculator.calculate — pre-conditions
    // -----------------------------------------------------------------------

    @Test
    void calculate_negativeBasePrice_violatesPreCondition() {
        assertThatThrownBy(() -> calculator.calculate(-1, 0, 0))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void calculate_negativeDiscountRate_violatesPreCondition() {
        assertThatThrownBy(() -> calculator.calculate(100, -1, 0))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void calculate_discountRateOver100_violatesPreCondition() {
        assertThatThrownBy(() -> calculator.calculate(100, 101, 0))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void calculate_negativeTaxRate_violatesPreCondition() {
        assertThatThrownBy(() -> calculator.calculate(100, 0, -1))
            .isInstanceOf(AssertionError.class);
    }

    @Test
    void calculate_taxRateOver100_violatesPreCondition() {
        assertThatThrownBy(() -> calculator.calculate(100, 0, 101))
            .isInstanceOf(AssertionError.class);
    }

    // post-condition: result >= 0
    @Test
    void calculate_validInputs_postConditionHolds() {
        double result = calculator.calculate(100, 20, 10);
        assertThat(result).isGreaterThanOrEqualTo(0);
        assertThat(result).isCloseTo(88.0, within(0.001));
    }

    // valid boundary values — no exception
    @Test
    void calculate_boundaryValues_contractHolds() {
        assertThatCode(() -> calculator.calculate(0, 0, 0)).doesNotThrowAnyException();
        assertThatCode(() -> calculator.calculate(100, 100, 100)).doesNotThrowAnyException();
    }
}