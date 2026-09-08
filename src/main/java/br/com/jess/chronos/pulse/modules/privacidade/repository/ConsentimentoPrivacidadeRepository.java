package br.com.jess.chronos.pulse.modules.privacidade.repository;

import br.com.jess.chronos.pulse.modules.privacidade.domain.ConsentimentoPrivacidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentimentoPrivacidadeRepository extends JpaRepository<ConsentimentoPrivacidade, UUID> {

    List<ConsentimentoPrivacidade> findByCpcIdOrderByDataConsentimentoDesc(UUID cpcId);

    Optional<ConsentimentoPrivacidade> findTopByCpcIdOrderByDataConsentimentoDesc(UUID cpcId);
}