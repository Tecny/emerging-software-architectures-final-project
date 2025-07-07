package com.dtaquito_backend.dtaquito_backend.users.domain.services;


import com.dtaquito_backend.dtaquito_backend.users.domain.model.aggregates.User;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.queries.GetUserByIdQuery;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {

    Optional<User> handle(GetUserByIdQuery query);

    List<User> getAllUsers();

    String getUserRoleByUserId(Long userId);
}
