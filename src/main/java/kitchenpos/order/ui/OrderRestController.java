package kitchenpos.order.ui;

import kitchenpos.order.application.OrderService;
import kitchenpos.dto.OrderCreateRequest;
import kitchenpos.dto.OrderResponse;
import kitchenpos.order.application.OrderSheet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OrderRestController {

    private final OrderService orderService;

    public OrderRestController(final OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/orders")
    public ResponseEntity<OrderResponse> create(@RequestBody final OrderCreateRequest request) {
        final OrderSheet requestOrderSheet = new OrderSheet(
                request.getOrderTableId(),
                request.getOrderLineItems()
                        .stream()
                        .map(orderLineItem -> new OrderSheet.OrderSheetItem(
                                orderLineItem.getMenuId(),
                                orderLineItem.getQuantity()
                        )).collect(Collectors.toList())
        );

        final OrderResponse created = orderService.create(requestOrderSheet);
        final URI uri = URI.create("/api/orders/" + created.getId());
        return ResponseEntity.created(uri)
                .body(created);
    }

    @GetMapping("/api/orders")
    public ResponseEntity<List<OrderResponse>> list() {
        return ResponseEntity.ok()
                .body(orderService.list());
    }

    @PutMapping("/api/orders/{orderId}/order-status")
    public ResponseEntity<OrderResponse> changeOrderStatus(
            @PathVariable final Long orderId,
            @RequestBody final String orderStatus
    ) {
        return ResponseEntity.ok(orderService.changeOrderStatus(orderId, orderStatus));
    }
}
