package br.com.jess.chronos.pulse.modules.compras.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

/**
 * Protocolo NFeDistDFeInt (Distribuição de DF-e): monta o envelope SOAP 1.2
 * de consulta por chave e extrai o documento fiscal da resposta.
 */
public final class NfeSefazProtocolo {

    private static final String NAMESPACE = "http://www.portalfiscal.inf.br/nfe";

    private NfeSefazProtocolo() {
    }

    public static String montarPedidoDistDfe(String chaveNfe, String cnpjDestinatario, boolean ambienteProducao) {
        String tpAmb = ambienteProducao ? "1" : "2";
        String cUf = chaveNfe.substring(0, 2);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soap12:Envelope xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
                + "<soap12:Body>"
                + "<nfeDistDFeInt xmlns=\"" + NAMESPACE + "\" versao=\"1.01\">"
                + "<tpAmb>" + tpAmb + "</tpAmb>"
                + "<cUFAutor>" + cUf + "</cUFAutor>"
                + "<CNPJ>" + cnpjDestinatario + "</CNPJ>"
                + "<consChNFe chNFe=\"" + chaveNfe + "\"/>"
                + "</nfeDistDFeInt>"
                + "</soap12:Body>"
                + "</soap12:Envelope>";
    }

    public static String extrairNfeDaResposta(String soapResponse) {
        try {
            Document documento = parsearDom(soapResponse);
            Element docZip = primeiroPorLocalName(documento.getDocumentElement(), "docZip");
            if (docZip != null) {
                return descompactarBase64Gzip(docZip.getTextContent().trim());
            }
            Element alvo = primeiroPorLocalName(documento.getDocumentElement(), "nfeProc");
            if (alvo == null) {
                alvo = primeiroPorLocalName(documento.getDocumentElement(), "NFe");
            }
            if (alvo == null) {
                throw new IllegalArgumentException(
                        "Resposta SEFAZ sem documento fiscal (NFe/nfeProc) — verificar a manifestação/distribuição");
            }
            return serializar(alvo);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Não foi possível interpretar a resposta da SEFAZ", e);
        }
    }

    // ============================ AUXILIARES ============================

    private static String descompactarBase64Gzip(String base64) throws Exception {
        byte[] bytes = Base64.getMimeDecoder().decode(base64);
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
             ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int lidos;
            while ((lidos = gzip.read(buffer)) > 0) {
                saida.write(buffer, 0, lidos);
            }
            return saida.toString(StandardCharsets.UTF_8);
        }
    }

    private static Document parsearDom(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static String serializar(Element elemento) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(elemento), new StreamResult(writer));
        return writer.toString();
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