package com.spring.marombaapp.login.repository;

import com.spring.marombaapp.login.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String nomeUsuario);

    Boolean existePorNomeUsuario(String nomeUsuario);

    Boolean existePorEmail(String email);
}
