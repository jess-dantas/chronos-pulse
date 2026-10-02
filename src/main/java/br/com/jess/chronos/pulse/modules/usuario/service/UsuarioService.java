package br.com.jess.chronos.pulse.modules.usuario.service;

import br.com.jess.chronos.pulse.modules.auditoria.service.AuditoriaService;
import br.com.jess.chronos.pulse.modules.auth.domain.model.CpcUsuario;
import br.com.jess.chronos.pulse.modules.auth.domain.model.Role;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.CpcUsuarioRepositoryPort;
import br.com.jess.chronos.pulse.modules.auth.domain.service.PasswordPolicy;
import br.com.jess.chronos.pulse.modules.modulo.domain.ports.output.ModulosPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/// Gestão de contas administrativas da empresa (papéis GESTOR_RH e
/// ADMIN_EMPRESA): listagem, criação e suspensão. Colaboradores ficam na
/// gestão de colaboradores (módulo RECURSOS_HUMANOS); aqui não se cria
/// nem se suspende conta COLABORADOR.
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final CpcUsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModulosPort modulosPort;
    private final AuditoriaService auditoriaService;

    /// Papéis que a administração da empresa pode criar/suspender.
    public static final List<Role> PAPEIS_GERENCIAVEIS = List.of(Role.GESTOR_RH, Role.ADMIN_EMPRESA);

    public record UsuarioItem(UUID id, String cpf, String nome, String email,
                              String role, boolean ativo, Instant criadoEm) {}

    public record CriarUsuario(String cpf, String nome, String emailCorporativo,
                               String senha, String papel) {}

    public List<UsuarioItem> listar(UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Operação disponível apenas para usuários de uma empresa.");
        }
        return usuarioRepository.listarPorTenant(tenantId).stream()
                .filter(usuario -> PAPEIS_GERENCIAVEIS.contains(usuario.getRole()))
                .sorted(Comparator.comparing(CpcUsuario::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(this::paraItem)
                .toList();
    }

    public UsuarioItem criar(CriarUsuario comando, UUID tenantId, CpcUsuario operador) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Operação disponível apenas para usuários de uma empresa.");
        }
        if (comando.cpf() == null || comando.cpf().isBlank()) {
            throw new IllegalArgumentException("CPF é obrigatório.");
        }
        if (comando.nome() == null || comando.nome().isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório.");
        }
        Role papel = papelGerenciavel(comando.papel());
        if (usuarioRepository.existePorCpf(comando.cpf())) {
            throw new IllegalArgumentException("CPF já cadastrado: " + comando.cpf());
        }
        PasswordPolicy.validar(comando.senha(), papel)
                .ifPresent(mensagem -> {
                    throw new IllegalArgumentException(mensagem);
                });

        CpcUsuario usuario = new CpcUsuario(
                null, null, comando.cpf(), comando.nome().trim(), comando.emailCorporativo(),
                passwordEncoder.encode(comando.senha()), papel, tenantId);
        usuario = usuarioRepository.salvar(usuario);

        if (papel == Role.GESTOR_RH) {
            // Escopo fixo do papel (mesma normalização da V005): PONTO + RH.
            modulosPort.definirModulosDoUsuario(usuario.getId(), tenantId,
                    List.of("PONTO", "RECURSOS_HUMANOS"));
        }

        auditoriaService.registrar("CRIACAO", "USUARIO", usuario.getId(),
                "Criação de usuário " + papel + " " + comando.cpf(),
                tenantId, operador.getCpcId(), operador.getCpf(),
                operador.getRole().name(), null,
                "cpf=" + comando.cpf() + "&papel=" + papel, null);
        return paraItem(usuario);
    }

    public UsuarioItem suspender(UUID usuarioId, UUID tenantId, CpcUsuario operador) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Operação disponível apenas para usuários de uma empresa.");
        }
        CpcUsuario alvo = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        if (!tenantId.equals(alvo.getTenantId())) {
            throw new IllegalArgumentException("Usuário não pertence a este tenant.");
        }
        if (alvo.getRole() == Role.ADMIN_PLATAFORMA) {
            throw new IllegalArgumentException("Não é possível suspender um usuário da plataforma.");
        }
        if (alvo.getRole() == Role.COLABORADOR) {
            throw new IllegalArgumentException(
                    "Contas COLABORADOR são suspensas pela exclusão na gestão de colaboradores.");
        }
        if (alvo.getId().equals(operador.getId())) {
            throw new IllegalArgumentException("Não é possível suspender o próprio usuário.");
        }
        if (!alvo.isAtivo()) {
            throw new IllegalArgumentException("O usuário já está suspenso.");
        }

        CpcUsuario suspenso = usuarioRepository.atualizar(alvo.comAtivo(false));
        auditoriaService.registrar("SUSPENSAO", "USUARIO", alvo.getId(),
                "Suspensão de usuário " + alvo.getRole() + " " + alvo.getCpf(),
                tenantId, operador.getCpcId(), operador.getCpf(),
                operador.getRole().name(), null,
                "cpf=" + alvo.getCpf() + "&ativo=false", null);
        return paraItem(suspenso);
    }

    private Role papelGerenciavel(String papel) {
        if (papel == null || papel.isBlank()) {
            throw new IllegalArgumentException("Papel é obrigatório.");
        }
        Role papelEnum;
        try {
            papelEnum = Role.valueOf(papel.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Papel inválido: " + papel);
        }
        if (!PAPEIS_GERENCIAVEIS.contains(papelEnum)) {
            throw new IllegalArgumentException(
                    "Só é possível criar usuários com papel GESTOR_RH ou ADMIN_EMPRESA.");
        }
        return papelEnum;
    }

    private UsuarioItem paraItem(CpcUsuario usuario) {
        return new UsuarioItem(usuario.getId(), usuario.getCpf(), usuario.getNome(),
                usuario.getEmailCorporativo() != null ? usuario.getEmailCorporativo() : usuario.getEmailPessoal(),
                usuario.getRole().name(), usuario.isAtivo(), usuario.getCriadoEm());
    }
}
