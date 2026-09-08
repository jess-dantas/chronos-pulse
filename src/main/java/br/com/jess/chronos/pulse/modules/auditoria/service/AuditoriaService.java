package br.com.jess.chronos.pulse.modules.auditoria.service;

import br.com.jess.chronos.pulse.modules.auditoria.domain.entity.Auditoria;
import br.com.jess.chronos.pulse.modules.auditoria.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    /**
     * Registra uma mutação na trilha de auditoria imutável (hash chain).
     * A operação roda em uma transação própria (REQUIRES_NEW) para garantir
     * persistência mesmo se a transação de negócio for revertida.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String acao,
                          String entidade,
                          UUID entidadeId,
                          String descricao,
                          UUID tenantId,
                          UUID usuarioCpcId,
                          String usuarioCpf,
                          String papel,
                          String payloadAntes,
                          String payloadDepois,
                          String ipOrigem) {
        Auditoria anterior = auditoriaRepository.findTopByOrderByDataHoraDesc().orElse(null);

        String hashAnterior = anterior != null ? anterior.getHashRegistro() : "GENESIS";

        String conteudo = String.join("|",
                acao, entidade, entidadeId != null ? entidadeId.toString() : "",
                descricao != null ? descricao : "",
                tenantId != null ? tenantId.toString() : "",
                usuarioCpcId != null ? usuarioCpcId.toString() : "",
                usuarioCpf != null ? usuarioCpf : "",
                OffsetDateTime.now().toString(),
                hashAnterior);

        Auditoria registro = Auditoria.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .usuarioCpcId(usuarioCpcId)
                .usuarioCpf(usuarioCpf)
                .papel(papel)
                .acao(acao)
                .entidade(entidade)
                .entidadeId(entidadeId != null ? entidadeId.toString() : null)
                .descricao(descricao)
                .payloadAntes(payloadAntes)
                .payloadDepois(payloadDepois)
                .ipOrigem(ipOrigem)
                .dataHora(OffsetDateTime.now())
                .hashAnterior(hashAnterior)
                .hashRegistro(sha256(conteudo))
                .build();

        auditoriaRepository.save(registro);
    }

    private String sha256(String entrada) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(entrada.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}