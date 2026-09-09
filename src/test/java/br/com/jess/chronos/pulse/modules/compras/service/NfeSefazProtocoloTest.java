package br.com.jess.chronos.pulse.modules.compras.service;

import br.com.jess.chronos.pulse.modules.compras.web.dto.NfeImportadoDTO;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NfeSefazProtocoloTest {

    private static final String CHAVE = "35250922334455000190550010000000011000000087";

    @Test
    void montaEnvelopeDistDfeEmProducao() {
        String envelope = NfeSefazProtocolo.montarPedidoDistDfe(CHAVE, "49262262000113", true);
        assertTrue(envelope.contains("soap12:Envelope"));
        assertTrue(envelope.contains("<tpAmb>1</tpAmb>"));
        assertTrue(envelope.contains("<cUFAutor>35</cUFAutor>"));
        assertTrue(envelope.contains("<CNPJ>49262262000113</CNPJ>"));
        assertTrue(envelope.contains("chNFe=\"" + CHAVE + "\""));
    }

    @Test
    void usaAmbienteHomologacaoQuandoConfigurado() {
        String envelope = NfeSefazProtocolo.montarPedidoDistDfe(CHAVE, "49262262000113", false);
        assertTrue(envelope.contains("<tpAmb>2</tpAmb>"));
    }

    @Test
    void extraiNfeDiretamenteDaResposta() {
        String resposta = soapEnvelope(nfeProcXml(CHAVE));
        String xmlNfe = NfeSefazProtocolo.extrairNfeDaResposta(resposta);
        assertTrue(xmlNfe.contains("infNFe"));

        NfeImportadoDTO dto = NfeXmlParser.parsear(xmlNfe);
        assertEquals(CHAVE, dto.chaveNfe());
        assertEquals("49262262000113", dto.cnpjEmitente());
    }

    @Test
    void extraiNfeDeDocZipGzipBase64() throws Exception {
        String nfeProc = nfeProcXml(CHAVE);
        String docZip = Base64.getEncoder().encodeToString(gzip(nfeProc));
        String resposta = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "<soap:Body><nfeDistDFeIntResult xmlns=\"http://www.portalfiscal.inf.br/nfe\">"
                + "<retNFeDistDFeInt versao=\"1.01\"><cStat>138</cStat>"
                + "<xMotivo>Documento localizado</xMotivo>"
                + "<docZip>" + docZip + "</docZip>"
                + "</retNFeDistDFeInt></nfeDistDFeIntResult></soap:Body></soap:Envelope>";

        String xmlNfe = NfeSefazProtocolo.extrairNfeDaResposta(resposta);
        assertTrue(xmlNfe.contains("infNFe"));
        assertEquals(CHAVE, NfeXmlParser.parsear(xmlNfe).chaveNfe());
    }

    @Test
    void rejeitaRespostaSemDocumentoFiscal() {
        String resposta = soapEnvelope("<retorno><cStat>137</cStat><xMotivo>Nenhum documento</xMotivo></retorno>");
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> NfeSefazProtocolo.extrairNfeDaResposta(resposta));
        assertFalse(ex.getMessage().isBlank());
    }

    private String soapEnvelope(String corpo) {
        return "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">"
                + "<soap:Body>" + corpo + "</soap:Body></soap:Envelope>";
    }

    private String nfeProcXml(String chave) {
        return "<nfeProc versao=\"4.00\" xmlns=\"http://www.portalfiscal.inf.br/nfe\">"
                + "<NFe><infNFe Id=\"NFe" + chave + "\" versao=\"4.00\">"
                + "<ide><cUF>35</cUF><cNF>00000008</cNF><natOp>VENDA</natOp><mod>55</mod>"
                + "<serie>001</serie><nNF>100</nNF><dhEmi>2025-09-12T14:30:00-03:00</dhEmi>"
                + "<tpNF>1</tpNF><idDest>1</idDest><cMunFG>3550308</cMunFG></ide>"
                + "<emit><CNPJ>49262262000113</CNPJ><xNome>TESTE SEFAZ</xNome></emit>"
                + "<det nItem=\"1\"><prod><cProd>0001</cProd><xProd>ITEM TESTE</xProd>"
                + "<NCM>96091000</NCM><CFOP>5102</CFOP><uCom>CX</uCom><qCom>2.0000</qCom>"
                + "<vUnCom>10.0000</vUnCom><vProd>20.00</vProd></prod></det>"
                + "<total><vNF>20.00</vNF></total>"
                + "</infNFe></NFe></nfeProc>";
    }

    private byte[] gzip(String conteudo) throws Exception {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(saida)) {
            gzip.write(conteudo.getBytes(StandardCharsets.UTF_8));
        }
        return saida.toByteArray();
    }
}