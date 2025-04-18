package com.calendarugr.user_service.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.calendarugr.user_service.entities.Role;
import com.calendarugr.user_service.repositories.RoleRepository;

@Service
public class RoleService {

    @Autowired 
    private RoleRepository roleRepository;

    public Iterable<Role> findAll() {
        return roleRepository.findAll();
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }

    public void delete(Long id) {
        roleRepository.deleteById(id);
    }

}
