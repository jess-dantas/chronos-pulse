package br.com.jess.chronos.pulse.modules.modulo.repository;

import br.com.jess.chronos.pulse.modules.modulo.domain.entity.ModuloPlataforma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModuloPlataformaRepository extends JpaRepository<ModuloPlataforma, UUID> {

    Optional<ModuloPlataforma> findByCodigo(String codigo);

    List<ModuloPlataforma> findByAtivoTrueOrderByNomeAsc();
}