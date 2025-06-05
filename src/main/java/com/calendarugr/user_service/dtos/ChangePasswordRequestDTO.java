package com.calendarugr.user_service.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePasswordRequestDTO {

    private String currentPassword; // Used also as confirmPassword in the reset password flow

    private String newPassword;

    // token can exist or not
    private String token; // Optional, used for reset password flow
}
