package br.com.jess.chronos.pulse.modules.admin.infrastructure.adapters.output.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regressão do first-run wizard: o único INSERT JPA de {@code admin_plataforma}
 * é o bootstrap. Sem preenchimento de {@code criadoEm} no {@code @PrePersist}
 * da JPA entity, o Hibernate enviava NULL e o Postgres/H2 rejeitava com
 * NOT NULL violation (409 "Conflito de integridade de dados").
 */
@DataJpaTest
@ActiveProfiles("test")
class AdminPlataformaJpaEntityPersistTest {

    @Autowired
    private AdminPlataformaJpaRepository repository;

    @Test
    void devePreencherCriadoEmAutomaticamenteQuandoNulo() {
        var admin = AdminPlataformaJpaEntity.builder()
                .username("fundador")
                .senhaHash("$2a$10$hashDeTeste")
                .nomeCompleto("Fundador")
                .email("fundador@example.com")
                .twoFactorEnabled(false)
                .criadoEm(null)
                .build();

        var salvo = repository.saveAndFlush(admin);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getCriadoEm())
                .as("criado_em NOT NULL deve ser preenchido pelo @PrePersist")
                .isNotNull();
        assertThat(salvo.isAtivo()).isTrue();

        var recarregado = repository.findById(salvo.getId()).orElseThrow();
        assertThat(recarregado.getCriadoEm()).isNotNull();
    }
}
