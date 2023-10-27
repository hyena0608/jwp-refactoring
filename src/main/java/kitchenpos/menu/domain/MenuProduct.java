package kitchenpos.menu.domain;

import kitchenpos.common.vo.Price;
import kitchenpos.common.vo.Quantity;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class MenuProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_menu_product_to_product"))
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Embedded
    private Quantity quantity;

    protected MenuProduct() {
    }

    protected MenuProduct(final Product product,
                          final Quantity quantity
    ) {
        this(null, product, quantity);
    }

    protected MenuProduct(final Long seq,
                          final Product product,
                          final Quantity quantity
    ) {
        this.seq = seq;
        this.product = product;
        this.quantity = quantity;
    }

    public static MenuProduct withoutMenu(final Product product, final Quantity quantity) {
        return new MenuProduct(null, product, quantity);
    }

    public Price getTotalPrice() {
        return product.getPrice().multiply(quantity);
    }

    public Long getSeq() {
        return seq;
    }

    public Product getProduct() {
        return product;
    }

    public Quantity getQuantity() {
        return quantity;
    }
}
