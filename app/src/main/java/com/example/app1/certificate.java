package com.example.app1;

import android.util.Log;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class certificate {
    public String listInstalledCertificates() {
        StringBuilder cert = new StringBuilder("证书信息\n---------------------------------\n");
        try {
            // 获取Android系统的KeyStore
            KeyStore keyStore = KeyStore.getInstance("AndroidCAStore");
            keyStore.load(null, null); // 加载密钥库

            // 遍历所有证书别名
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate certificate = keyStore.getCertificate(alias);
                if (certificate instanceof X509Certificate x509c) {
                    // 提取证书信息
                    String subjectDN = x509c.getSubjectDN().getName();  // 使用者
                    String issuerDN = x509c.getIssuerDN().getName();    // 颁发者
                    String validity = x509c.getNotBefore() + " - " + x509c.getNotAfter(); // 有效期

                    cert.append("Alias:").append(alias).append("\n");
                    cert.append("Subject:").append(subjectDN).append("\n");
                    cert.append("Issuer:").append(issuerDN).append("\n");
                    cert.append("Validity:").append(validity).append("\n");
                    cert.append("---------------------------------").append("\n");
                }
            }
        } catch (Exception e) {
            Log.w("checkcertException", e.getMessage(),e);
        }
        return cert.toString();
    }

    public void listInstalledCertificates1() {
        try {
            // 获取Android系统的KeyStore
            KeyStore keyStore = KeyStore.getInstance("AndroidCAStore");
            keyStore.load(null, null); // 加载密钥库

            // 存储所有证书
            List<X509Certificate> certificates = new ArrayList<>();

            // 遍历所有证书别名并存储X509证书
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate certificate = keyStore.getCertificate(alias);
                if (certificate instanceof X509Certificate) {
                    certificates.add((X509Certificate) certificate);
                }
            }

            // 打印证书链
            for (X509Certificate cert : certificates) {
                System.out.println("Certificate: " + cert.getSubjectDN().getName());
                printCertificateChain(cert, certificates);
                System.out.println("---------------------------------");
            }
        } catch (Exception e) {
            Log.w("checkcertException", e.getMessage(),e);
        }
    }

    private static void printCertificateChain(X509Certificate cert, List<X509Certificate> allCertificates) {
        List<X509Certificate> chain = new ArrayList<>();
        X509Certificate currentCert = cert;

        // 构建证书链
        while (currentCert != null) {
            chain.add(currentCert);
            currentCert = findIssuer(currentCert, allCertificates);
        }

        // 输出证书链
        for (X509Certificate c : chain) {
            System.out.println(" - " + c.getSubjectDN().getName() + " (Issuer: " + c.getIssuerDN().getName() + ")");
        }
    }

    private static X509Certificate findIssuer(X509Certificate cert, List<X509Certificate> allCertificates) {
        String issuerDN = cert.getIssuerDN().getName();
        for (X509Certificate c : allCertificates) {
            if (c.getSubjectDN().getName().equals(issuerDN)) {
                return c;
            }
        }
        return null; // 找不到颁发者
    }

}
