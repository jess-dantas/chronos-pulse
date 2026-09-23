package br.com.jess.chronos.pulse.modules.titularidade.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.titularidade.domain.model.TitularidadeCodigo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TitularidadeCodigoJpaRepository extends JpaRepository<TitularidadeCodigo, UUID> {

    List<TitularidadeCodigo> findByTransferenciaIdAndEtapaOrderByCriadoEmDesc(
            UUID transferenciaId, String etapa);

    void deleteByTransferenciaIdAndEtapa(UUID transferenciaId, String etapa);
}
