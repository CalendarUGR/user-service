package com.calendarugr.user_service.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.calendarugr.user_service.PasswordUtil;
import com.calendarugr.user_service.entities.Role;
import com.calendarugr.user_service.entities.User;
import com.calendarugr.user_service.repositories.RoleRepository;
import com.calendarugr.user_service.repositories.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private RoleRepository roleRepository;

    public Optional<User> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Register User

    // Activate User

    // Deactivate User

    @Transactional // This is a method only for the admin
    public User save(User user) {
        if (!checkUGREmail(user)){
            throw new IllegalArgumentException("Only UGR emails are allowed");
        }
        if (user.getRole() == null) {
            Optional<Role> roleOptional = roleRepository.findByName("ROLE_INACTIVE");
            if (roleOptional.isPresent()) {
                user.setRole(roleOptional.get());
            }else{
                throw new EntityNotFoundException("Role inactive not found when creating user");
            }
        }else{
            Optional<Role> roleOptional = roleRepository.findByName(user.getRole().getName());
            if (roleOptional.isPresent()) {
                user.setRole(roleOptional.get());
            }else{
                throw new EntityNotFoundException("Role not found when creating user");
            }
        }
        // Encrypt password
        user.setPassword(PasswordUtil.encryptPassword(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional // This is a method only for the admin
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
            if (user.getRole() != null){
                Optional<Role> roleOptional = roleRepository.findByName(user.getRole().getName());
                if (roleOptional.isPresent()) {
                    userToUpdate.setRole(roleOptional.get());
                }else{
                    throw new ConstraintViolationException("Role not found", null);
                }
            }
            
            return userRepository.save(userToUpdate);
        }
        return null;
    }

    @Transactional 
    public User updateNickname(Long id, User user) {
        if (user.getNickname() == null) {
            throw new ConstraintViolationException("Nickname cannot be null", null);
        }
        if (userRepository.findByNickname(user.getNickname()).isPresent()) {
            throw new ConstraintViolationException("Nickname already exists", null);
        }
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User userToUpdate = userOptional.get();
            userToUpdate.setNickname(user.getNickname());
            return userRepository.save(userToUpdate);
        }
        return null;
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    // Auxiliary method to check the role of the user and UGR email
    
    private Boolean checkUGREmail ( User user ) {
        return user.getEmail().trim().endsWith("ugr.es");
    }

    private Role checkRoleUGR(User user) {

        if (user.getEmail().trim().endsWith("@correo.ugr.es")) {
            Optional<Role> roleOptional = roleRepository.findByName("ROLE_STUDENT");
            if (roleOptional.isPresent()) {
                return roleOptional.get();
            }
        }else if (user.getEmail().trim().endsWith("@ugr.es")) {
            Optional<Role> roleOptional = roleRepository.findByName("ROLE_TEACHER");
            if (roleOptional.isPresent()) {
                return roleOptional.get();
            }
        }
        return roleRepository.findByName("ROLE_INACTIVE").get();
    }

}
