package com.dtaquito_backend.dtaquito_backend.iam.domain.services;

import com.dtaquito_backend.dtaquito_backend.iam.domain.model.queries.GetAllRolesQuery;
import com.dtaquito_backend.dtaquito_backend.iam.domain.model.queries.GetRoleByNameQuery;
import com.dtaquito_backend.dtaquito_backend.users.domain.model.entities.Role;

import java.util.List;
import java.util.Optional;

public interface RoleQueryService {

    List<Role> handle(GetAllRolesQuery query);
    Optional<Role> handle(GetRoleByNameQuery query);
}
