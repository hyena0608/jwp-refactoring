package kitchenpos.application.menugroup;

import kitchenpos.application.MenuGroupService;
import kitchenpos.config.ApplicationTestConfig;
import kitchenpos.domain.MenuGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class MenuGroupServiceTest extends ApplicationTestConfig {

    private MenuGroupService menuGroupService;

    @BeforeEach
    void setUp() {
        menuGroupService = new MenuGroupService(menuGroupDao);
    }

    @DisplayName("[SUCCESS] 메뉴 그룹을 생성한다.")
    @Test
    void success_create() {
        // given
        final MenuGroup expected = new MenuGroup("테스트 메뉴 그룹");

        // when
        final MenuGroup actual = menuGroupService.create(expected);

        // then
        assertSoftly(softly -> {
            softly.assertThat(actual.getId()).isPositive();
            softly.assertThat(actual.getName()).isEqualTo(expected.getName());
        });
    }
}
