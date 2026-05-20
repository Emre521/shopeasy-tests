package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ShoppingCartStructuralTest {

    private ShoppingCart cart;
    private Product apple;
    private Product banana;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        apple  = new Product("P001", "Apple",  1.50, 100);
        banana = new Product("P002", "Banana", 0.80, 50);
    }

    // -----------------------------------------------------------------------
    // addItem — yeni ürün ekleme (new product branch)
    // -----------------------------------------------------------------------

    @Test
    void addNewItemIncreasesItemCount() {
        cart.addItem(apple, 3);
        assertThat(cart.itemCount()).isEqualTo(1);
    }

    @Test
    void addNewItemUpdatesTotal() {
        cart.addItem(apple, 2);
        assertThat(cart.total()).isCloseTo(3.0, within(0.001));
    }

    // -----------------------------------------------------------------------
    // addItem — aynı ürün tekrar eklenince miktar birleşmeli (existing product branch)
    // -----------------------------------------------------------------------

    @Test
    void addSameItemTwiceCombinesQuantity() {
        cart.addItem(apple, 2);
        cart.addItem(apple, 3);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isCloseTo(7.5, within(0.001)); // 5 * 1.50
    }

    @Test
    void addDifferentItemsKeepsSeparateLines() {
        cart.addItem(apple, 1);
        cart.addItem(banana, 1);
        assertThat(cart.itemCount()).isEqualTo(2);
    }

    // -----------------------------------------------------------------------
    // removeItem — sepette olan ürünü kaldırma (found branch)
    // -----------------------------------------------------------------------

    @Test
    void removeExistingItemDecreasesCount() {
        cart.addItem(apple, 2);
        cart.removeItem("P001");
        assertThat(cart.itemCount()).isEqualTo(0);
    }

    @Test
    void removeExistingItemUpdatesTotal() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 1);
        cart.removeItem("P001");
        assertThat(cart.total()).isCloseTo(0.80, within(0.001));
    }

    // -----------------------------------------------------------------------
    // removeItem — sepette olmayan ürünü kaldırma (not found branch — no-op)
    // -----------------------------------------------------------------------

    @Test
    void removeNonExistingItemDoesNothing() {
        cart.addItem(apple, 2);
        cart.removeItem("P999");
        assertThat(cart.itemCount()).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // updateQuantity — ürün bulundu, geçerli miktar (happy path)
    // -----------------------------------------------------------------------

    @Test
    void updateQuantityChangesTotal() {
        cart.addItem(apple, 1);
        cart.updateQuantity("P001", 5);
        assertThat(cart.total()).isCloseTo(7.5, within(0.001));
    }

    // -----------------------------------------------------------------------
    // updateQuantity — geçersiz miktar (quantity <= 0 branch)
    // -----------------------------------------------------------------------

    @Test
    void updateQuantityWithZeroThrows() {
        cart.addItem(apple, 1);
        assertThatThrownBy(() -> cart.updateQuantity("P001", 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateQuantityWithNegativeThrows() {
        cart.addItem(apple, 1);
        assertThatThrownBy(() -> cart.updateQuantity("P001", -1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // -----------------------------------------------------------------------
    // updateQuantity — ürün sepette yok (not found branch)
    // -----------------------------------------------------------------------

    @Test
    void updateQuantityForMissingProductThrows() {
        assertThatThrownBy(() -> cart.updateQuantity("P999", 3))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("not found");
    }

    // -----------------------------------------------------------------------
    // applyDiscount — sıfır indirim (zero discount branch)
    // -----------------------------------------------------------------------

    @Test
    void applyZeroDiscountReturnsSameTotal() {
        cart.addItem(apple, 4); // 6.0
        assertThat(cart.applyDiscount(0)).isCloseTo(6.0, within(0.001));
    }

    // -----------------------------------------------------------------------
    // applyDiscount — pozitif indirim (positive discount branch)
    // -----------------------------------------------------------------------

    @Test
    void applyDiscountReducesTotal() {
        cart.addItem(apple, 4); // 6.0
        assertThat(cart.applyDiscount(50)).isCloseTo(3.0, within(0.001));
    }

    @Test
    void applyFullDiscountReturnsZero() {
        cart.addItem(apple, 2);
        assertThat(cart.applyDiscount(100)).isCloseTo(0.0, within(0.001));
    }

    // -----------------------------------------------------------------------
    // total — boş sepet (empty cart branch)
    // -----------------------------------------------------------------------

    @Test
    void emptyCartTotalIsZero() {
        assertThat(cart.total()).isEqualTo(0.0);
    }

    // -----------------------------------------------------------------------
    // clear — sepeti temizleme
    // -----------------------------------------------------------------------

    @Test
    void clearRemovesAllItems() {
        cart.addItem(apple, 1);
        cart.addItem(banana, 2);
        cart.clear();
        assertThat(cart.itemCount()).isEqualTo(0);
        assertThat(cart.total()).isEqualTo(0.0);
    }

    // -----------------------------------------------------------------------
    // getItems — unmodifiable list kontrolü
    // -----------------------------------------------------------------------

    @Test
    void getItemsReturnsUnmodifiableList() {
        cart.addItem(apple, 1);
        assertThatThrownBy(() -> cart.getItems().add(new CartItem(banana, 1)))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    // -----------------------------------------------------------------------
    // toString — smoke test
    // -----------------------------------------------------------------------

    @Test
    void toStringContainsItemCountAndTotal() {
        cart.addItem(apple, 2);
        assertThat(cart.toString()).contains("items=1").contains("total=");
    }
}