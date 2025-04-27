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

import com.calendarugr.user_service.dtos.ChangePasswordRequestDTO;
import com.calendarugr.user_service.dtos.UserDTO;
import com.calendarugr.user_service.entities.User;
import com.calendarugr.user_service.mappers.UserMapper;
import com.calendarugr.user_service.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // Auxiliary method to check if the user is not trying to modify another user's data
    private ResponseEntity<String> authenticateRequest(Long userId, String userIdHeader, String userRoleHeader) {
        System.out.println("User ID: " + userId + " User ID Header: " + userIdHeader + " User Role Header: " + userRoleHeader);
        if (!userId.equals(Long.parseLong(userIdHeader)) && !userRoleHeader.equals("ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No está autorizado para modificar este usuario");
        }
        return null; // Null means the request is authenticated
    }

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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }else{
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @GetMapping("/email/{email}") // Here I dont use the DTO because I need the user entity to check extra info in auth service
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.findByEmail(email);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }else{
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La información del usuario está incompleta");
        }
    
        if (userService.findByNickname(user.getNickname()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El nickname ya está en uso");
        }
    
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El email ya está en uso");
        }
        String passRegex = "^(?=.*[A-Z])(?=.*[0-9]).{9,}$"; 
        if (!user.getPassword().matches(passRegex)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La contraseña debe contener al menos 9 caracteres, una letra mayúscula y un número");  
        }
    
        System.out.println("Email: " + user.getEmail() + " ends with ugr.es: " + user.getEmail().endsWith("@ugr.es") + " ends with correo.ugr.es: " + user.getEmail().endsWith("@correo.ugr.es"));
        if (!user.getEmail().endsWith("@ugr.es") && !user.getEmail().endsWith("@correo.ugr.es")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El correo electrónico debe terminar en @ugr.es o @correo.ugr.es");
        }
    
        User savedUser = userService.registerUser(user);
        UserDTO userDTO = UserMapper.toDTO(savedUser);
    
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED); 
    }

    @GetMapping("/activate") // This endpont is used by email-service to activate the user
    public ResponseEntity<?> activateUser(@RequestParam String token){
        
        Optional<User> user = userService.activateUser(token);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado o token inválido");
        }else{
            //TODO: Redirect to login page
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id, @RequestBody ChangePasswordRequestDTO changePasswordRequest,
                                            @RequestHeader("X-User-ID") String userIdHeader,
                                            @RequestHeader("X-User-Role") String userRoleHeader){

        ResponseEntity<String> authResponse = authenticateRequest(id,userIdHeader,userRoleHeader);

        if (authResponse != null) {
            return authResponse;
        }

        Optional<User> user = userService.deactivateUser(id, changePasswordRequest.getCurrentPassword());
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado o contraseña no coincidente");

        }else{
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PutMapping("/nickname/{id}")
    public ResponseEntity<?> updateNickname(@PathVariable Long id, @RequestBody User user,
                                            @RequestHeader("X-User-ID") String userIdHeader,
                                            @RequestHeader("X-User-Role") String userRoleHeader){

        ResponseEntity<String> authResponse = authenticateRequest(id ,userIdHeader,userRoleHeader);

        if (authResponse != null) {
            return authResponse;
        }
        
        if (!userService.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        User userUpdated = userService.updateNickname(id, user);
        UserDTO userDTO = UserMapper.toDTO(userUpdated);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @PutMapping("/role/{id}")
    public ResponseEntity<?> changeRole(@PathVariable Long id,
                                        @RequestHeader("X-User-ID") String userIdHeader,
                                        @RequestHeader("X-User-Role") String userRoleHeader){

        ResponseEntity<String> authResponse = authenticateRequest(id ,userIdHeader,userRoleHeader);

        if (authResponse != null) {
            return authResponse;
        }
        
        Optional<User> user = userService.changeRole(id);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }else{
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PutMapping("/password/{id}")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequestDTO changePasswordRequest,
                                            @RequestHeader("X-User-ID") String userIdHeader,
                                            @RequestHeader("X-User-Role") String userRoleHeader){

        ResponseEntity<String> authResponse = authenticateRequest(id ,userIdHeader,userRoleHeader);

        if (authResponse != null) {
            return authResponse;
        }

        String passRegex = "^(?=.*[A-Z])(?=.*[0-9]).{9,}$"; 
        if (!changePasswordRequest.getNewPassword().matches(passRegex)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La contraseña debe contener al menos 9 caracteres, una letra mayúscula y un número");   
        }
        
        Optional<User> user = userService.changePassword(id, changePasswordRequest);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado o contraseña no coincidente");
        }else{
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PutMapping("/activate-notifications/{id}")
    public ResponseEntity<?> activateNotifications(@PathVariable Long id,
                                                    @RequestHeader("X-User-ID") String userIdHeader,
                                                    @RequestHeader("X-User-Role") String userRoleHeader){

        ResponseEntity<String> authResponse = authenticateRequest(id ,userIdHeader,userRoleHeader);

        if (authResponse != null) {
            return authResponse;
        }

        Optional<User> user = userService.activateNotifications(id);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }else{
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PutMapping("/deactivate-notifications/{id}")
    public ResponseEntity<?> deactivateNotifications(@PathVariable Long id,
                                                    @RequestHeader("X-User-ID") String userIdHeader,
                                                    @RequestHeader("X-User-Role") String userRoleHeader){

        ResponseEntity<String> authResponse = authenticateRequest(id ,userIdHeader,userRoleHeader);

        if (authResponse != null) {
            return authResponse;
        }

        Optional<User> user = userService.deactivateNotifications(id);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }else{
            UserDTO userDTO = UserMapper.toDTO(user.get());
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
    }

    @PostMapping("/email-list") // ENdpoint used by academic-subscription-service to notify certain users
    public ResponseEntity<?> getEmails(@RequestBody List<Long> ids) {
        List<String> emails = userService.getEmailsWhereNotifications(ids);
        return ResponseEntity.ok(emails);
    }

    // ADMIN Endpoints - Without DTOs

    @PostMapping("admin/register")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userService.findByNickname(user.getNickname()).isPresent() || userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El usuario ya existe");
        }
        return ResponseEntity.ok(userService.save(user));
    }

    @PutMapping("admin/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!userService.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
        return ResponseEntity.ok(userService.update(id, user));
    }

    @DeleteMapping("admin/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userService.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
