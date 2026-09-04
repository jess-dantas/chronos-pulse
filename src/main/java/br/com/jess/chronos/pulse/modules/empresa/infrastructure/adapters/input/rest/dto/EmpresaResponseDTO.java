package br.com.jess.chronos.pulse.modules.empresa.infrastructure.adapters.input.rest.dto;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;

import java.time.Instant;
import java.util.UUID;

public record EmpresaResponseDTO(
        UUID id,
        String cnpj,
        String nome,
        String responsavelNome,
        String responsavelCpf,
        String responsavelEmail,
        String responsavelCelular,
        boolean ativo,
        Instant criadoEm
) {
    public static EmpresaResponseDTO fromDomain(Empresa e) {
        return new EmpresaResponseDTO(
                e.getId(), e.getCnpj(), e.getNome(),
                e.getResponsavelNome(), e.getResponsavelCpf(),
                e.getResponsavelEmail(), e.getResponsavelCelular(),
                e.isAtivo(), e.getCriadoEm()
        );
    }
}
