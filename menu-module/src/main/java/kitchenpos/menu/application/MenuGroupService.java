package kitchenpos.menu.application;

import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.common.vo.Name;
import kitchenpos.menu.application.request.MenuGroupCreateRequest;
import kitchenpos.menu.application.response.MenuGroupResponse;
import kitchenpos.menu.domain.MenuGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
public class MenuGroupService {

    private final MenuGroupRepository menuGroupRepository;

    public MenuGroupService(final MenuGroupRepository menuGroupRepository) {
        this.menuGroupRepository = menuGroupRepository;
    }

    @Transactional
    public MenuGroupResponse create(final MenuGroupCreateRequest request) {
        final Name menuGroupName = new Name(request.getName());
        final MenuGroup savedMenuGroup = menuGroupRepository.save(new MenuGroup(menuGroupName));

        return MenuGroupResponse.from(savedMenuGroup);
    }

    public List<MenuGroupResponse> list() {
        return MenuGroupResponse.from(menuGroupRepository.findAll());
    }
}
