package com.calendarugr.user_service.mappers;

import com.calendarugr.user_service.dtos.RoleDTO;
import com.calendarugr.user_service.entities.Role;

public class RoleMapper {

    public static RoleDTO toDTO(Role role) {
        if (role == null) {
            return null;
        }

        return new RoleDTO(
            role.getName()
        );
    }
}