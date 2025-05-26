package com.calendarugr.user_service.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // to prevent lazy initialization exception
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 50)
    @Column(unique = true, length = 50)
    private String nickname;

    @Size(max = 100)
    @Column(unique = true, length = 100)
    private String email;

    @Size(max = 255)
    @Column(length = 255)
    private String password;

    @ManyToOne
    @JoinColumn(name= "role", nullable = false)
    private Role role;

    @NotNull
    @Column(nullable = false)
    private Boolean notification = false;
}
