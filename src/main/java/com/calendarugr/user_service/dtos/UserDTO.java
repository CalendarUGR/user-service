package com.calendarugr.user_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class UserDTO{

    private String nickname;

    private String email;

    private RoleDTO role;

    private Boolean notification = false;
}
