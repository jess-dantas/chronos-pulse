package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.ConfiguracaoFiscal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfiguracaoFiscalRepositoryAdapterTest {

    @Mock private ConfiguracaoFiscalJpaRepository jpaRepository;
    @Mock private ConfiguracaoFiscalMapper mapper;
    @InjectMocks private ConfiguracaoFiscalRepositoryAdapter adapter;

    private final UUID tenantId = UUID.randomUUID();

    private ConfiguracaoFiscalJpaEntity entidade() {
        ConfiguracaoFiscalJpaEntity e = new ConfiguracaoFiscalJpaEntity();
        e.setTenantId(tenantId);
        e.setPrtpNome("CHRONOS PULSE");
        e.setPrtpVersao("1.0.0");
        return e;
    }

    private ConfiguracaoFiscal modelo() {
        return new ConfiguracaoFiscal(tenantId, null, null, "CHRONOS PULSE", "1.0.0", null, null, null);
    }

    @Test
    void deveBuscarPorTenantExistente() {
        ConfiguracaoFiscalJpaEntity entidade = entidade();
        ConfiguracaoFiscal modelo = modelo();
        when(jpaRepository.findById(tenantId)).thenReturn(Optional.of(entidade));
        when(mapper.toModel(entidade)).thenReturn(modelo);

        assertThat(adapter.buscarPorTenant(tenantId)).contains(modelo);
    }

    @Test
    void deveRetornarVazioQuandoTenantNaoTemConfiguracao() {
        when(jpaRepository.findById(tenantId)).thenReturn(Optional.empty());

        assertThat(adapter.buscarPorTenant(tenantId)).isEmpty();
    }

    @Test
    void deveSalvarERetornarModelo() {
        ConfiguracaoFiscal modelo = modelo();
        ConfiguracaoFiscalJpaEntity entidade = entidade();
        when(mapper.toEntity(modelo)).thenReturn(entidade);
        when(jpaRepository.save(entidade)).thenReturn(entidade);
        when(mapper.toModel(entidade)).thenReturn(modelo);

        assertThat(adapter.salvar(modelo)).isEqualTo(modelo);
    }
}