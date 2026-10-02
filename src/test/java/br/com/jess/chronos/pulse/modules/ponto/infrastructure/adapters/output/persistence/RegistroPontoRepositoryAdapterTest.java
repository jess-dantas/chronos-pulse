package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.ponto.domain.model.RegistroPonto;
import br.com.jess.chronos.pulse.modules.ponto.domain.model.TipoRegistro;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroPontoRepositoryAdapterTest {

    @Mock private RegistroPontoJpaRepository jpaRepository;
    @Mock private RegistroPontoMapper mapper;
    @InjectMocks private RegistroPontoRepositoryAdapter adapter;

    private RegistroPonto modelo() {
        return new RegistroPonto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Instant.now(),
                null, TipoRegistro.ENTRADA, BigDecimal.ZERO, BigDecimal.ZERO, null, null, false, 1L);
    }

    private RegistroPontoJpaEntity entidade() {
        RegistroPontoJpaEntity e = new RegistroPontoJpaEntity();
        e.setId(UUID.randomUUID());
        return e;
    }

    @Test
    void deveSalvarERetornarModelo() {
        RegistroPonto modelo = modelo();
        RegistroPontoJpaEntity entidade = entidade();
        when(mapper.toEntity(modelo)).thenReturn(entidade);
        when(jpaRepository.save(entidade)).thenReturn(entidade);
        when(mapper.toModel(entidade)).thenReturn(modelo);

        assertThat(adapter.salvar(modelo)).isEqualTo(modelo);
    }

    @Test
    void deveCopiarVersionDeRegistroExistenteAoSalvar() {
        RegistroPonto modelo = modelo();
        RegistroPontoJpaEntity atual = entidade();
        RegistroPontoJpaEntity paraSalvar = entidade();
        paraSalvar.setId(atual.getId());
        atual.setVersion(7L);
        when(mapper.toEntity(modelo)).thenReturn(paraSalvar);
        when(jpaRepository.findById(atual.getId())).thenReturn(Optional.of(atual));
        when(jpaRepository.save(paraSalvar)).thenReturn(paraSalvar);
        when(mapper.toModel(paraSalvar)).thenReturn(modelo);

        adapter.salvar(modelo);

        assertThat(paraSalvar.getVersion()).isEqualTo(7L);
        verify(jpaRepository).save(paraSalvar);
    }

    @Test
    void deveManterVersionNulaQuandoRegistroNaoExiste() {
        RegistroPonto modelo = modelo();
        RegistroPontoJpaEntity entidade = entidade();
        when(mapper.toEntity(modelo)).thenReturn(entidade);
        when(jpaRepository.findById(entidade.getId())).thenReturn(Optional.empty());
        when(jpaRepository.save(entidade)).thenReturn(entidade);
        when(mapper.toModel(entidade)).thenReturn(modelo);

        adapter.salvar(modelo);

        assertThat(entidade.getVersion()).isNull();
        verify(jpaRepository).save(entidade);
    }

    @Test
    void deveBuscarPorIdExistente() {
        UUID id = UUID.randomUUID();
        RegistroPontoJpaEntity entidade = entidade();
        RegistroPonto modelo = modelo();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(entidade));
        when(mapper.toModel(entidade)).thenReturn(modelo);

        assertThat(adapter.buscarPorId(id)).contains(modelo);
    }

    @Test
    void deveRetornarVazioQuandoIdNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThat(adapter.buscarPorId(id)).isEmpty();
    }

    @Test
    void deveRetornarProximoNsr() {
        when(jpaRepository.obterProximoNsr()).thenReturn(42L);
        assertThat(adapter.obterProximoNsr()).isEqualTo(42L);
    }

    @Test
    void deveBuscarUltimoTipoPorColaborador() {
        UUID colaboradorId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-10-02T03:00:00Z");
        Instant fim = Instant.parse("2026-10-03T03:00:00Z");
        when(jpaRepository.buscarUltimoTipoPorColaborador(colaboradorId, tenantId, inicio, fim)).thenReturn(Optional.of(TipoRegistro.ENTRADA));

        Optional<TipoRegistro> resultado = adapter.buscarUltimoTipoPorColaborador(colaboradorId, tenantId, inicio, fim);

        assertThat(resultado).contains(TipoRegistro.ENTRADA);
        verify(jpaRepository).buscarUltimoTipoPorColaborador(colaboradorId, tenantId, inicio, fim);
    }
}
