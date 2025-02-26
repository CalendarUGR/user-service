package com.calendarugr.user_service.controllers;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.calendarugr.user_service.entities.User;
import com.calendarugr.user_service.services.UserService;
import com.rabbitmq.client.RpcClient.Response;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<Iterable<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("nickname/{nickname}")
    public ResponseEntity<User> getUserByNickname(@PathVariable String nickname) {
        Optional<User> user = userService.findByNickname(nickname);
        if (!user.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.findByEmail(email);
        if (!user.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        }
    }

    @PutMapping("/updateNickname/{id}")
    public ResponseEntity<User> updateNickname(@PathVariable Long id, @RequestBody User user) {
        if (!userService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(userService.updateNickname(id, user));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        if (user == null) {
            //return new ResponseEntity<>("User data is missing", HttpStatus.BAD_REQUEST);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User data is missing");
        }

        if (userService.findByNickname(user.getNickname()).isPresent() || userService.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
        }

        String passRegex = "^(?=.*[A-Z])(?=.*[0-9]).{9,}$"; 
        if (!user.getPassword().matches(passRegex)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must contain at least 9 characters, one uppercase letter and one number");   
        }
        System.out.println("Email: " + user.getEmail()+ " ends with ugr.es: " + user.getEmail().endsWith("@ugr.es")+ " ends with correo.ugr.es: " + user.getEmail().endsWith("@correo.ugr.es"));
        if (!user.getEmail().endsWith("@ugr.es") && !user.getEmail().endsWith("@correo.ugr.es")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email must be from UGR domain");
        }

        User savedUser = userService.registerUser(user);

        return new ResponseEntity<>(savedUser, HttpStatus.CREATED); 
    }

    @GetMapping("/activate")
    public ResponseEntity<User> activateUser(@RequestParam String token){
        System.out.println("Token: " + token);
        Optional<User> user = userService.activateUser(token);
        if (!user.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            //TODO: Redirect to login page
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        }
    }

    @GetMapping("/deactivate")
    public ResponseEntity<User> deactivateUser(@RequestParam Long id){
        Optional<User> user = userService.deactivateUser(id);
        if (!user.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        }
    }

    // ADMIN Endpoints

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (userService.findByNickname(user.getNickname()).isPresent() || userService.findByEmail(user.getEmail()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return ResponseEntity.ok(userService.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!userService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(userService.update(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userService.findById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
