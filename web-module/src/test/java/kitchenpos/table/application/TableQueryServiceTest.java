package kitchenpos.table.application;

import kitchenpos.config.ApplicationTestConfig;
import kitchenpos.table.application.request.OrderTableCreateRequest;
import kitchenpos.table.application.response.OrderTableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class TableQueryServiceTest extends ApplicationTestConfig {

    private TableService tableService;

    @BeforeEach
    void setUp() {
        tableService = new TableService(orderTableRepository, orderTableValidator);
    }

    @DisplayName("[SUCCESS] 전체 테이블 목록을 조회한다.")
    @Test
    void success_findAll() {
        // given
        final OrderTableCreateRequest request = new OrderTableCreateRequest(5, false);
        final OrderTableResponse expected = tableService.create(request);

        // when
        final List<OrderTableResponse> actual = tableService.list();

        // then
        assertSoftly(softly -> {
            softly.assertThat(actual).hasSize(1);
            final OrderTableResponse actualOrderTable = actual.get(0);

            softly.assertThat(actualOrderTable.getId()).isEqualTo(expected.getId());
            softly.assertThat(actualOrderTable.getNumberOfGuests()).isEqualTo(expected.getNumberOfGuests());
            softly.assertThat(actualOrderTable.isEmpty()).isEqualTo(expected.isEmpty());
        });
    }
}
