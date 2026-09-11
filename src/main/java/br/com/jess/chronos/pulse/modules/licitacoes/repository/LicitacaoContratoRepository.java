package br.com.jess.chronos.pulse.modules.licitacoes.repository;

import br.com.jess.chronos.pulse.modules.licitacoes.domain.entity.ContratoLicitacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LicitacaoContratoRepository extends JpaRepository<ContratoLicitacao, UUID> {
}