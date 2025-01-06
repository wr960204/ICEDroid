package com.example.app1;

import android.content.Context;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class certificate {
    public void listInstalledCertificates(Context context) {
        try {
            // 获取Android系统的KeyStore
            KeyStore keyStore = KeyStore.getInstance("AndroidCAStore");
            keyStore.load(null, null); // 加载密钥库

            // 遍历所有证书别名
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate certificate = keyStore.getCertificate(alias);
                if (certificate instanceof X509Certificate) {
                    X509Certificate x509c = (X509Certificate) certificate;

                    // 提取证书信息
                    String subjectDN = x509c.getSubjectDN().getName();  // 使用者
                    String issuerDN = x509c.getIssuerDN().getName();    // 颁发者
                    String validity = x509c.getNotBefore() + " - " + x509c.getNotAfter(); // 有效期

                    // 打印所需的信息
                    System.out.println("Alias: " + alias);
                    System.out.println("Subject: " + subjectDN);
                    System.out.println("Issuer: " + issuerDN);
                    System.out.println("Validity: " + validity);
                    System.out.println("---------------------------------");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
