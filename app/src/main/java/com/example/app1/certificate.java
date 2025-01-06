package com.example.app1;

import android.content.Context;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.security.auth.x500.X500Principal;

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
                // 打印证书的别名和信息
                System.out.println("Alias: " + alias);
                System.out.println("Certificate: " + certificate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
