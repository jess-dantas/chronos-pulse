package br.com.jess.chronos.pulse.modules.compras.service;

import br.com.jess.chronos.pulse.modules.compras.web.dto.NfeImportadoDTO;
import org.springframework.stereotype.Service;

@Service
public class NfeImportacaoService {

    public NfeImportadoDTO importarXml(String conteudoXml) {
        return NfeXmlParser.parsear(conteudoXml);
    }
}