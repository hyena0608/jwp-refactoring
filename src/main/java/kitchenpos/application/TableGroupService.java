package kitchenpos.application;

import kitchenpos.domain.Order;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.dto.TableGroupCreateRequest;
import kitchenpos.dto.TableGroupResponse;
import kitchenpos.repository.OrderRepository;
import kitchenpos.repository.OrderTableRepository;
import kitchenpos.repository.TableGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
public class TableGroupService {

    private final OrderRepository orderRepository;
    private final OrderTableRepository orderTableRepository;
    private final TableGroupRepository tableGroupRepository;

    public TableGroupService(final OrderRepository orderRepository,
                             final OrderTableRepository orderTableRepository,
                             final TableGroupRepository tableGroupRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderTableRepository = orderTableRepository;
        this.tableGroupRepository = tableGroupRepository;
    }

    @Transactional
    public TableGroupResponse create(final TableGroupCreateRequest request) {
        final List<Long> requestOrderTableIds = request.getOrderTableIds();
        final List<OrderTable> findOrderTables = orderTableRepository.findAllByIdIn(requestOrderTableIds);
        if (requestOrderTableIds.size() != findOrderTables.size()) {
            throw new IllegalArgumentException("단체 지정을 위해 요청하신 주문 테이블 목록이 정확하지 않습니다. 선택한 주문 테이블 목록을 다시 확인해주세요.");
        }

        final TableGroup savedTableGroup = tableGroupRepository.save(TableGroup.withOrderTables(findOrderTables));

        return TableGroupResponse.from(savedTableGroup);
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        orderRepository.findOrdersByTableGroupId(tableGroupId)
                .forEach(Order::ungroupOrderTable);
    }
}
