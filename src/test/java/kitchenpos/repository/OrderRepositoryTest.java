package kitchenpos.repository;

import kitchenpos.config.RepositoryTestConfig;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.Product;
import kitchenpos.domain.TableGroup;
import kitchenpos.domain.vo.Name;
import kitchenpos.domain.vo.Price;
import kitchenpos.domain.vo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class OrderRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private OrderRepository orderRepository;

    @DisplayName("[SUCCESS] 주문 식별자값으로 주문 조회를 실패할 경우 예외가 발생한다.")
    @Test
    void success_findOrderById() {
        // given
        final OrderTable savedOrderTable = persistOrderTable(OrderTable.withoutTableGroup(5, true));
        final Order expected = persistOrder(Order.ofEmptyOrderLineItems(savedOrderTable));

        em.flush();
        em.close();

        // when
        final Order actual = orderRepository.findOrderByOrderTableId(savedOrderTable.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(actual.getId()).isEqualTo(expected.getId());
            softly.assertThat(actual.getOrderTable()).isEqualTo(expected.getOrderTable());
            softly.assertThat(actual.getOrderStatus()).isEqualTo(expected.getOrderStatus());
            softly.assertThat(actual.getOrderedTime()).isEqualTo(expected.getOrderedTime());
            softly.assertThat(actual.getOrderLineItems()).isEqualTo(expected.getOrderLineItems());
        });
    }

    @DisplayName("[EXCEPTION] 주문 식별자값으로 주문 조회를 실패할 경우 예외가 발생한다.")
    @Test
    void throwException_findOrderById_when_notFount() {
        assertThatThrownBy(() -> orderRepository.findOrderById(0L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @DisplayName("[SUCCESS] 주문 테이블 식별자값으로 주문을 조회한다.")
    @Test
    void success_findOrderByOrderTableId() {
        // given
        final OrderTable savedOrderTable = persistOrderTable(OrderTable.withoutTableGroup(5, true));
        final Order expected = persistOrder(Order.ofEmptyOrderLineItems(savedOrderTable));

        em.flush();
        em.close();

        // when
        final Order actual = orderRepository.findOrderByOrderTableId(savedOrderTable.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(actual.getId()).isEqualTo(expected.getId());
            softly.assertThat(actual.getOrderTable()).isEqualTo(expected.getOrderTable());
            softly.assertThat(actual.getOrderStatus()).isEqualTo(expected.getOrderStatus());
            softly.assertThat(actual.getOrderedTime()).isEqualTo(expected.getOrderedTime());
            softly.assertThat(actual.getOrderLineItems()).isEqualTo(expected.getOrderLineItems());
        });
    }

    @DisplayName("[EXCEPTION] 주문 테이블 식별자값으로 주문 조회를 실패할 경우 예외가 발생한다.")
    @Test
    void throwException_findOrderByOrderTableId_when_notFount() {
        assertThatThrownBy(() -> orderRepository.findOrderByOrderTableId(0L))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @DisplayName("[SUCCESS] 단체 지정 식별자값으로 주문 목록을 조회한다.")
    @Test
    void success_findOrdersByTableGroupId() {
        // given
        final Menu savedMenu = createMenu();
        final OrderTable orderTableOne = OrderTable.withoutTableGroup(5, true);
        final OrderTable orderTableTwo = OrderTable.withoutTableGroup(5, true);
        final TableGroup savedTableGroup = TableGroup.withOrderTables(List.of(
                orderTableOne,
                orderTableTwo
        ));
        persistTableGroup(savedTableGroup);

        final Order order = Order.ofEmptyOrderLineItems(orderTableTwo);
        order.addOrderLineItems(List.of(
                OrderLineItem.withoutOrder(savedMenu, new Quantity(1))
        ));
        persistOrder(order);

        em.flush();
        em.close();

        // when
        final List<Order> actual = orderRepository.findOrdersByTableGroupId(savedTableGroup.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(actual).hasSize(1);
            final Order actualOrder = actual.get(0);

            softly.assertThat(actualOrder).isEqualTo(order);
        });
    }

    private Menu createMenu() {
        final Product savedProduct = persistProduct(new Product(new Name("테스트용 상품명"), Price.from("10000")));
        final MenuGroup savedMenuGroup = persistMenuGroup(new MenuGroup(new Name("테스트용 메뉴 그룹명")));
        final Menu savedMenu = persistMenu(Menu.withEmptyMenuProducts(new Name("테스트용 메뉴명"), Price.ZERO, savedMenuGroup));
        savedMenu.addMenuProducts(List.of(
                MenuProduct.withoutMenu(savedProduct, new Quantity(1))
        ));

        return savedMenu;
    }

    @DisplayName("[SUCCESS] 주문 테이블 식별자값과 주문 상태 목록 조건에 해당하는 주문이 존재하는지 확인한다.")
    @Test
    void success_existsByOrderTableIdAndOrderStatusIn() {
        // given
        final Menu savedMenu = createMenu();
        final OrderTable savedOrderTable = persistOrderTable(OrderTable.withoutTableGroup(10, false));
        final Order savedOrder = persistOrder(Order.ofEmptyOrderLineItems(savedOrderTable));
        savedOrder.addOrderLineItems(List.of(
                OrderLineItem.withoutOrder(savedMenu, new Quantity(1))
        ));

        em.flush();
        em.close();

        // when
        final boolean actual = orderRepository.existsByOrderTableIdAndOrderStatusIn(savedOrderTable.getId(), List.of(OrderStatus.COOKING));

        // then
        assertThat(actual).isTrue();
    }
}
