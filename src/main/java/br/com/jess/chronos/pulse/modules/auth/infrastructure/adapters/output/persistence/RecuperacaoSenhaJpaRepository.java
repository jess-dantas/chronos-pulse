package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecuperacaoSenhaJpaRepository extends JpaRepository<RecuperacaoSenhaJpaEntity, UUID> {
    Optional<RecuperacaoSenhaJpaEntity> findFirstByCpfOrderByCriadoEmDesc(String cpf);
    List<RecuperacaoSenhaJpaEntity> findByCpf(String cpf);
}