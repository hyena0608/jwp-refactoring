package kitchenpos.dto;

import kitchenpos.domain.Product;

import java.util.List;
import java.util.stream.Collectors;

public class ProductResponse {

    private long id;
    private String name;
    private String price;

    public ProductResponse(final long id, final String name, final String price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public static ProductResponse from(final Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName().getValue(),
                product.getPrice().getValue().toPlainString()
        );
    }

    public static List<ProductResponse> from(final List<Product> products) {
        return products.stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }
}
