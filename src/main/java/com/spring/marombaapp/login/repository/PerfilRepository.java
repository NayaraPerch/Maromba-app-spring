package com.spring.marombaapp.login.repository;

import com.spring.marombaapp.login.model.Perfil;
import com.spring.marombaapp.login.model.PerfilEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil,Long> {
    Optional<Perfil> buscaPorNome(PerfilEnum nome);
}
