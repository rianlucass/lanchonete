package com.br.api.lanchonete.controllers;

import com.br.api.lanchonete.domain.user.AuthenticationDTO;
import com.br.api.lanchonete.domain.user.LoginResponseDTO;
import com.br.api.lanchonete.domain.user.RegisterDTO;
import com.br.api.lanchonete.domain.user.User;
import com.br.api.lanchonete.infra.security.TokenService;
import com.br.api.lanchonete.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @PostMapping("login")
    public ResponseEntity login(@RequestBody @Validated AuthenticationDTO login) {
        var UsernamePassword = new UsernamePasswordAuthenticationToken(login.username(), login.password());
        var auth = this.authenticationManager.authenticate(UsernamePassword);
        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("register")
    public ResponseEntity responseEntity(@RequestBody @Validated RegisterDTO register) {
        if(this.userRepository.findByUsername(register.username()) != null)
            return ResponseEntity.badRequest().build();

        if(this.userRepository.findByEmail(register.email()) != null)
            return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(register.password());
        User newUser = new User(register.username(), register.email(), register.name(), encryptedPassword, register.role());

        this.userRepository.save(newUser);
        return ResponseEntity.ok().build();
    }

}
