package br.com.jess.chronos.pulse.modules.compras.service;

import br.com.jess.chronos.pulse.modules.compras.web.dto.NfeImportadoDTO;
import br.com.jess.chronos.pulse.modules.compras.web.dto.NfeImportadoItemDTO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse estrutural do XML da NFe (nfeProc/NFe, padrão 4.00).
 * Valida a chave de acesso (44 dígitos + DV módulo 11) extraída do id do
 * {@code infNFe} e confere coerentemente os dados mínimos do documento.
 */
public final class NfeXmlParser {

    private NfeXmlParser() {
    }

    public static NfeImportadoDTO parsear(String conteudoXml) {
        String xml = conteudoXml == null ? "" : conteudoXml.trim();
        if (xml.isEmpty()) {
            throw new IllegalArgumentException("XML da NFe vazio");
        }

        Document documento = parsearDom(xml);

        Element infNFe = primeiroPorLocalName(documento.getDocumentElement(), "infNFe");
        if (infNFe == null) {
            throw new IllegalArgumentException("Documento não é uma NFe (elemento infNFe não encontrado)");
        }

        String id = infNFe.getAttribute("Id");
        if (id.isBlank()) {
            id = infNFe.getAttribute("id");
        }
        String chave = extrairChave(id);

        Element ide = primeiroFilhoPorLocalName(infNFe, "ide");
        Element emit = primeiroFilhoPorLocalName(infNFe, "emit");
        Element total = primeiroFilhoPorLocalName(infNFe, "total");

        if (emit == null || textoFilho(emit, "CNPJ") == null) {
            throw new IllegalArgumentException("NFe sem identificação do emitente (emit/CNPJ)");
        }

        List<NfeImportadoItemDTO> itens = new ArrayList<>();
        NodeList filhos = infNFe.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Node node = filhos.item(i);
            if (!(node instanceof Element det) || !"det".equals(det.getLocalName())) {
                continue;
            }
            Element prod = primeiroFilhoPorLocalName(det, "prod");
            if (prod == null) {
                continue;
            }
            itens.add(new NfeImportadoItemDTO(
                    numeroInteiro(det.getAttribute("nItem")),
                    textoFilho(prod, "cProd"),
                    textoFilho(prod, "xProd"),
                    textoFilho(prod, "NCM"),
                    textoFilho(prod, "CFOP"),
                    textoFilho(prod, "uCom"),
                    decimalFilho(prod, "qCom"),
                    decimalFilho(prod, "vUnCom"),
                    decimalFilho(prod, "vProd")));
        }
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("NFe sem itens (det/prod)");
        }

        return new NfeImportadoDTO(
                chave,
                textoFilho(ide, "nNF"),
                textoFilho(ide, "serie"),
                dataEmissao(ide),
                decimalFilho(total, "vNF"),
                textoFilho(emit, "CNPJ"),
                textoFilho(emit, "xNome"),
                "XML",
                itens);
    }

    // ============================ AUXILIARES ============================

    private static Document parsearDom(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalArgumentException("XML da NFe inválido ou malformado", e);
        }
    }

    private static String extrairChave(String id) {
        String valor = id == null ? "" : id.trim();
        if (valor.toUpperCase().startsWith("NFE")) {
            valor = valor.substring(3);
        }
        String chave = valor.replaceAll("[^0-9]", "");
        if (chave.length() != 44) {
            throw new IllegalArgumentException("Chave NFe ausente ou inválida no XML (infNFe/@Id)");
        }
        if (!ComprasService.validarChaveNfe(chave)) {
            throw new IllegalArgumentException("Dígito verificador da chave NFe do XML inválido");
        }
        return chave;
    }

    private static LocalDate dataEmissao(Element ide) {
        String dhEmi = textoFilho(ide, "dhEmi");
        if (dhEmi != null) {
            return parsearData(dhEmi.length() >= 10 ? dhEmi.substring(0, 10) : dhEmi);
        }
        return parsearData(textoFilho(ide, "dEmi"));
    }

    private static LocalDate parsearData(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static BigDecimal decimalFilho(Element pai, String localName) {
        if (pai == null) {
            return null;
        }
        String texto = textoFilho(pai, localName);
        if (texto == null) {
            return null;
        }
        try {
            return new BigDecimal(texto.replace(',', '.').trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer numeroInteiro(String valor) {
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String textoFilho(Element pai, String localName) {
        Element filho = primeiroFilhoPorLocalName(pai, localName);
        if (filho == null) {
            return null;
        }
        String texto = filho.getTextContent();
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private static Element primeiroFilhoPorLocalName(Element pai, String localName) {
        NodeList filhos = pai.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Node node = filhos.item(i);
            if (node instanceof Element el && localName.equals(el.getLocalName())) {
                return el;
            }
        }
        return null;
    }

    private static Element primeiroPorLocalName(Node origem, String localName) {
        NodeList filhos = origem.getChildNodes();
        for (int i = 0; i < filhos.getLength(); i++) {
            Node node = filhos.item(i);
            if (node instanceof Element el) {
                if (localName.equals(el.getLocalName())) {
                    return el;
                }
                Element encontrado = primeiroPorLocalName(el, localName);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }
}