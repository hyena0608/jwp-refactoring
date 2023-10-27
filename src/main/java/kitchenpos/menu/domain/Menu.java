package kitchenpos.menu.domain;

import kitchenpos.common.vo.Name;
import kitchenpos.common.vo.Price;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.List;

@Entity
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Name name;

    @Embedded
    private Price price;

    @JoinColumn(name = "menu_group_id", foreignKey = @ForeignKey(name = "fk_menu_to_menu_group"))
    @ManyToOne(fetch = FetchType.LAZY)
    private MenuGroup menuGroup;

    @Embedded
    private MenuProducts menuProducts;

    protected Menu() {
    }

    protected Menu(final Name name,
                   final Price price,
                   final MenuGroup menuGroup,
                   final MenuProducts menuProducts
    ) {
        this(null, name, price, menuGroup, menuProducts);
    }

    protected Menu(final Long id,
                   final Name name,
                   final Price price,
                   final MenuGroup menuGroup,
                   final MenuProducts menuProducts
    ) {
        validate(price);
        this.id = id;
        this.name = name;
        this.price = price;
        this.menuGroup = menuGroup;
        this.menuProducts = menuProducts;
    }

    private void validate(final Price price) {
        if (isPriceNullOrNegative(price)) {
            throw new IllegalArgumentException("상품의 가격은 null 이거나 음수일 수 없습니다.");
        }
    }

    private boolean isPriceNullOrNegative(final Price price) {
        return price == null || price.getValue().compareTo(BigDecimal.ZERO) < 0;
    }

    public static Menu withEmptyMenuProducts(final Name name, final Price price, final MenuGroup menuGroup) {
        return new Menu(name, price, menuGroup, MenuProducts.empty());
    }

    public void addMenuProducts(final List<MenuProduct> requestMenuProducts) {
        final MenuProducts newMenuProducts = new MenuProducts(requestMenuProducts);
        final Price requestTotalSum = menuProducts.getTotalPrice().sum(newMenuProducts.getTotalPrice());
        if (price.isGreaterThan(requestTotalSum)) {
            throw new IllegalArgumentException("메뉴의 가격은 모든 메뉴 상품의 가격 합보다 클 수 없습니다.");
        }

        this.menuProducts.addAll(newMenuProducts);
    }

    public Long getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public Price getPrice() {
        return price;
    }

    public MenuGroup getMenuGroup() {
        return menuGroup;
    }

    public MenuProducts getMenuProducts() {
        return menuProducts;
    }
}
