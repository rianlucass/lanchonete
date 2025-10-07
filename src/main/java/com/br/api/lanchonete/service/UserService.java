package com.br.api.lanchonete.service;

import com.br.api.lanchonete.domain.user.ResponsetUserDTO;
import com.br.api.lanchonete.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<ResponsetUserDTO> getAllUsers (){
        return userRepository.findAll().stream().map(user -> {
            ResponsetUserDTO responsetUserDTO = new ResponsetUserDTO(
                    user.getId(),
                    user.getUsername()
            );
            return responsetUserDTO;
        }).collect(Collectors.toList());
    }
}
