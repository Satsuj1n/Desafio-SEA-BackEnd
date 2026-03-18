package com.felipelima.clientmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.felipelima.clientmanager.entity.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
