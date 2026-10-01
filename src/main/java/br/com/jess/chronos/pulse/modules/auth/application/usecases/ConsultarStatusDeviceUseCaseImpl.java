package br.com.jess.chronos.pulse.modules.auth.application.usecases;

import br.com.jess.chronos.pulse.modules.auth.application.service.DeviceTokenService;
import br.com.jess.chronos.pulse.modules.auth.domain.ports.input.ConsultarStatusDeviceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConsultarStatusDeviceUseCaseImpl implements ConsultarStatusDeviceUseCase {

    private final DeviceTokenService deviceTokenService;

    @Override
    public Optional<StatusResultado> executar(String deviceTokenBruto) {
        return deviceTokenService.autenticarComVinculo(deviceTokenBruto)
                .map(vinculo -> new StatusResultado(
                        vinculo.usuario().getCpcId(),
                        vinculo.usuario().getNome(),
                        vinculo.usuario().isTwoFactorEnabled(),
                        true,
                        vinculo.expiraEm()));
    }
}
