package com.br.api.lanchonete.repositories;

import com.br.api.lanchonete.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository extends JpaRepository<User, String > {
    UserDetails findByUsername(String username);
    User findByEmail(String email);
}
