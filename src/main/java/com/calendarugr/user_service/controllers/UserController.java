package com.calendarugr.user_service.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.calendarugr.user_service.config.PasswordUtil;
import com.calendarugr.user_service.dtos.ChangePasswordRequestDTO;
import com.calendarugr.user_service.dtos.EmailDTO;
import com.calendarugr.user_service.dtos.UserDTO;
import com.calendarugr.user_service.dtos.ErrorResponseDTO;
import com.calendarugr.user_service.entities.User;
import com.calendarugr.user_service.mappers.UserMapper;
import com.calendarugr.user_service.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserDTO> userDTOs = UserMapper.toDTOList(users);
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("nickname/{nickname}")
    public ResponseEntity<?> getUserByNickname(@PathVariable String nickname) {
        Optional<User> user = userService.findByNickname(nickname);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado"));
        } else {
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.findByEmail(email);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado"));
        } else {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        }
    }

    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("X-User-ID") String userIdHeader,
                                         @RequestHeader("X-User-Role") String userRoleHeader) {
        Long userId = Long.parseLong(userIdHeader);
        Optional<User> user = userService.findById(userId);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado"));
        } else {
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("BadRequest", "La información del usuario está incompleta"));
        }

        if (userService.findByNickname(user.getNickname()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO("Conflict", "El nickname ya está en uso"));
        }
    
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO("Conflict", "El email ya está en uso"));
        }
        String passRegex = "^(?=.*[A-Z])(?=.*[0-9]).{9,}$"; 
        if (!user.getPassword().matches(passRegex)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("BadRequest", "La contraseña debe contener al menos 9 caracteres, una letra mayúscula y un número"));
        }
    
        if (!user.getEmail().endsWith("@ugr.es") && !user.getEmail().endsWith("@correo.ugr.es")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("BadRequest", "El correo electrónico debe terminar en @ugr.es o @correo.ugr.es"));
        }
    
        User savedUser = userService.registerUser(user);
        UserDTO userDTO = UserMapper.toDTO(savedUser);
    
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED); 
    }

    @PostMapping("/reset-pass-mail")
    public ResponseEntity<?> resetPasswordMail(@RequestBody EmailDTO emailDTO) {
        String email = emailDTO.getEmail();
        if (email == null || email.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("BadRequest", "El correo electrónico no puede estar vacío"));
        }

        Optional<User> user = userService.findByEmail(email);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado"));
        }

        userService.sendResetPasswordEmail(user.get());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ChangePasswordRequestDTO changePasswordRequest) {
        String token = changePasswordRequest.getToken();
        String newPassword = changePasswordRequest.getNewPassword();
        String currentPassword = changePasswordRequest.getCurrentPassword();

        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()
            || currentPassword == null || currentPassword.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("BadRequest", "Token y nueva contraseña son obligatorios"));
        }

        String passRegex = "^(?=.*[A-Z])(?=.*[0-9]).{9,}$";
        if (!newPassword.matches(passRegex)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("BadRequest", "La contraseña debe contener al menos 9 caracteres, una letra mayúscula y un número"));
        }

        Optional<User> user = userService.resetPassword(token, newPassword);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado o token inválido"));
        } else {
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<?> activateUser(@RequestParam String token){
        Optional<User> user = userService.activateUser(token);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado o token inválido"));
        } else {
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PutMapping("/deactivate")
    public ResponseEntity<?> deactivateUser(@RequestBody ChangePasswordRequestDTO changePasswordRequest,
                                            @RequestHeader("X-User-ID") String userIdHeader,
                                            @RequestHeader("X-User-Role") String userRoleHeader) throws Exception{

        Long id = Long.parseLong(userIdHeader);

        Optional<User> user = userService.deactivateUser(id, changePasswordRequest.getCurrentPassword());
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado o contraseña no coincidente"));
        } else {
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PutMapping("/nickname")
    public ResponseEntity<?> updateNickname(@RequestBody User user,
                                            @RequestHeader("X-User-ID") String userIdHeader,
                                            @RequestHeader("X-User-Role") String userRoleHeader){

        Long id = Long.parseLong(userIdHeader);

        if (!userService.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado"));
        }

        User userUpdated = userService.updateNickname(id, user);
        UserDTO userDTO = UserMapper.toDTO(userUpdated);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @PutMapping("/role")
    public ResponseEntity<?> changeRole(@RequestHeader("X-User-ID") String userIdHeader,
                                        @RequestHeader("X-User-Role") String userRoleHeader){

        Long id = Long.parseLong(userIdHeader);

        Optional<User> user = userService.changeRole(id);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado"));
        } else {
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequestDTO changePasswordRequest,
                                            @RequestHeader("X-User-ID") String userIdHeader,
                                            @RequestHeader("X-User-Role") String userRoleHeader){

        Long id = Long.parseLong(userIdHeader);

        String passRegex = "^(?=.*[A-Z])(?=.*[0-9]).{9,}$";
        if (!changePasswordRequest.getNewPassword().matches(passRegex)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDTO("BadRequest", "La contraseña debe contener al menos 9 caracteres, una letra mayúscula y un número"));
        }
        
        Optional<User> user = userService.changePassword(id, changePasswordRequest);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado o contraseña no coincidente"));
        } else {
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PutMapping("/activate-notifications")
    public ResponseEntity<?> activateNotifications(@RequestHeader("X-User-ID") String userIdHeader,
                                                    @RequestHeader("X-User-Role") String userRoleHeader){

        Long id = Long.parseLong(userIdHeader);

        Optional<User> user = userService.activateNotifications(id);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado"));
        } else {
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PutMapping("/deactivate-notifications")
    public ResponseEntity<?> deactivateNotifications(@RequestHeader("X-User-ID") String userIdHeader,
                                                    @RequestHeader("X-User-Role") String userRoleHeader){
        Long id = Long.parseLong(userIdHeader);

        Optional<User> user = userService.deactivateNotifications(id);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado"));
        } else {
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PostMapping("/email-list")
    public ResponseEntity<?> getEmails(@RequestBody List<Long> ids) {
        List<String> emails = userService.getEmailsWhereNotifications(ids);
        return ResponseEntity.ok(emails);
    }

    // ADMIN Endpoints - Without DTOs

    @PostMapping("admin/register")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userService.findByNickname(user.getNickname()).isPresent() || userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO("Conflict", "El usuario ya existe"));
        }
        return ResponseEntity.ok(userService.save(user));
    }

    @PutMapping("admin/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!userService.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado"));
        }
        return ResponseEntity.ok(userService.update(id, user));
    }

    @DeleteMapping("admin/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userService.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO("NotFound", "Usuario no encontrado"));
        }
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}