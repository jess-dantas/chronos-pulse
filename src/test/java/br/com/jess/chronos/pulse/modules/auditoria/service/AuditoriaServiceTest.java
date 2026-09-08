package br.com.jess.chronos.pulse.modules.auditoria.service;

import br.com.jess.chronos.pulse.modules.auditoria.domain.entity.Auditoria;
import br.com.jess.chronos.pulse.modules.auditoria.repository.AuditoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @Captor
    private ArgumentCaptor<Auditoria> captor;

    private AuditoriaService auditoriaService;

    @BeforeEach
    void setUp() {
        auditoriaService = new AuditoriaService(auditoriaRepository);
    }

    @Test
    void deveRegistrarPrimeiroRegistroComHashAnteriorGenesis() {
        when(auditoriaRepository.findTopByOrderByDataHoraDesc()).thenReturn(Optional.empty());

        auditoriaService.registrar("TESTE", "ENTIDADE", null, "descricao",
                null, null, null, null, null, null, null);

        verify(auditoriaRepository).save(captor.capture());
        Auditoria registro = captor.getValue();

        assertThat(registro.getHashAnterior()).isEqualTo("GENESIS");
        assertThat(registro.getHashRegistro()).hasSize(64);
        assertThat(registro.getHashRegistro()).isNotEqualTo("GENESIS");
    }

    @Test
    void deveEncadearHashDoRegistroAnterior() {
        Auditoria anterior = Auditoria.builder()
                .hashRegistro("hash-do-registro-anterior")
                .build();
        when(auditoriaRepository.findTopByOrderByDataHoraDesc()).thenReturn(Optional.of(anterior));

        auditoriaService.registrar("TESTE", "ENTIDADE", UUID.randomUUID(), "descricao",
                UUID.randomUUID(), UUID.randomUUID(), "12345678901", "ADMIN_EMPRESA",
                null, null, null);

        verify(auditoriaRepository).save(captor.capture());
        Auditoria registro = captor.getValue();

        assertThat(registro.getHashAnterior()).isEqualTo("hash-do-registro-anterior");
        assertThat(registro.getHashRegistro()).hasSize(64);
        assertThat(registro.getHashRegistro()).isNotEqualTo("hash-do-registro-anterior");
    }

    @Test
    void devePersistirContextoDoUsuario() {
        UUID tenantId = UUID.randomUUID();
        UUID cpcId = UUID.randomUUID();
        when(auditoriaRepository.findTopByOrderByDataHoraDesc()).thenReturn(Optional.empty());

        auditoriaService.registrar("CADASTRO", "COLABORADOR", UUID.randomUUID(), "Cadastro",
                tenantId, cpcId, "12345678901", "ADMIN_EMPRESA", null, null, null);

        verify(auditoriaRepository).save(captor.capture());
        Auditoria registro = captor.getValue();

        assertThat(registro.getTenantId()).isEqualTo(tenantId);
        assertThat(registro.getUsuarioCpcId()).isEqualTo(cpcId);
        assertThat(registro.getUsuarioCpf()).isEqualTo("12345678901");
        assertThat(registro.getPapel()).isEqualTo("ADMIN_EMPRESA");
        assertThat(registro.getAcao()).isEqualTo("CADASTRO");
        assertThat(registro.getEntidade()).isEqualTo("COLABORADOR");
        assertThat(registro.getDataHora()).isNotNull();
    }
}