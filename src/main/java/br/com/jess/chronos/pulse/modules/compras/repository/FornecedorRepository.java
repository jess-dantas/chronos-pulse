package br.com.jess.chronos.pulse.modules.compras.repository;

import br.com.jess.chronos.pulse.modules.compras.domain.entity.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, UUID> {
    Optional<Fornecedor> findByIdAndTenantId(UUID id, UUID tenantId);
    List<Fornecedor> findAllByTenantIdOrderByRazaoSocial(UUID tenantId);
    List<Fornecedor> findAllByTenantIdAndAtivoTrueOrderByRazaoSocial(UUID tenantId);
    Optional<Fornecedor> findByTenantIdAndCnpj(UUID tenantId, String cnpj);
}