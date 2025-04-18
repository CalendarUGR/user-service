package com.calendarugr.user_service.mappers;

import com.calendarugr.user_service.dtos.UserDTO;
import com.calendarugr.user_service.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserDTO(
            user.getNickname(),
            user.getEmail(),
            RoleMapper.toDTO(user.getRole()), // Asumiendo que tienes un RoleMapper
            user.getNotification()
        );
    }

    public static List<UserDTO> toDTOList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
            .map(UserMapper::toDTO)
            .collect(Collectors.toList());
    }
}