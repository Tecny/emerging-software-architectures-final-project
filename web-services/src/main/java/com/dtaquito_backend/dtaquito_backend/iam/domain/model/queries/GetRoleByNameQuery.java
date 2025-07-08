package com.dtaquito_backend.dtaquito_backend.iam.domain.model.queries;


import com.dtaquito_backend.dtaquito_backend.users.domain.model.valueObjects.RoleTypes;

public record GetRoleByNameQuery(RoleTypes name) {
}
