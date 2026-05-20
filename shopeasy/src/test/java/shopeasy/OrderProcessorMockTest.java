package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessorMockTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private OrderProcessor orderProcessor;

    private ShoppingCart cart;
    private Product widget;
    private Product gadget;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        widget = new Product("P001", "Widget", 25.0, 100);
        gadget = new Product("P002", "Gadget", 10.0, 50);
    }

    // -----------------------------------------------------------------------
    // Scenario 1: Happy path — inventory OK, payment succeeds → Order returned
    // -----------------------------------------------------------------------

    @Test
    void process_inventoryOkAndPaymentOk_returnsOrder() {
        cart.addItem(widget, 2); // total = 50.0

        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(paymentGateway.charge("customer-1", 50.0)).thenReturn(true);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("customer-1");
        assertThat(order.getTotal()).isEqualTo(50.0);
        assertThat(order.getItems()).hasSize(1);
        verify(paymentGateway).charge("customer-1", 50.0);
    }

    // -----------------------------------------------------------------------
    // Scenario 2: Inventory failure — isAvailable() returns false
    //             → null returned, charge() never called
    // -----------------------------------------------------------------------

    @Test
    void process_inventoryUnavailable_returnsNullAndNeverCharges() {
        cart.addItem(widget, 2);

        when(inventoryService.isAvailable(widget, 2)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNull();
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    // -----------------------------------------------------------------------
    // Scenario 3: Payment failure — inventory OK but charge() returns false
    //             → null returned
    // -----------------------------------------------------------------------

    @Test
    void process_paymentFails_returnsNull() {
        cart.addItem(widget, 1); // total = 25.0

        when(inventoryService.isAvailable(widget, 1)).thenReturn(true);
        when(paymentGateway.charge("customer-1", 25.0)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNull();
        verify(paymentGateway).charge("customer-1", 25.0);
    }

    // -----------------------------------------------------------------------
    // Scenario 4: Partial quantity — first item OK, second item unavailable
    //             → null returned, charge() never called
    // -----------------------------------------------------------------------

    @Test
    void process_partialInventory_returnsNullAndNeverCharges() {
        cart.addItem(widget, 2);
        cart.addItem(gadget, 3);

        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(inventoryService.isAvailable(gadget, 3)).thenReturn(false);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNull();
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    // -----------------------------------------------------------------------
    // Scenario 5: Multiple items, all available — happy path with 2 products
    // -----------------------------------------------------------------------

    @Test
    void process_multipleItemsAllAvailable_returnsOrder() {
        cart.addItem(widget, 1); // 25.0
        cart.addItem(gadget, 2); // 20.0 → total = 45.0

        when(inventoryService.isAvailable(widget, 1)).thenReturn(true);
        when(inventoryService.isAvailable(gadget, 2)).thenReturn(true);
        when(paymentGateway.charge("customer-2", 45.0)).thenReturn(true);

        Order order = orderProcessor.process("customer-2", cart);

        assertThat(order).isNotNull();
        assertThat(order.getTotal()).isEqualTo(45.0);
        assertThat(order.getItems()).hasSize(2);
    }

    // -----------------------------------------------------------------------
    // Scenario 6: Empty cart → IllegalArgumentException, no mocks touched
    // -----------------------------------------------------------------------

    @Test
    void process_emptyCart_throwsException() {
        assertThatThrownBy(() -> orderProcessor.process("customer-1", cart))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("empty");

        verifyNoInteractions(inventoryService, paymentGateway);
    }

    // -----------------------------------------------------------------------
    // Scenario 7: Null customerId → IllegalArgumentException
    // -----------------------------------------------------------------------

    @Test
    void process_nullCustomerId_throwsException() {
        cart.addItem(widget, 1);

        assertThatThrownBy(() -> orderProcessor.process(null, cart))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(inventoryService, paymentGateway);
    }
}