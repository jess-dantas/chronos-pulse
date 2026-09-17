package br.com.jess.chronos.pulse.modules.ponto.infrastructure.adapters.output.fiscal;

import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;

/**
 * Assinatura digital CAdES (CMS Advanced Electronic Signature) destacada
 * (formato {@code .p7s}) exigida para AFD e AEJ pelo art. 86/88 da Portaria
 * MTP n. 671/2021. O certificado (PKCS#12, ICP-Brasil) é fornecido pelas
 * variáveis de ambiente {@code FISCAL_PFX_BASE64} e {@code FISCAL_PFX_SENHA}.
 */
@Component
public class AssinadorCadesAdapter {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private final String pfxBase64;
    private final String pfxSenha;

    public AssinadorCadesAdapter(
            @Value("${chronos.fiscal.assinatura.pfx-base64:}") String pfxBase64,
            @Value("${chronos.fiscal.assinatura.pfx-senha:}") String pfxSenha) {
        this.pfxBase64 = pfxBase64;
        this.pfxSenha = pfxSenha;
    }

    public boolean assinaturaDisponivel() {
        return pfxBase64 != null && !pfxBase64.isBlank() && pfxSenha != null && !pfxSenha.isBlank();
    }

    /** Gera a assinatura CAdES destacada (DER) do conteúdo informado. */
    public byte[] assinarDetached(byte[] conteudo)
            throws GeneralSecurityException, IOException, OperatorCreationException, CMSException {
        if (!assinaturaDisponivel()) {
            throw new IllegalStateException(
                    "Assinatura digital não configurada: defina FISCAL_PFX_BASE64/FISCAL_PFX_SENHA.");
        }

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(Base64.getDecoder().decode(pfxBase64.trim())),
                pfxSenha.toCharArray());

        PrivateKey chave = null;
        List<X509Certificate> cadeia = new ArrayList<>();
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (!ks.isKeyEntry(alias)) {
                continue;
            }
            chave = (PrivateKey) ks.getKey(alias, pfxSenha.toCharArray());
            java.security.cert.Certificate[] chain = ks.getCertificateChain(alias);
            if (chain != null) {
                for (java.security.cert.Certificate c : chain) {
                    if (c instanceof X509Certificate x509) {
                        cadeia.add(x509);
                    }
                }
            }
            break;
        }
        if (chave == null || cadeia.isEmpty()) {
            throw new GeneralSecurityException("Nenhuma chave privada/certificado encontrado no PKCS#12.");
        }

        CMSSignedDataGenerator gerador = new CMSSignedDataGenerator();
        ContentSigner signer = new JcaContentSignerBuilder(algoritmo(chave))
                .setProvider("BC").build(chave);
        gerador.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build())
                .build(signer, new JcaX509CertificateHolder(cadeia.get(0))));
        gerador.addCertificates(new JcaCertStore(cadeia));

        CMSTypedData dados = new CMSProcessableByteArray(conteudo);
        CMSSignedData assinado = gerador.generate(dados, false); // destacado (detached)
        return assinado.getEncoded();
    }

    private static String algoritmo(PrivateKey chave) {
        String algorithm = chave.getAlgorithm();
        if (algorithm != null && algorithm.toUpperCase().contains("EC")) {
            return "SHA256withECDSA";
        }
        return "SHA256withRSA";
    }
}