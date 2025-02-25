package com.calendarugr.user_service.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.calendarugr.user_service.PasswordUtil;
import com.calendarugr.user_service.entities.User;
import com.calendarugr.user_service.repositories.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        // Encrypt password
        user.setPassword(PasswordUtil.encryptPassword(user.getPassword()));
        return userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    public User update(Long id, User user) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User userToUpdate = userOptional.get();
            if (user.getNickname() != null){
                userToUpdate.setNickname(user.getNickname());
            }
            if (user.getEmail() != null){
                userToUpdate.setEmail(user.getEmail());
            }
            if (user.getPassword() != null){
                userToUpdate.setPassword(PasswordUtil.encryptPassword(user.getPassword()));
            }
            
            return userRepository.save(userToUpdate);
        }
        return null;
    }

}
