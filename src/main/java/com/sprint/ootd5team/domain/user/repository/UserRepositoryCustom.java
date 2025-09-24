package com.sprint.ootd5team.domain.user.repository;

import com.sprint.ootd5team.domain.user.entity.User;
import java.util.List;
import java.util.UUID;

public interface UserRepositoryCustom {

    List<User> findUsersWithCursor(
        String cursor,
        UUID idAfter,
        Integer limit,
        String sortBy,
        String sortDirection,
        String emailLike,
        String roleEqual,
        Boolean locked);


    Long countUsers(String role,String emailLike, Boolean locked);

}
