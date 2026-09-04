package br.com.jess.chronos.pulse.modules.empresa.domain.ports.input;

import br.com.jess.chronos.pulse.modules.empresa.domain.model.Empresa;
import java.util.List;

public interface ListarEmpresasUseCase {
    List<Empresa> executar();
}
