package com.spring.marombaapp.login.repository;

import com.spring.marombaapp.login.model.Role;
import com.spring.marombaapp.login.model.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(RoleEnum name);
}
