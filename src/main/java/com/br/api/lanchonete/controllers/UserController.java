package com.br.api.lanchonete.controllers;

import com.br.api.lanchonete.domain.user.ResponsetUserDTO;
import com.br.api.lanchonete.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("users")
    public List<ResponsetUserDTO> getUsers (){
        return userService.getAllUsers();
    }


}
