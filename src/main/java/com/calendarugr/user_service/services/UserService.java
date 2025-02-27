package com.calendarugr.user_service.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.calendarugr.user_service.config.PasswordUtil;
import com.calendarugr.user_service.config.RabbitMQConfig;
import com.calendarugr.user_service.entities.Role;
import com.calendarugr.user_service.entities.TemporaryToken;
import com.calendarugr.user_service.entities.User;
import com.calendarugr.user_service.repositories.RoleRepository;
import com.calendarugr.user_service.repositories.TemporaryTokenRepository;
import com.calendarugr.user_service.repositories.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private RoleRepository roleRepository;

    @Autowired
    private TemporaryTokenRepository temporaryTokenRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

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

    //  The method to register a user if you are not an admin
    @Transactional
    public User registerUser(User user) {
        Optional<Role> roleOptional = roleRepository.findByName("ROLE_INACTIVE");
        if (roleOptional.isPresent()) {
            user.setRole(roleOptional.get());
        }
        user.setPassword(PasswordUtil.encryptPassword(user.getPassword()));
        User toReturn = userRepository.save(user);
        // Generate an activation token for the email and save it in the database
        String token = UUID.randomUUID().toString();
        TemporaryToken temporaryToken = new TemporaryToken();
        temporaryToken.setToken(token);
        temporaryToken.setNickname(user.getNickname());
        temporaryToken.setUser(user);

        temporaryTokenRepository.save(temporaryToken);

        // Send email to user through RabbitMQ
        Map<String, String> message = new HashMap<>();
        message.put("email", user.getEmail());
        message.put("token", token);

        try {
            // Convert map to json
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(message);

            // Send message
            Message msg = MessageBuilder.withBody(json.getBytes())
                .setContentType("application/json")
                .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.MAIL_EXCHANGE, RabbitMQConfig.MAIL_ROUTING_KEY, msg);

        } catch (JsonProcessingException e) {
            // Manejar la excepción aquí
            e.printStackTrace();
            throw new RuntimeException("Error processing JSON", e);
        }

        return toReturn;
    }

    @Transactional
    public Optional<User> activateUser(String token) {
        Optional<TemporaryToken> temporaryToken = temporaryTokenRepository.findByToken(token);
        if (temporaryToken.isPresent()) {
            Optional<User> user = userRepository.findByNickname(temporaryToken.get().getNickname());
            if (user.isPresent()) {
                user.get().setRole(checkRoleUGR(user.get()));
                userRepository.save(user.get());
                temporaryTokenRepository.delete(temporaryToken.get());
                return user;
            }else{
                return Optional.empty();
            }
        }else{
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<User> deactivateUser(Long id){
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            user.get().setRole(roleRepository.findByName("ROLE_INACTIVE").get());
            userRepository.save(user.get());
            return user;
        }else{
            return Optional.empty();
        }
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

    // This method is only used by "TEACHER" or "ADMIN" roles ( set in the API Gateway )
    @Transactional
    public Optional<User> changeRole(Long id){
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getRole().getName().equals("ROLE_TEACHER")) {
                user.setRole(roleRepository.findByName("ROLE_ADMIN").get());
            }else if (user.getRole().getName().equals("ROLE_ADMIN")) {
                user.setRole(roleRepository.findByName("ROLE_TEACHER").get());
            }
            return Optional.of(userRepository.save(user));
        }else{
            return Optional.empty();
        }
    }

    // ADMIN Endpoints

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
