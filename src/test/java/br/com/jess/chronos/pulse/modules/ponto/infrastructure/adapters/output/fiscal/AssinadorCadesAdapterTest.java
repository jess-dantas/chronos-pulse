package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSSignerDigestMismatchException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssinadorCadesAdapterTest {

    private static final char[] SENHA = "senha123".toCharArray();
    private static String pfxBase64;

    @BeforeAll
    static void prepararCertificado() throws Exception {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        pfxBase64 = pfxComCertificadoSelfSigned();
    }

    @Test
    void deveIndicarIndisponibilidadeSemCertificadoConfigurado() {
        AssinadorCadesAdapter adaptador = new AssinadorCadesAdapter("", "");
        assertThat(adaptador.assinaturaDisponivel()).isFalse();
    }

    @Test
    void deveLancarAoAssinarSemCertificado() {
        AssinadorCadesAdapter adaptador = new AssinadorCadesAdapter("", "");
        assertThatThrownBy(() -> adaptador.assinarDetached("conteudo".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deveGerarAssinaturaCadesDestacadaValidaParaOConteudo() throws Exception {
        byte[] corpo = "AFD/LINHA1\r\nAFD/LINHA2\r\n".getBytes(StandardCharsets.ISO_8859_1);

        AssinadorCadesAdapter adaptador = new AssinadorCadesAdapter(pfxBase64, new String(SENHA));
        assertThat(adaptador.assinaturaDisponivel()).isTrue();
        byte[] p7s = adaptador.assinarDetached(corpo);

        assertThat(p7s).isNotEmpty();

        CMSSignedData assinaturaAvulsa = new CMSSignedData(p7s);
        assertThat(assinaturaAvulsa.getSignedContent()).isNull();

        CMSSignedData assinado = new CMSSignedData(new CMSProcessableByteArray(corpo), p7s);
        SignerInformation signer = assinado.getSignerInfos().getSigners().iterator().next();
        X509CertificateHolder holder = (X509CertificateHolder) assinado.getCertificates()
                .getMatches(signer.getSID()).iterator().next();
        X509Certificate certificado =
                new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
        assertThat(signer.verify(
                new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(certificado))).isTrue();
    }

    @Test
    void deveRejeitarVerificacaoComConteudoAlterado() throws Exception {
        byte[] corpo = "AEJ/LINHA1\r\n".getBytes(StandardCharsets.ISO_8859_1);

        AssinadorCadesAdapter adaptador = new AssinadorCadesAdapter(pfxBase64, new String(SENHA));
        byte[] p7s = adaptador.assinarDetached(corpo);

        byte[] adulterado = "AEJ/LINHA1\r\nAEJ/LINHA2\r\n".getBytes(StandardCharsets.ISO_8859_1);
        CMSSignedData assinado = new CMSSignedData(new CMSProcessableByteArray(adulterado), p7s);
        SignerInformation signer = assinado.getSignerInfos().getSigners().iterator().next();
        X509CertificateHolder holder = (X509CertificateHolder) assinado.getCertificates()
                .getMatches(signer.getSID()).iterator().next();
        X509Certificate certificado =
                new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
        assertThatThrownBy(() -> signer.verify(
                new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(certificado)))
                .isInstanceOf(CMSSignerDigestMismatchException.class);
    }

    private static String pfxComCertificadoSelfSigned() throws Exception {
        KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
        gerador.initialize(2048);
        KeyPair par = gerador.generateKeyPair();

        X500Name nome = new X500Name("CN=Chronos Pulse Teste, O=Jess Teste");
        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC").build(par.getPrivate());
        X509Certificate certificado = new JcaX509CertificateConverter().setProvider("BC")
                .getCertificate(new JcaX509v3CertificateBuilder(
                        nome, BigInteger.ONE,
                        Date.from(Instant.now()),
                        Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
                        nome, par.getPublic()).build(signer));

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry("teste", par.getPrivate(), SENHA, new java.security.cert.Certificate[]{certificado});

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ks.store(bytes, SENHA);
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }
}