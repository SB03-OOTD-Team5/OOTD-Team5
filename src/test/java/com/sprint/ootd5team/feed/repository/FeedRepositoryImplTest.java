package com.sprint.ootd5team.feed.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.ootd5team.base.config.JpaConfig;
import com.sprint.ootd5team.base.config.QuerydslConfig;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttribute;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeDef;
import com.sprint.ootd5team.domain.clothattribute.entity.ClothesAttributeValue;
import com.sprint.ootd5team.domain.clothes.entity.Clothes;
import com.sprint.ootd5team.domain.clothes.enums.ClothesType;
import com.sprint.ootd5team.domain.feed.dto.data.OotdDto;
import com.sprint.ootd5team.domain.feed.entity.FeedClothes;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.FeedClothesRepository;
import com.sprint.ootd5team.domain.feed.repository.feedClothes.impl.FeedClothesRepositoryImpl;
import com.sprint.ootd5team.domain.user.entity.Role;
import com.sprint.ootd5team.domain.user.entity.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({FeedClothesRepositoryImpl.class, QuerydslConfig.class, JpaConfig.class})
@ActiveProfiles("test")
@DisplayName("FeedRepositoryImpl 테스트")
public class FeedRepositoryImplTest {

    @Autowired
    private FeedClothesRepository feedClothesRepository;

    @Autowired
    private TestEntityManager em;

    private UUID feedId;

    @BeforeEach
    void setUp() {
        feedId = UUID.randomUUID();

        User owner = new User(
            "테스트유저",
            "test@example.com",
            "password",
            Role.USER
        );
        em.persist(owner);

        Clothes clothes = Clothes.builder()
            .name("아디다스 트레이닝 팬츠")
            .type(ClothesType.BOTTOM)
            .imageUrl("https://image.url/adidasPants.jpg")
            .owner(owner)
            .build();
        em.persist(clothes);

        FeedClothes feedClothes = new FeedClothes(feedId, clothes.getId());
        em.persist(feedClothes);

        ClothesAttribute attr = new ClothesAttribute("색상");
        em.persist(attr);

        ClothesAttributeDef attrDef = new ClothesAttributeDef(attr, "초록");
        em.persist(attrDef);

        ClothesAttributeValue cav = new ClothesAttributeValue(clothes, attr, "초록");
        em.persist(cav);

        em.flush();
        em.clear();
    }

    @Test
    void findOotdsByFeedIds_success() {
        // when
        Map<UUID, List<OotdDto>> result = feedClothesRepository.findOotdsByFeedIds(List.of(feedId));

        // then
        assertThat(result).containsKey(feedId);
        List<OotdDto> ootds = result.get(feedId);
        assertThat(ootds).hasSize(1);

        OotdDto ootd = ootds.get(0);
        assertThat(ootd.name()).isEqualTo("아디다스 트레이닝 팬츠");
        assertThat(ootd.type()).isEqualTo("BOTTOM");
        assertThat(ootd.attributes()).hasSize(1);
        assertThat(ootd.attributes().get(0).value()).isEqualTo("초록");
    }
}