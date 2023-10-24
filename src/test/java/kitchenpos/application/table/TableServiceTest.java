package kitchenpos.application.table;

import kitchenpos.application.TableService;
import kitchenpos.config.ApplicationTestConfig;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.domain.vo.Name;
import kitchenpos.domain.vo.Price;
import kitchenpos.domain.vo.Quantity;
import kitchenpos.dto.OrderTableCreateRequest;
import kitchenpos.dto.OrderTableEmptyUpdateRequest;
import kitchenpos.dto.OrderTableResponse;
import kitchenpos.dto.TableNumberOfGuestsUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class TableServiceTest extends ApplicationTestConfig {

    private TableService tableService;

    @BeforeEach
    void setUp() {
        tableService = new TableService(orderRepository, orderTableRepository);
    }

    @DisplayName("[SUCCESS] 주문 테이블을 등록한다.")
    @Test
    void success_create() {
        // given
        final OrderTableCreateRequest request = new OrderTableCreateRequest(5, false);

        // when
        final OrderTableResponse actual = tableService.create(request);

        // then
        assertSoftly(softly -> {
            softly.assertThat(actual.getId()).isPositive();
            softly.assertThat(actual.getNumberOfGuests()).isEqualTo(request.getNumberOfGuests());
            softly.assertThat(actual.isEmpty()).isEqualTo(request.isEmpty());
        });
    }

    @TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("빈 상태 변경")
    @Nested
    class ChangeEmptyNestedTest {

        @DisplayName("[SUCCESS] 주문 상태를 고려하며 주문 테이블의 빈 상태를 변경한다.")
        @Test
        void success_changeEmpty_when_order_isExists() {
            // given
            final OrderTable savedOrderTable = orderTableRepository.save(OrderTable.withoutTableGroup(10, false));
            final Order savedOrderStatus = orderRepository.save(Order.ofEmptyOrderLineItems(savedOrderTable));
            savedOrderStatus.changeOrderStatus(OrderStatus.COMPLETION);

            // when
            final OrderTableResponse actual = tableService.changeEmpty(savedOrderTable.getId(), new OrderTableEmptyUpdateRequest(true));

            // then
            final OrderTableResponse expected = OrderTableResponse.from(savedOrderTable);

            assertSoftly(softly -> {
                softly.assertThat(actual.getId()).isEqualTo(expected.getId());
                softly.assertThat(actual.getTableGroupId()).isEqualTo(expected.getTableGroupId());
                softly.assertThat(actual.getNumberOfGuests()).isEqualTo(expected.getNumberOfGuests());
                softly.assertThat(actual.isEmpty()).isTrue();
            });
        }

        @DisplayName("[SUCCESS] 주문이 없는 주문 테이블의 빈 상태를 변경한다.")
        @Test
        void success_changeEmpty_when_order_isNotExists() {
            // given
            final OrderTable savedOrderTable = orderTableRepository.save(OrderTable.withoutTableGroup(10, false));

            // when
            final OrderTableResponse actual = tableService.changeEmpty(savedOrderTable.getId(), new OrderTableEmptyUpdateRequest(true));

            // then
            final OrderTableResponse expected = OrderTableResponse.from(savedOrderTable);

            assertSoftly(softly -> {
                softly.assertThat(actual.getId()).isEqualTo(expected.getId());
                softly.assertThat(actual.getTableGroupId()).isEqualTo(expected.getTableGroupId());
                softly.assertThat(actual.getNumberOfGuests()).isEqualTo(expected.getNumberOfGuests());
                softly.assertThat(actual.isEmpty()).isTrue();
            });
        }

        @DisplayName("[EXCEPTION] 주문 테이블이 단체 지정되어 있는 경우 예외가 발생한다.")
        @Test
        void throwException_when_changeEmpty_orderTable_isIn_tableGroup() {
            // given
            final OrderTable savedOrderTableWithFiveGuests = orderTableRepository.save(OrderTable.withoutTableGroup(5, true));
            final List<OrderTable> savedOrderTables = List.of(
                    savedOrderTableWithFiveGuests,
                    orderTableRepository.save(OrderTable.withoutTableGroup(10, false))
            );
            tableGroupRepository.save(TableGroup.withOrderTables(savedOrderTables));

            savedOrderTables.forEach(orderTable -> orderRepository.save(Order.ofEmptyOrderLineItems(orderTable)));

            // expect
            assertThatThrownBy(() -> tableService.changeEmpty(savedOrderTableWithFiveGuests.getId(), new OrderTableEmptyUpdateRequest(true)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("[EXCEPTION] 음식이 준비 중이거나 식사 중일 경우 예외가 발생한다.")
        @ParameterizedTest
        @MethodSource("getOrderStatusWithoutCompletion")
        void throwException_when_changeEmpty_orderStatus_isCookieOrMeal(final OrderStatus orderStatus) {
            // given
            final MenuGroup savedMenuGroup = menuGroupRepository.save(new MenuGroup(new Name("테스트용 메뉴 그룹명")));
            final Menu savedMenu = menuRepository.save(
                    Menu.withEmptyMenuProducts(
                            new Name("테스트용 메뉴명"),
                            Price.from("0"),
                            savedMenuGroup
                    )
            );
            final List<OrderLineItem> orderLineItems = List.of(OrderLineItem.withoutOrder(savedMenu, new Quantity(10)));
            final OrderTable savedOrderTable = orderTableRepository.save(OrderTable.withoutTableGroup(5, false));
            final Order savedOrder = orderRepository.save(Order.ofEmptyOrderLineItems(savedOrderTable));
            savedOrder.addOrderLineItems(orderLineItems);
            savedOrder.changeOrderStatus(orderStatus);

            // expect
            assertThatThrownBy(() -> tableService.changeEmpty(savedOrderTable.getId(), new OrderTableEmptyUpdateRequest(true)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        private Stream<Arguments> getOrderStatusWithoutCompletion() {
            return Arrays.stream(OrderStatus.values())
                    .filter(orderStatus -> orderStatus != OrderStatus.COMPLETION)
                    .map(Arguments::arguments);
        }
    }

    @DisplayName("손님 수 수정")
    @Nested
    class ChangeNumberOfGuestsNestedTest {

        @DisplayName("[EXCEPTION] 손님 수를 0 미만으로 수정할 경우 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(ints = {-1, -2, -10, -100})
        void throwException_when_changeNumberOfGuests_orderTable_numberOfGuests_isLessThanZero(final int negativeValue) {
            // given
            final OrderTable savedOrderTable = orderTableRepository.save(OrderTable.withoutTableGroup(10, true));
            final TableNumberOfGuestsUpdateRequest request = new TableNumberOfGuestsUpdateRequest(negativeValue);

            // expect
            assertThatThrownBy(() -> tableService.changeNumberOfGuests(savedOrderTable.getId(), request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("[EXCEPTION] 주문 테이블이 비어있는 상태일 경우 예외가 발생한다.")
        @Test
        void throwException_when_changeNumberOfGuests_orderTableIsEmpty() {
            // given
            final OrderTable savedOrderTable = orderTableRepository.save(OrderTable.withoutTableGroup(10, true));
            final TableNumberOfGuestsUpdateRequest request = new TableNumberOfGuestsUpdateRequest(10);

            // expect
            assertThatThrownBy(() -> tableService.changeNumberOfGuests(savedOrderTable.getId(), request))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
