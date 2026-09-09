package br.com.jess.chronos.pulse.modules.compras.service;

import br.com.jess.chronos.pulse.modules.compras.web.dto.NfeImportadoDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.NfeImportadoItemDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NfeXmlParserTest {

    private static final String CHAVE_VALIDA = "35250922334455000190550010000000011000000087";

    @Test
    void parseiaNfeValida() {
        NfeImportadoDTO dto = NfeXmlParser.parsear(nfeXml(CHAVE_VALIDA));

        assertEquals(CHAVE_VALIDA, dto.chaveNfe());
        assertEquals("987", dto.numero());
        assertEquals("001", dto.serie());
        assertEquals(LocalDate.of(2025, 9, 12), dto.dataEmissao());
        assertEquals(new BigDecimal("250.50"), dto.valorNota().setScale(2));
        assertEquals("22334455000190", dto.cnpjEmitente());
        assertEquals("FORNECEDOR DEMONSTRACAO LTDA", dto.razaoEmitente());
        assertEquals("XML", dto.origem());
        assertEquals(2, dto.itens().size());

        NfeImportadoItemDTO primeiro = dto.itens().get(0);
        assertEquals(1, primeiro.numeroItem());
        assertEquals("0001", primeiro.codigoProduto());
        assertEquals("LAPIS PRETO", primeiro.descricao());
        assertEquals("96091000", primeiro.ncm());
        assertEquals("5102", primeiro.cfop());
        assertEquals("CX", primeiro.unidadeComercial());
        assertEquals(new BigDecimal("10.0000"), primeiro.quantidadeComercial());
        assertEquals(new BigDecimal("12.5000"), primeiro.valorUnitarioComercial());
        assertEquals(new BigDecimal("125.00"), primeiro.valorTotalProduto());
    }

    @Test
    void aceitaXmlSemValorTotal() {
        NfeImportadoDTO dto = NfeXmlParser.parsear(nfeXmlSemTotal(CHAVE_VALIDA));
        assertNull(dto.valorNota());
    }

    @Test
    void rejeitaDvInvalidoDaChave() {
        String chaveCorrompida = CHAVE_VALIDA.substring(0, 43) + "6";
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> NfeXmlParser.parsear(nfeXml(chaveCorrompida)));
        assertTrue(ex.getMessage().contains("verificador"));
    }

    @Test
    void rejeitaXmlSemItens() {
        String xml = nfeXml(CHAVE_VALIDA);
        String semItens = xml.substring(0, xml.indexOf("<det"))
                + xml.substring(xml.lastIndexOf("</det>") + "</det>".length());
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> NfeXmlParser.parsear(semItens));
        assertTrue(ex.getMessage().contains("sem itens"));
    }

    @Test
    void rejeitaXmlSemEmitente() {
        String xml = nfeXml(CHAVE_VALIDA);
        String semEmitente = xml.substring(0, xml.indexOf("<emit"))
                + xml.substring(xml.lastIndexOf("</emit>") + "</emit>".length());
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> NfeXmlParser.parsear(semEmitente));
        assertTrue(ex.getMessage().contains("emitente"));
    }

    @Test
    void rejeitaXmlMalformado() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> NfeXmlParser.parsear("isto não é um xml"));
        assertTrue(ex.getMessage().contains("inválido"));
    }

    @Test
    void rejeitaXmlVazio() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> NfeXmlParser.parsear("   "));
        assertTrue(ex.getMessage().contains("vazio"));
    }

    private String nfeXml(String chave) {
        return "<nfeProc versao=\"4.00\" xmlns=\"http://www.portalfiscal.inf.br/nfe\">"
                + "<NFe><infNFe Id=\"NFe" + chave + "\" versao=\"4.00\">"
                + "<ide><cUF>35</cUF><cNF>00000008</cNF><natOp>VENDA</natOp><mod>55</mod>"
                + "<serie>001</serie><nNF>987</nNF><dhEmi>2025-09-12T14:30:00-03:00</dhEmi>"
                + "<tpNF>1</tpNF><idDest>1</idDest><cMunFG>3550308</cMunFG></ide>"
                + "<emit><CNPJ>22334455000190</CNPJ><xNome>FORNECEDOR DEMONSTRACAO LTDA</xNome></emit>"
                + "<dest><CNPJ>49262262000113</CNPJ><xNome>LJ CHRONOS PULSE TECNOLOGIA E SISTEMAS LTDA</xNome></dest>"
                + "<det nItem=\"1\"><prod><cProd>0001</cProd><cEAN>SEM GTIN</cEAN><xProd>LAPIS PRETO</xProd>"
                + "<NCM>96091000</NCM><CFOP>5102</CFOP><uCom>CX</uCom><qCom>10.0000</qCom>"
                + "<vUnCom>12.5000</vUnCom><vProd>125.00</vProd></prod></det>"
                + "<det nItem=\"2\"><prod><cProd>0002</cProd><cEAN>SEM GTIN</cEAN><xProd>CADERNO UNIVERSITARIO</xProd>"
                + "<NCM>48201000</NCM><CFOP>5102</CFOP><uCom>UN</uCom><qCom>5.0000</qCom>"
                + "<vUnCom>25.1000</vUnCom><vProd>125.50</vProd></prod></det>"
                + "<total><vProd>250.50</vProd><vNF>250.50</vNF></total>"
                + "</infNFe></NFe></nfeProc>";
    }

    private String nfeXmlSemTotal(String chave) {
        return nfeXml(chave)
                .replace("<total><vProd>250.50</vProd><vNF>250.50</vNF></total>", "");
    }
}