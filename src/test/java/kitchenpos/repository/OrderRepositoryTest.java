package kitchenpos.repository;

import kitchenpos.config.RepositoryTestConfig;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderLineItems;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.Product;
import kitchenpos.domain.vo.Name;
import kitchenpos.domain.vo.Price;
import kitchenpos.domain.vo.Quantity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryTest extends RepositoryTestConfig {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void existsByOrderTableIdAndOrderStatusIn() {
        // given
        final Product savedProduct = persistProduct(new Product(new Name("테스트용 상품명"), new Price("10000")));
        final MenuGroup savedMenuGroup = persistMenuGroup(new MenuGroup(new Name("테스트용 메뉴 그룹명")));
        final Menu savedMenu = persistMenu(new Menu(new Name("테스트용 메뉴명"), new Price("10000"), savedMenuGroup, Collections.emptyList()));
        persistMenuProduct(new MenuProduct(savedMenu, savedProduct, new Quantity(1)));
        final OrderTable savedOrderTable = persistOrderTable(new OrderTable(null, 10, true));
        final Order savedOrder = persistOrder(new Order(savedOrderTable, OrderStatus.COOKING, LocalDateTime.now(), new OrderLineItems(new ArrayList<>())));
        persistOrderLineItem(new OrderLineItem(savedOrder, savedMenu, new Quantity(1)));
        persistOrderLineItem(new OrderLineItem(savedOrder, savedMenu, new Quantity(1)));

        em.flush();
        em.close();

        // when
        final boolean actual = orderRepository.existsByOrderTableIdAndOrderStatusIn(savedOrderTable.getId(), List.of(OrderStatus.COOKING));

        // then
        assertThat(actual).isTrue();
    }
}
