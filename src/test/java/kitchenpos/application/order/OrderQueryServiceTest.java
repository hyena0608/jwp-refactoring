package kitchenpos.application.order;

import kitchenpos.application.OrderService;
import kitchenpos.config.ApplicationTestConfig;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class OrderQueryServiceTest extends ApplicationTestConfig {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(menuDao, orderDao, orderLineItemDao, orderTableDao);
    }

    @DisplayName("[SUCCESS] 전체 주문 목록을 조회한다.")
    @Test
    void success_findAll() {
        // given
        final MenuGroup savedMenuGroup = menuGroupDao.save(new MenuGroup("테스트용 메뉴 그룹명"));
        final Menu savedMenu = menuDao.save(new Menu(
                "테스트용 메뉴명",
                BigDecimal.ZERO,
                savedMenuGroup.getId(),
                Collections.emptyList()
        ));
        final OrderTable savedOrderTable = orderTableDao.save(new OrderTable(null, 5, false));

        final List<OrderLineItem> orderLineItems = List.of(new OrderLineItem(null, savedMenu.getId(), 10));

        final Order order = new Order(
                savedOrderTable.getId(),
                OrderStatus.COOKING.name(),
                LocalDateTime.now(),
                orderLineItems
        );

        final Order expected = orderService.create(order);

        // when
        final List<Order> actual = orderService.list();

        // then
        assertSoftly(softly -> {
            softly.assertThat(actual).hasSize(1);
            final Order actualOrder = actual.get(0);

            softly.assertThat(actualOrder.getId()).isEqualTo(expected.getId());
            softly.assertThat(actualOrder.getOrderTableId()).isEqualTo(expected.getOrderTableId());
            softly.assertThat(actualOrder.getOrderStatus()).isEqualTo(expected.getOrderStatus());
            softly.assertThat(actualOrder.getOrderedTime()).isEqualTo(expected.getOrderedTime());
            softly.assertThat(actualOrder.getOrderLineItems())
                    .usingRecursiveComparison()
                    .ignoringExpectedNullFields()
                    .isEqualTo(expected.getOrderLineItems());
        });
    }
}
