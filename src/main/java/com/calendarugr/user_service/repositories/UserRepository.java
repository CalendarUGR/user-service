package com.calendarugr.user_service.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.calendarugr.user_service.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id IN :ids AND u.notification = true")
    List<User> findAllByIdAndNotificationTrue(@Param("ids") List<Long> ids);
}
