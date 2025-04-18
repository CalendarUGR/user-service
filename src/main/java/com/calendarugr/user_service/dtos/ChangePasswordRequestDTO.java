package com.calendarugr.user_service.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePasswordRequestDTO {

    private String currentPassword;

    private String newPassword;
}
