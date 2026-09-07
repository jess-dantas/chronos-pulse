package br.com.jess.chronos.pulse.modules.protocolo.service;

import br.com.jess.chronos.pulse.modules.protocolo.domain.entity.Protocolo;
import br.com.jess.chronos.pulse.modules.protocolo.repository.ProtocoloRepository;
import br.com.jess.chronos.pulse.modules.protocolo.web.dto.AtualizarStatusProtocoloDTO;
import br.com.jess.chronos.pulse.modules.protocolo.web.dto.CadastrarProtocoloDTO;
import br.com.jess.chronos.pulse.modules.protocolo.web.dto.ProtocoloResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProtocoloService {

    private final ProtocoloRepository protocoloRepository;

    @Transactional
    public ProtocoloResponseDTO cadastrar(CadastrarProtocoloDTO dto, UUID tenantId) {
        if (protocoloRepository.existsByNumeroProtocoloAndTenantId(dto.numeroProtocolo(), tenantId)) {
            throw new IllegalArgumentException("Número de protocolo já cadastrado");
        }

        Protocolo protocolo = Protocolo.builder()
                .tenantId(tenantId)
                .numeroProtocolo(dto.numeroProtocolo())
                .tipo(dto.tipo().toUpperCase())
                .assunto(dto.assunto())
                .descricao(dto.descricao())
                .remetente(dto.remetente())
                .destinatario(dto.destinatario())
                .status(dto.status() != null ? dto.status().toUpperCase() : "RECEBIDO")
                .responsavel(dto.responsavel())
                .observacoes(dto.observacoes())
                .ativo(true)
                .build();

        Protocolo salvo = protocoloRepository.save(protocolo);
        return toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public Page<ProtocoloResponseDTO> listar(UUID tenantId, Pageable pageable) {
        return protocoloRepository.findAllByTenantId(tenantId, pageable)
                .map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ProtocoloResponseDTO buscarPorId(UUID id, UUID tenantId) {
        return protocoloRepository.findByIdAndTenantId(id, tenantId)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado"));
    }

    @Transactional
    public ProtocoloResponseDTO atualizarStatus(UUID id, AtualizarStatusProtocoloDTO dto, UUID tenantId) {
        Protocolo protocolo = protocoloRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado"));

        protocolo.setStatus(dto.status().toUpperCase());
        if (dto.responsavel() != null) {
            protocolo.setResponsavel(dto.responsavel());
        }
        if (dto.observacoes() != null) {
            protocolo.setObservacoes(dto.observacoes());
        }

        Protocolo salvo = protocoloRepository.save(protocolo);
        return toResponseDTO(salvo);
    }

    private ProtocoloResponseDTO toResponseDTO(Protocolo p) {
        return new ProtocoloResponseDTO(
                p.getId(), p.getNumeroProtocolo(), p.getTipo(), p.getAssunto(),
                p.getDescricao(), p.getRemetente(), p.getDestinatario(),
                p.getDataProtocolo(), p.getStatus(), p.getResponsavel(),
                p.getObservacoes(), p.getAtivo());
    }
}
