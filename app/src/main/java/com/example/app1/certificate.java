package com.example.app1;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.security.auth.x500.X500Principal;

public class certificate {
    public X509Certificate getCertificateFromKeystore() {
        try {
            // 获取KeyStore实例
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null); // 加载KeyStore，此处没有提供密码

            // 获取所有证书
            Enumeration<String> aliases = keyStore.aliases();
            System.out.println(aliases);

            while (aliases.hasMoreElements()) {
                // 获取第一个证书
                String alias = aliases.nextElement();
                Certificate certificate = keyStore.getCertificate(alias);

                // 确保是X509证书
                if (certificate instanceof X509Certificate) {
                    return (X509Certificate) certificate;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCertificateSubject(X509Certificate certificate) {
        if (certificate != null) {
            X500Principal principal = certificate.getSubjectX500Principal();
            return principal.getName();
        }
        return null;
    }

}
