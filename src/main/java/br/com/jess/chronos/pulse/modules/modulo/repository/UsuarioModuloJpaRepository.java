package br.com.jess.chronos.pulse.modules.modulo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface UsuarioModuloJpaRepository extends JpaRepository<UsuarioModuloJpaEntity, UUID> {
    List<UsuarioModuloJpaEntity> findByUsuarioIdAndTenantId(UUID usuarioId, UUID tenantId);

    @Modifying
    @Transactional
    void deleteByUsuarioIdAndTenantId(UUID usuarioId, UUID tenantId);

    /// Remove TODAS as linhas do usuário, em qualquer tenant.
    ///
    /// O filtro por tenant_id deixava escapar linhas com tenant_id divergente ou
    /// NULL (coluna anulável): o insert seguinte batia em uk_usuario_modulo
    /// (duplicate key) e derrubava a transação inteira do provisionamento.
    @Modifying
    @Transactional
    void deleteByUsuarioId(UUID usuarioId);

    /// Insert idempotente: se (usuario_id, codigo) já existe, atualiza o tenant.
    /// Garante que uma repetição (retry, reprovisionamento) nunca falhe por
    /// duplicate key.
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO usuario_modulo (id, tenant_id, usuario_id, codigo)
            VALUES (:id, :tenantId, :usuarioId, :codigo)
            ON CONFLICT (usuario_id, codigo)
            DO UPDATE SET tenant_id = EXCLUDED.tenant_id
            """, nativeQuery = true)
    void upsertModulo(UUID id, UUID tenantId, UUID usuarioId, String codigo);
}
