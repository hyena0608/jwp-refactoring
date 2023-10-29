package kitchenpos.table.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class TableGroupTest {

    @DisplayName("단체 지정 생성")
    @Nested
    class CreateNestedClass {

        @DisplayName("[SUCCESS] 생성한다.")
        @Test
        void success_create() {
            assertThatCode(() -> TableGroup.withOrderTables(List.of(
                    OrderTable.withoutTableGroup(10, true),
                    OrderTable.withoutTableGroup(10, true)
            ))).doesNotThrowAnyException();
        }

        @DisplayName("[EXCEPTION] 주문 테이블 개수가 2 미만일 경우 예외가 발생한다.")
        @Test
        void throwException_create_when_orderTables_sizeIsLessThan2() {
            assertThatThrownBy(() -> TableGroup.withOrderTables(List.of(
                    OrderTable.withoutTableGroup(10, true)
            ))).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("[EXCEPTION] 주문 테이블이 이미 단체 지정되어 있는 경우 예외가 발생한다.")
        @Test
        void throwException_create_when_orderTable_isAlreadyAssigned() {
            assertThatThrownBy(() -> TableGroup.withOrderTables(List.of(
                    new OrderTable(1L, 10, false),
                    OrderTable.withoutTableGroup(10, true)
            ))).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("[EXCEPTION] 주문 테이블이 비어있지 않은 경우 예외가 발생한다.")
        @Test
        void throwException_create_when_orderTable_isNotEmpty() {
            assertThatThrownBy(() -> TableGroup.withOrderTables(List.of(
                    OrderTable.withoutTableGroup(10, false),
                    OrderTable.withoutTableGroup(10, false)
            ))).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @DisplayName("[SUCCESS] 주문 테이블을 단체 지정에 주입할 때 주문 테이블의 비어있는 상태를 false 로 수정한다.")
    @Test
    void success_addOrderTablesAndChangeEmptyFull() {
        // given
        final OrderTable orderTableOne = OrderTable.withoutTableGroup(10, true);
        final OrderTable orderTableTwo = OrderTable.withoutTableGroup(10, true);
        final TableGroup tableGroup = new TableGroup(OrderTables.empty());

        // when
        tableGroup.addOrderTables(List.of(orderTableOne, orderTableTwo));

        // then
        assertSoftly(softly -> {
            final List<OrderTable> actualOrderTables = tableGroup.getOrderTables().getOrderTableItems();
            softly.assertThat(actualOrderTables).hasSize(2);

            final OrderTable actualOrderTableOne = actualOrderTables.get(0);
            final OrderTable actualOrderTableTwo = actualOrderTables.get(1);

            softly.assertThat(actualOrderTableOne.isEmpty()).isFalse();
            softly.assertThat(actualOrderTableTwo.isEmpty()).isFalse();
        });
    }
}
