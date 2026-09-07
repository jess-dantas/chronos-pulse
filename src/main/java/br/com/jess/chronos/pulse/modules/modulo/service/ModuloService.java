package br.com.jess.chronos.pulse.modules.modulo.service;

import br.com.jess.chronos.pulse.modules.modulo.domain.entity.EmpresaModulo;
import br.com.jess.chronos.pulse.modules.modulo.domain.entity.ModuloPlataforma;
import br.com.jess.chronos.pulse.modules.modulo.repository.EmpresaModuloRepository;
import br.com.jess.chronos.pulse.modules.modulo.repository.ModuloPlataformaRepository;
import br.com.jess.chronos.pulse.modules.modulo.web.dto.ModuloResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ModuloService {

    private static final List<String> MODULOS_PADRAO_NOVA_EMPRESA = List.of("PONTO", "RECURSOS_HUMANOS");

    private final ModuloPlataformaRepository moduloRepository;
    private final EmpresaModuloRepository empresaModuloRepository;

    public List<ModuloResponseDTO> listarCatalogo() {
        return moduloRepository.findByAtivoTrueOrderByNomeAsc().stream()
                .map(ModuloService::toDTO)
                .toList();
    }

    public List<String> listarCodigosAtivos(UUID tenantId) {
        if (tenantId == null) {
            return Collections.emptyList();
        }
        return empresaModuloRepository.findCodigosAtivos(tenantId);
    }

    public boolean isAtivo(UUID tenantId, String codigo) {
        if (tenantId == null) {
            return false;
        }
        return empresaModuloRepository.isTenantModuloAtivo(tenantId, codigo);
    }

    @Transactional
    public List<String> atualizarModulos(UUID tenantId, List<String> codigos) {
        Set<String> unicos = new LinkedHashSet<>();
        for (String codigo : codigos) {
            if (codigo != null && !codigo.isBlank()) {
                unicos.add(codigo.trim().toUpperCase(Locale.ROOT));
            }
        }

        List<ModuloPlataforma> modulos = new ArrayList<>();
        for (String codigo : unicos) {
            ModuloPlataforma modulo = moduloRepository.findByCodigo(codigo)
                    .orElseThrow(() -> new IllegalArgumentException("Módulo inexistente: " + codigo));
            if (!Boolean.TRUE.equals(modulo.getAtivo())) {
                throw new IllegalArgumentException("Módulo não disponível para contratação: " + codigo);
            }
            modulos.add(modulo);
        }

        empresaModuloRepository.deleteByTenantId(tenantId);
        Instant agora = Instant.now();
        modulos.forEach(modulo -> empresaModuloRepository.save(EmpresaModulo.builder()
                .tenantId(tenantId)
                .modulo(modulo)
                .ativadoEm(agora)
                .build()));

        return new ArrayList<>(unicos);
    }

    @Transactional
    public void ativarModulosPadrao(UUID tenantId) {
        if (tenantId == null) {
            return;
        }
        Instant agora = Instant.now();
        for (String codigo : MODULOS_PADRAO_NOVA_EMPRESA) {
            Optional<ModuloPlataforma> moduloOpt = moduloRepository.findByCodigo(codigo);
            if (moduloOpt.isEmpty() || !Boolean.TRUE.equals(moduloOpt.get().getAtivo())) {
                continue;
            }
            ModuloPlataforma modulo = moduloOpt.get();
            if (!empresaModuloRepository.existsByTenantIdAndModulo_Id(tenantId, modulo.getId())) {
                empresaModuloRepository.save(EmpresaModulo.builder()
                        .tenantId(tenantId)
                        .modulo(modulo)
                        .ativadoEm(agora)
                        .build());
            }
        }
    }

    private static ModuloResponseDTO toDTO(ModuloPlataforma modulo) {
        return new ModuloResponseDTO(
                modulo.getCodigo(),
                modulo.getNome(),
                modulo.getDescricao(),
                Boolean.TRUE.equals(modulo.getAtivo()));
    }
}