package br.com.jess.chronos.pulse.modules.auth.infrastructure.adapters.output.persistence;

import br.com.jess.chronos.pulse.modules.auth.domain.model.RecuperacaoSenha;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.output.RecuperacaoSenhaRepositoryPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class RecuperacaoSenhaRepositoryAdapter implements RecuperacaoSenhaRepositoryPort {

    private final RecuperacaoSenhaJpaRepository jpaRepository;
    private final RecuperacaoSenhaMapper mapper;

    public RecuperacaoSenhaRepositoryAdapter(RecuperacaoSenhaJpaRepository jpaRepository,
                                             RecuperacaoSenhaMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public RecuperacaoSenha salvar(RecuperacaoSenha recuperacaoSenha) {
        return mapper.toModel(jpaRepository.save(mapper.toEntity(recuperacaoSenha)));
    }

    @Override
    public RecuperacaoSenha atualizar(RecuperacaoSenha recuperacaoSenha) {
        return mapper.toModel(jpaRepository.save(mapper.toEntity(recuperacaoSenha)));
    }

    @Override
    public Optional<RecuperacaoSenha> buscarUltimaPorCpf(String cpf) {
        return jpaRepository.findFirstByCpfOrderByCriadoEmDesc(cpf).map(mapper::toModel);
    }

    @Override
    @Transactional
    public void marcarTodasComoUsadas(String cpf) {
        jpaRepository.findByCpf(cpf).forEach(e -> {
            e.setUsado(true);
            jpaRepository.save(e);
        });
    }
}