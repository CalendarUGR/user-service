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
import com.calendarugr.user_service.dtos.ErrorResponseDTO;

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
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO("Conflict", "El rol ya existe"));
        }
        return ResponseEntity.ok(roleService.save(role));
    }

    @DeleteMapping("/roles/delete")
    public ResponseEntity<?> deleteRole(@RequestBody Role role) {
        Optional<Role> roleToDelete = roleService.findByName(role.getName());
        if (roleToDelete.isPresent()) {
            Long id = roleToDelete.get().getId();
            roleService.delete(id);
            return ResponseEntity.ok().body(new ErrorResponseDTO("Success", "Rol eliminado correctamente"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponseDTO("NotFound", "Rol no encontrado"));
    }
}