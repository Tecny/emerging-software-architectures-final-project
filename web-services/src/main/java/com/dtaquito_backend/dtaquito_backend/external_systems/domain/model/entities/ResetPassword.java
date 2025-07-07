package com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPassword {
    private String token;
    private String newPassword;
}