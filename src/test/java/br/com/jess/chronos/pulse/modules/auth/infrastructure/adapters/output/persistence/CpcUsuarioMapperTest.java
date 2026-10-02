package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CpcUsuarioMapperTest {

    private final CpcUsuarioMapperImpl mapper = new CpcUsuarioMapperImpl();

    @Test
    void toModelPreservaAtivoECriadoEm() {
        Instant criadoEm = Instant.parse("2024-01-15T10:00:00Z");
        CpcUsuarioJpaEntity entidade = new CpcUsuarioJpaEntity();
        entidade.setId(UUID.randomUUID());
        entidade.setCpcId(UUID.randomUUID());
        entidade.setCpf("22222222222");
        entidade.setNome("Conta Suspenso");
        entidade.setSenhaHash("hash");
        entidade.setRole(Role.GESTOR_RH);
        entidade.setTenantId(UUID.randomUUID());
        entidade.setAtivo(false);
        entidade.setCriadoEm(criadoEm);

        CpcUsuario model = mapper.toModel(entidade);

        // Regressão: a fábrica antiga descartava os campos finais e a conta
        // suspenso voltava ativo=true (login/refresh/filtro aceitavam).
        assertThat(model.isAtivo()).isFalse();
        assertThat(model.getCriadoEm()).isEqualTo(criadoEm);
        assertThat(model.getCpf()).isEqualTo("22222222222");
        assertThat(model.getRole()).isEqualTo(Role.GESTOR_RH);
    }

    @Test
    void toEntityPreservaAtivoInativo() {
        CpcUsuario model = new CpcUsuario(null, null, "22222222222", "Conta",
                "a@b.com", "hash", Role.ADMIN_EMPRESA, UUID.randomUUID());
        CpcUsuario suspenso = model.comAtivo(false);

        CpcUsuarioJpaEntity entidade = mapper.toEntity(suspenso);

        assertThat(entidade.isAtivo()).isFalse();
        assertThat(entidade.getCriadoEm()).isEqualTo(model.getCriadoEm());
    }

    @Test
    void comAtivoPreservaOsDemaisCampos() {
        CpcUsuario original = new CpcUsuario(null, null, "11111111111", "Admin",
                "a@b.com", "hash", Role.ADMIN_EMPRESA, UUID.randomUUID());
        original.registrarLoginSucesso();

        CpcUsuario suspenso = original.comAtivo(false);

        assertThat(suspenso.isAtivo()).isFalse();
        assertThat(suspenso.getId()).isEqualTo(original.getId());
        assertThat(suspenso.getCpf()).isEqualTo(original.getCpf());
        assertThat(suspenso.getRole()).isEqualTo(original.getRole());
        assertThat(suspenso.getTenantId()).isEqualTo(original.getTenantId());
        assertThat(suspenso.getSenhaHash()).isEqualTo(original.getSenhaHash());
        assertThat(suspenso.getCriadoEm()).isEqualTo(original.getCriadoEm());
    }
}
