package com.calendarugr.user_service.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calendarugr.user_service.entities.Role;
import com.calendarugr.user_service.services.RoleService;

@RestController
@RequestMapping("/user")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/roles/all")
    public ResponseEntity<Iterable<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @PostMapping("/roles/create")
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        if (roleService.findByName(role.getName()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return ResponseEntity.ok(roleService.save(role));
    }

    @DeleteMapping("/roles/delete")
    public ResponseEntity<?> deleteRole(@RequestBody Role role) {
        Optional<Role> roleToDelete = roleService.findByName(role.getName());
        if (roleToDelete.isPresent()) {
            Long id = roleToDelete.get().getId();
            roleService.delete(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
