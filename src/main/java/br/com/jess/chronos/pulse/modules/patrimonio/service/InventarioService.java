package br.com.jess.chronos.pulse.modules.patrimonio.service;

import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Inventario;
import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.InventarioItem;
import br.com.jess.chronos.pulse.modules.patrimonio.domain.entity.Patrimonio;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.InventarioRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.repository.PatrimonioRepository;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.ConferirItemDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.CriarInventarioDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.InventarioItemResponseDTO;
import br.com.jess.chronos.pulse.modules.patrimonio.web.dto.InventarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;
    private final PatrimonioRepository patrimonioRepository;

    @Transactional
    public InventarioResponseDTO criar(CriarInventarioDTO dto, UUID tenantId, String nomeUsuario) {
        LocalDate dataInicio = dto.dataInicio() != null ? dto.dataInicio() : LocalDate.now();
        Inventario inventario = Inventario.builder()
                .tenantId(tenantId)
                .descricao(dto.descricao())
                .dataInicio(dataInicio)
                .dataFim(dto.dataFim())
                .status("EM_ANDAMENTO")
                .criadoPor(nomeUsuario)
                .build();

        List<Patrimonio> ativos = patrimonioRepository.findAllByTenantIdAndAtivoTrue(tenantId);
        List<InventarioItem> itens = ativos.stream()
                .map(bem -> InventarioItem.builder()
                        .inventario(inventario)
                        .patrimonioId(bem.getId())
                        .patrimonioTombamento(bem.getTombamento())
                        .patrimonioDescricao(bem.getDescricao())
                        .conferido(false)
                        .build())
                .toList();
        inventario.setItens(itens);

        Inventario salvo = inventarioRepository.save(inventario);
        return toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<InventarioResponseDTO> listar(UUID tenantId) {
        return inventarioRepository.findAllByTenantIdOrderByCriadoEmDesc(tenantId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventarioResponseDTO buscarPorId(UUID id, UUID tenantId) {
        return inventarioRepository.findByIdAndTenantId(id, tenantId)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Inventário não encontrado"));
    }

    @Transactional
    public InventarioResponseDTO conferirItem(UUID id, ConferirItemDTO dto, UUID tenantId, String nomeConferente) {
        Inventario inventario = inventarioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Inventário não encontrado"));
        if (!"EM_ANDAMENTO".equals(inventario.getStatus())) {
            throw new IllegalStateException("Somente inventários em andamento podem ser conferidos");
        }

        InventarioItem item = inventario.getItens().stream()
                .filter(i -> i.getPatrimonioId().equals(dto.patrimonioId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item informado não pertence à sessão de inventário"));

        if (Boolean.TRUE.equals(item.getConferido())) {
            throw new IllegalStateException("Item já conferido nesta sessão");
        }

        String resultado = dto.resultado().toUpperCase();
        if (!"CONFORME".equals(resultado) && !"DIVERGENCIA".equals(resultado)) {
            throw new IllegalArgumentException("Resultado deve ser CONFORME ou DIVERGENCIA");
        }

        item.setConferido(true);
        item.setConferidoPor(nomeConferente);
        item.setDataConferencia(OffsetDateTime.now());
        item.setResultado(resultado);
        item.setObservacao(dto.observacao());

        return toResponseDTO(inventarioRepository.save(inventario));
    }

    @Transactional
    public InventarioResponseDTO finalizar(UUID id, UUID tenantId) {
        Inventario inventario = inventarioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Inventário não encontrado"));
        if (!"EM_ANDAMENTO".equals(inventario.getStatus())) {
            throw new IllegalStateException("Somente inventários em andamento podem ser concluídos");
        }

        inventario.getItens().stream()
                .filter(i -> !Boolean.TRUE.equals(i.getConferido()))
                .forEach(i -> {
                    i.setConferido(true);
                    i.setResultado("DIVERGENCIA");
                    i.setObservacao("Item não conferido durante a sessão");
                });

        inventario.setStatus("CONCLUIDO");
        inventario.setDataFim(LocalDate.now());
        return toResponseDTO(inventarioRepository.save(inventario));
    }

    @Transactional
    public InventarioResponseDTO cancelar(UUID id, UUID tenantId) {
        Inventario inventario = inventarioRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Inventário não encontrado"));
        if (!"EM_ANDAMENTO".equals(inventario.getStatus())) {
            throw new IllegalStateException("Somente inventários em andamento podem ser cancelados");
        }
        inventario.setStatus("CANCELADO");
        return toResponseDTO(inventarioRepository.save(inventario));
    }

    private InventarioResponseDTO toResponseDTO(Inventario inventario) {
        List<InventarioItemResponseDTO> itens = inventario.getItens().stream()
                .map(i -> new InventarioItemResponseDTO(
                        i.getId(), i.getPatrimonioId(), i.getPatrimonioTombamento(),
                        i.getPatrimonioDescricao(), Boolean.TRUE.equals(i.getConferido()),
                        i.getConferidoPor(), i.getDataConferencia(), i.getResultado(), i.getObservacao()))
                .toList();

        int totalConferidos = (int) itens.stream().filter(InventarioItemResponseDTO::conferido).count();
        int totalConformes = (int) itens.stream().filter(i -> "CONFORME".equals(i.resultado())).count();
        int totalDivergencias = (int) itens.stream().filter(i -> "DIVERGENCIA".equals(i.resultado())).count();

        return new InventarioResponseDTO(
                inventario.getId(), inventario.getDescricao(), inventario.getDataInicio(),
                inventario.getDataFim(), inventario.getStatus(), inventario.getCriadoPor(),
                inventario.getCriadoEm(), itens.size(), totalConferidos, totalConformes,
                totalDivergencias, itens);
    }
}