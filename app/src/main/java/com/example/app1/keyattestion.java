package com.example.app1;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;


public class keyattestion {
    static final String ASN1_OID = "1.3.6.1.4.1.11129.2.1.17";

    public String checkcertchain() {
        //-------------------------------生成证书链-------------------------------------------
        try {
            // 从 Android Keystore 中获取密钥
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            // 生成一个非对称密钥对（RSA）如果还没有的话
            if (!keyStore.containsAlias("ec_test_key")) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
                keyPairGenerator.initialize(
                        new KeyGenParameterSpec.Builder(
                                "ec_test_key",
                                KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                                .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                                .setDigests(KeyProperties.DIGEST_SHA256,
                                        KeyProperties.DIGEST_SHA384,
                                        KeyProperties.DIGEST_SHA512)
                                .setAttestationChallenge("hello world".getBytes(StandardCharsets.UTF_8))
                                .build());
            }
            //--------------------------------------获取证书---------------------------------------------------
            Certificate[] certs = keyStore.getCertificateChain("ec_test_key");//证书链
            X509Certificate x509Cert = (X509Certificate) certs[0];//获取含有1.3.6.1.4.1.11129.2.1.17extension的证书
            //--------------------------------------解析证书---------------------------------------------------
            byte[] attestationExtensionBytes = x509Cert.getExtensionValue(ASN1_OID);
            ASN1Sequence seq = getAsn1SequenceFromBytes(attestationExtensionBytes);
            //------------------KeyDescription-----------------------
            keydescription kd = new keydescription();
            String KeyDescription = kd.keyDescriptionResult(seq);
            //------------------AuthorizationList-----------------------
            keyauthorizationlist ka = new keyauthorizationlist();
            String AuthorizationList = ka.AuthorizationList(seq);
            return KeyDescription + "\n\n" + AuthorizationList;
        } catch (NoSuchAlgorithmException | CertificateException | IOException |
                 NoSuchProviderException |
                 InvalidAlgorithmParameterException |
                 java.security.KeyStoreException e) {
            Log.w("checkcertchainException", e.getMessage(),e);
        }
        return "解析失败";
    }
//---------------------------------------------------getdataFromAsn1------------------------------------------------
    public static ASN1Sequence getAsn1SequenceFromBytes(byte[] bytes)
            throws CertificateParsingException {
        try (ASN1InputStream asn1InputStream = new ASN1InputStream(bytes)) {
            return getAsn1SequenceFromStream(asn1InputStream);
        } catch (IOException e) {
            throw new CertificateParsingException("Failed to parse SEQUENCE", e);
        }
    }
    public static ASN1Sequence getAsn1SequenceFromStream(final ASN1InputStream asn1InputStream)
            throws IOException, CertificateParsingException {
        ASN1Primitive asn1Primitive = asn1InputStream.readObject();
        if (!(asn1Primitive instanceof ASN1OctetString)) {
            throw new CertificateParsingException(
                    "Expected octet stream, found " + asn1Primitive.getClass().getName());
        }
        try (ASN1InputStream seqInputStream = new ASN1InputStream(
                ((ASN1OctetString) asn1Primitive).getOctets())) {
            asn1Primitive = seqInputStream.readObject();
            if (!(asn1Primitive instanceof ASN1Sequence)) {
                throw new CertificateParsingException(
                        "Expected sequence, found " + asn1Primitive.getClass().getName());
            }
            return (ASN1Sequence) asn1Primitive;
        }
    }
}
