#include <cstdio>
#include <cstdlib>
#include <jni.h>
#include <sys/system_properties.h>
#include <media/NdkMediaDrm.h>
#include <cstring>
#include <android/log.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <net/if.h>
#include <unistd.h>
#include <linux/in.h>
#include <sys/endian.h>
#include <string.h>


#include "com_example_app1_fingerprintjni.h"

//-----------------------------------------------设备指纹检测------------------------------------------------------
#ifdef __cplusplus
extern "C" {
#endif
//-----------------------------------------------获取AndroidID------------------------------------------------------
JNIEXPORT jstring JNICALL Java_com_example_app1_fingerprintjni_getandroidid(JNIEnv *env, jobject){
    //获取Activity Thread的实例对象
    jclass activityThread = env->FindClass("android/app/ActivityThread");
    jmethodID currentActivityThread = env->GetStaticMethodID(activityThread, "currentActivityThread", "()Landroid/app/ActivityThread;");
    jobject at = env->CallStaticObjectMethod(activityThread, currentActivityThread);
    //获取Application，也就是全局的Context
    jmethodID getApplication = env->GetMethodID(activityThread, "getApplication", "()Landroid/app/Application;");
    jobject context = env->CallObjectMethod(at, getApplication);
    //获取contentResolver
    jclass contextClass = env->GetObjectClass(context);
    jmethodID getContentResolverMethod = env->GetMethodID(contextClass, "getContentResolver", "()Landroid/content/ContentResolver;");
    jobject contentResolver = env->CallObjectMethod(context, getContentResolverMethod);
    //初始化字符数组用于存储设备ID信息
    char deviceID[256]; // 确保缓冲区足够大
    snprintf(deviceID, sizeof(deviceID), "Android ID：");
    //获取Settings.Secure类
    jclass settingsSecureClass = env->FindClass("android/provider/Settings$Secure");
    if (settingsSecureClass == NULL) {
        return env->NewStringUTF("错误：无法找到 Settings.Secure 类");
    }
    //获取getString方法的ID
    jmethodID getStringMethod = env->GetStaticMethodID(settingsSecureClass, "getString", "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;");
    if (getStringMethod == NULL) {
        return env->NewStringUTF("错误：无法找到 getString 方法");
    }
    // 获取ANDROID_ID字段ID
    jstring androidIdField = env->NewStringUTF("android_id");
    //调用getString方法获取Android ID
    jstring androidId = (jstring)env->CallStaticObjectMethod(settingsSecureClass, getStringMethod, contentResolver, androidIdField);
    env->DeleteLocalRef(androidIdField); // 释放 android_id 字符串引用
    //从jstring转换为C字符串
    if (androidId != NULL) {
        const char *androidIdCStr = env->GetStringUTFChars(androidId, NULL);
        snprintf(deviceID + strlen(deviceID), sizeof(deviceID) - strlen(deviceID), "%s", androidIdCStr);
        env->ReleaseStringUTFChars(androidId, androidIdCStr); // 释放 C 字符串
        env->DeleteLocalRef(androidId); // 释放 Android ID 字符串引用
    } else {
        strncat(deviceID, "Android ID：未找到\n", sizeof(deviceID) - strlen(deviceID) - 1);
    }
    //清理和释放资源
    env->DeleteLocalRef(settingsSecureClass);
    return env->NewStringUTF(deviceID);
};

//-----------------------------------------------系统指纹检测------------------------------------------------------
JNIEXPORT jstring JNICALL Java_com_example_app1_fingerprintjni_fingerprint(JNIEnv *env, jobject ){
    const char *properties[] = {
            //复合指纹
            "ro.build.fingerprint",
            "ro.odm.build.fingerprint",
            "ro.product.build.fingerprint",
            "ro.system_ext.build.fingerprint",
            "ro.system.build.fingerprint",
            "ro.vendor.build.fingerprint",
            "ro.build.description",
            //普通指纹
            "ro.build.date",
            "ro.build.date.utc",
            "ro.product.board",
            "ro.bootloader",
            "ro.product.brand",
            "ro.product.cpu.abi",
            "ro.product.device",
            "ro.build.display.id",
            "ro.hardware",
            "ro.build.host",
            "ro.build.id",
            "ro.product.model",
            "ro.product.manufacturer",
            "ro.product.name",
            "ro.product.radio",
            "ro.build.tags",
            "ro.build.time",
            "ro.build.type",
            "ro.build.user",
            "ro.build.version.release",
            "ro.build.version.codename",
            "ro.build.version.incremental",
            "ro.build.version.sdk",
            "ro.build.version.sdk_int",
            "ro.build.version.security_patch",
            "ro.build.version.preview_sdk_int",
            "ro.build.support_abis",
    };
    int numProperties = sizeof(properties) / sizeof(properties[0]);
    char *result = (char *)malloc(4096); // 分配足够的内存以存储结果
    if (result == nullptr) {
        return nullptr; // 处理内存分配失败的情况
    }
    result[0] = '\0'; // 初始化字符串
    //替换空结果
    for (int i = 0; i < numProperties; i++) {
        char value[PROP_VALUE_MAX] = {0};
        __system_property_get(properties[i], value);
        if (strlen(value) == 0 | strcmp(value,"unknown") == 0) {
            snprintf(value, sizeof(value), "无结果");
        }
        //将属性名和属性值合并到结果字符串中
        snprintf(result + strlen(result), 4096 - strlen(result), "\n%s:%s", properties[i], value);
    }
    //组合普通指纹为复合指纹
    //创建FINGERPRINT字符串
    char fingerprint[1024] = {0}; // 用于存储组合后的属性信息
    char brand[PROP_VALUE_MAX] = {0};
    char name[PROP_VALUE_MAX] = {0};
    char device[PROP_VALUE_MAX] = {0};
    char versionRelease[PROP_VALUE_MAX] = {0};
    char buildId[PROP_VALUE_MAX] = {0};
    char versionIncremental[PROP_VALUE_MAX] = {0};
    char buildType[PROP_VALUE_MAX] = {0};
    char buildTags[PROP_VALUE_MAX] = {0};
    //获取所需的属性值
    __system_property_get("ro.product.brand", brand);
    __system_property_get("ro.product.name", name);
    __system_property_get("ro.product.device", device);
    __system_property_get("ro.build.version.release", versionRelease);
    __system_property_get("ro.build.id", buildId);
    __system_property_get("ro.build.version.incremental", versionIncremental);
    __system_property_get("ro.build.type", buildType);
    __system_property_get("ro.build.tags", buildTags);
    //组合所需的属性
    snprintf(fingerprint, sizeof(fingerprint),
             "FINGERPRINT:%s/%s/%s:%s/%s/%s:%s/%s",
             brand, name, device, versionRelease, buildId, versionIncremental, buildType, buildTags);
    //将组合后的字符串添加到结果中
    snprintf(result + strlen(result), 4096 - strlen(result), "\n%s", fingerprint);
    //创建Java字符串并返回
    jstring jResult = (*env).NewStringUTF(result);
    //清理
    free(result); // 释放分配的内存
    return jResult; // 返回合并后的字符串
}

//-----------------------------------------------网络地址检测------------------------------------------------------
JNIEXPORT jstring JNICALL Java_com_example_app1_fingerprintjni_netfp(JNIEnv * env, jobject){
    //获取Activity Thread的实例对象
    jclass activityThread = env->FindClass("android/app/ActivityThread");
    jmethodID currentActivityThread = env->GetStaticMethodID(activityThread, "currentActivityThread", "()Landroid/app/ActivityThread;");
    jobject at = env->CallStaticObjectMethod(activityThread, currentActivityThread);
    //获取Application，也就是全局的Context
    jmethodID getApplication = env->GetMethodID(activityThread, "getApplication", "()Landroid/app/Application;");
    jobject context = env->CallObjectMethod(at, getApplication);
    //获取WiFi服务
    jclass contextClass = env->FindClass("android/content/Context");
    jmethodID getSystemService = env->GetMethodID(contextClass, "getSystemService", "(Ljava/lang/String;)Ljava/lang/Object;");
    //获取WifiManager
    jstring wifiService = env->NewStringUTF("wifi");
    jobject wifiManager = env->CallObjectMethod(context, getSystemService, wifiService);
    //获取DhcpInfo
    jclass wifiManagerClass = env->GetObjectClass(wifiManager);
    jmethodID getDhcpInfoMethod = env->GetMethodID(wifiManagerClass, "getDhcpInfo", "()Landroid/net/DhcpInfo;");
    jobject dhcpInfo = env->CallObjectMethod(wifiManager, getDhcpInfoMethod);
    //获取IP地址
    jclass dhcpInfoClass = env->GetObjectClass(dhcpInfo);
    jfieldID ipAddressField = env->GetFieldID(dhcpInfoClass, "ipAddress", "I");
    jint ipAddress = env->GetIntField(dhcpInfo, ipAddressField);
    //转换IP地址为字符串
    char ipStr[16];
    snprintf(ipStr, sizeof(ipStr), "%d.%d.%d.%d",
             (ipAddress & 0xFF), (ipAddress >> 8) & 0xFF,
             (ipAddress >> 16) & 0xFF, (ipAddress >> 24) & 0xFF);
    jint mallocsize = 8192;//分配结果空间大小
    char *result = (char *)malloc(mallocsize); // 分配足够的内存以存储结果
    if (result == nullptr) {
        return nullptr; // 处理内存分配失败的情况
    }
    result[0] = '\0'; // 初始化字符串
    //组装结果
    snprintf(result + strlen(result), mallocsize - strlen(result), "IP地址:%s\n", ipStr);
    //尝试获取MAC地址（在高版本Android，MAC地址会受到限制）
    jclass wifiInfoClass = env->FindClass("android/net/wifi/WifiInfo");
    jmethodID getConnectionInfoMethod = env->GetMethodID(wifiManagerClass, "getConnectionInfo", "()Landroid/net/wifi/WifiInfo;");
    jobject wifiInfo = env->CallObjectMethod(wifiManager, getConnectionInfoMethod);
    if (wifiInfo != nullptr) {
        jmethodID getMacAddressMethod = env->GetMethodID(wifiInfoClass, "getMacAddress", "()Ljava/lang/String;");
        jstring macAddress = (jstring)env->CallObjectMethod(wifiInfo, getMacAddressMethod);
        const char *macCStr = env->GetStringUTFChars(macAddress, nullptr);
        snprintf(result + strlen(result), mallocsize - strlen(result), "MAC地址:%s\n", macCStr);
        env->ReleaseStringUTFChars(macAddress, macCStr);
    }
    env->DeleteLocalRef(wifiService);
    env->DeleteLocalRef(context);
    env->DeleteLocalRef(wifiManager);
    env->DeleteLocalRef(dhcpInfo);
    env->DeleteLocalRef(wifiInfo);
    //返回拼接的字符串
    jstring jResult = (*env).NewStringUTF(result);
    return jResult;
 };

//-----------------------------------------------hook检测方法------------------------------------------------------
JNIEXPORT jstring JNICALL Java_com_example_app1_fingerprintjni_check(JNIEnv *env, jobject){
    const char* result1 = "检测到frida服务器端口";
    const char* result2 = "未检测到frida服务器端口";
    struct sockaddr_in sa{};
    //创建一个socket文件描述符
    int sock;
    //定义一个字符数组res，用于存储接收到的数据
    char res[7];
    //循环遍历所有可能的端口号
    for(int i = 27042; i <= 27042; i++) {
        // 创建一个新的socket连接
        sock = socket(AF_INET, SOCK_STREAM, 0);
        // 设置socket地址结构体的端口号
        sa.sin_port = htons(i);
        // 尝试连接到当前端口
        if (connect(sock, (struct sockaddr*)&sa, sizeof(sa)) != -1) {
            //如果连接成功，记录日志信息，表示发现了一个开放的端口
            //__android_log_print(ANDROID_LOG_VERBOSE, "ZJ595", "FRIDA DETECTION [1]: Open Port: %d", i);
            //初始化res数组，清零
            memset(res, 0, 7);
            //向socket发送一个空字节
            send(sock, "\x00", 1, 0); // 注意这里的NULL被替换为0
            //发送AUTH请求
            send(sock, "AUTH\r\n", 6, 0);
            //等待100微秒
            usleep(100);
            //尝试接收响应
            if (recv(sock, res, 6, MSG_DONTWAIT) != -1) {
                // 如果接收到响应，检查响应内容是否为"REJECT"
                if (strcmp(res, "REJECT") == 0) {
                    // 如果是，关闭socket并返回true，表示检测到了Frida服务器
                    close(sock);
                    jstring jResult = (*env).NewStringUTF(result1);
                    return jResult; // Frida server detected
                }
            }
        }
        //如果当前端口连接失败或没有检测到Frida服务器，关闭socket
        close(sock);
    }
    //如果遍历完所有端口都没有检测到Frida服务器
    jstring jResult = (*env).NewStringUTF(result2);
    return jResult; // No Frida server detected
};
JNIEXPORT jstring JNICALL Java_com_example_app1_fingerprintjni_mapscheck(JNIEnv * env, jobject){
    char line[512];
    const char* result1 = "检测到frida特征文件";
    const char* result2 = "未检测到frida特征文件";
    const char* result3 = "系统状态异常";
    // 打开当前进程的内存映射文件/proc/self/maps进行读取
    FILE* fp = fopen("/proc/self/maps", "r");
    if (fp) {
        // 如果文件成功打开，循环读取每一行
        while (fgets(line, sizeof(line), fp)) {
            // 使用strstr函数检查当前行是否包含"frida"字符串
            if (strstr(line, "frida") || strstr(line, "gadget")) {
                // 如果找到了"frida"，关闭文件并返回true，表示检测到了恶意库
                fclose(fp);
                jstring jResult = (*env).NewStringUTF(result1);
                return jResult; // Evil library is loaded.
            }
        }
        // 遍历完文件后，关闭文件
        fclose(fp);
    } else {
        //如果无法打开文件，记录错误。这可能意味着系统状态异常
        //没有处理错误
        jstring jResult = (*env).NewStringUTF(result3);
        return jResult;
    }
    //如果没有在内存映射文件中找到"frida"，表示没有检测到恶意库
    jstring jResult = (*env).NewStringUTF(result2);
    return jResult; // No evil library detected.
};

//-----------------------------------------------获取已安装应用------------------------------------------------------
JNIEXPORT jstring JNICALL Java_com_example_app1_fingerprintjni_getappnames(JNIEnv * env, jobject ){
    //获取Activity Thread的实例对象
    jclass activityThread = env->FindClass("android/app/ActivityThread");
    jmethodID currentActivityThread = env->GetStaticMethodID(activityThread, "currentActivityThread", "()Landroid/app/ActivityThread;");
    jobject at = env->CallStaticObjectMethod(activityThread, currentActivityThread);
    //获取Application，也就是全局的Context
    jmethodID getApplication = env->GetMethodID(activityThread, "getApplication", "()Landroid/app/Application;");
    jobject context = env->CallObjectMethod(at, getApplication);
    //获取PackageManager
    jclass contextClass = env->GetObjectClass(context);
    jclass pmClass = env->FindClass("android/content/pm/PackageManager");
    //获取PackageManager实例
    jmethodID getPackageManagerMethod = env->GetMethodID(contextClass, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jobject pmObject = env->CallObjectMethod(context, getPackageManagerMethod);
    //获取已安装应用程序
    jmethodID getInstalledPackagesMethod = env->GetMethodID(pmClass, "getInstalledPackages", "(I)Ljava/util/List;");
    jobject installedPackages = env->CallObjectMethod(pmObject, getInstalledPackagesMethod, 0); // 0 = GET_UNINSTALLED_PACKAGES
    //获取List类和相关方法
    jclass listClass = env->FindClass("java/util/List");
    jmethodID sizeMethod = env->GetMethodID(listClass, "size", "()I");
    jmethodID getMethod = env->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");
    //获取List的大小
    jint mallocsize = 65535;//分配结果空间大小
    jint size = env->CallIntMethod(installedPackages, sizeMethod);
    char *result = (char *)malloc(mallocsize); // 分配足够的内存以存储结果
    if (result == nullptr) {
        return nullptr; // 处理内存分配失败的情况mallocsize
    }
    result[0] = '\0'; // 初始化字符串
    snprintf(result + strlen(result), mallocsize - strlen(result), "已安装应用个数: %d\n", size);
    //遍历安装的应用程序
    jclass packageInfoClass = env->FindClass("android/content/pm/PackageInfo");
    jclass applicationInfoClass = env->FindClass("android/content/pm/ApplicationInfo");
    for (int i = 0; i < size; i++) {
        jobject packageInfo = env->CallObjectMethod(installedPackages, getMethod, i);
        //获取应用名
        jfieldID applicationInfoField = env->GetFieldID(packageInfoClass, "applicationInfo", "Landroid/content/pm/ApplicationInfo;");
        jobject applicationInfo = env->GetObjectField(packageInfo, applicationInfoField);
        jclass aiClass = applicationInfoClass;
        jmethodID loadLabelMethod = env->GetMethodID(aiClass, "loadLabel", "(Landroid/content/pm/PackageManager;)Ljava/lang/CharSequence;");
        jobject appNameCharSequence = env->CallObjectMethod(applicationInfo, loadLabelMethod, pmObject);
        //将CharSequence转换为String
        jclass charSequenceClass = env->FindClass("java/lang/CharSequence");
        jmethodID toStringMethod = env->GetMethodID(charSequenceClass, "toString", "()Ljava/lang/String;");
        jstring appNameString = static_cast<jstring>(env->CallObjectMethod(appNameCharSequence, toStringMethod));
        // 获取包名
        jfieldID packageNameField = env->GetFieldID(packageInfoClass, "packageName", "Ljava/lang/String;");
        jstring packageNameString = static_cast<jstring>(env->GetObjectField(packageInfo, packageNameField));
        // 将字符串拼接到 appNames
        const char *appNameCStr = env->GetStringUTFChars(appNameString, nullptr);
        const char *packageNameCStr = env->GetStringUTFChars(packageNameString, nullptr);
        snprintf(result + strlen(result), mallocsize - strlen(result), "%s:%s\n", appNameCStr, packageNameCStr);
        env->ReleaseStringUTFChars(appNameString, appNameCStr);
        env->ReleaseStringUTFChars(packageNameString, packageNameCStr);
    }
    // 返回拼接的字符串
    jstring jResult = (*env).NewStringUTF(result);
    return jResult;
};

//-----------------------------------------------获取CA证书------------------------------------------------------
JNIEXPORT jstring JNICALL Java_com_example_app1_fingerprintjni_getcertificate(JNIEnv *env, jobject){
    //初始化字符串结果
    jint mallocsize = 65535;//分配结果空间大小
    char *certInfo = (char *)malloc(mallocsize); // 分配足够的内存以存储结果
    snprintf(certInfo, mallocsize, "证书信息\n---------------------------------\n");
    //获取KeyStore类
    jclass keyStoreClass = env->FindClass("java/security/KeyStore");
    if (keyStoreClass == nullptr) {
        return env->NewStringUTF("错误：无法找到 KeyStore 类");
    }
    //获取AndroidCAStore的实例
    jmethodID getInstanceMethod = env->GetStaticMethodID(keyStoreClass, "getInstance", "(Ljava/lang/String;)Ljava/security/KeyStore;");
    jstring androidCAStore = env->NewStringUTF("AndroidCAStore");
    jobject keyStore = env->CallStaticObjectMethod(keyStoreClass, getInstanceMethod, androidCAStore);
    env->DeleteLocalRef(androidCAStore);
    //加载密钥库
    jmethodID loadMethod = env->GetMethodID(keyStoreClass, "load", "(Ljava/io/InputStream;[C)V");
    env->CallVoidMethod(keyStore, loadMethod, nullptr, nullptr); // 使用默认参数加载
    //获取证书别名的迭代器
    jmethodID aliasesMethod = env->GetMethodID(keyStoreClass, "aliases", "()Ljava/util/Enumeration;");
    jobject aliasesEnumeration = env->CallObjectMethod(keyStore, aliasesMethod);
    //获取Enumeration类和相关方法
    jclass enumerationClass = env->FindClass("java/util/Enumeration");
    jmethodID hasMoreElementsMethod = env->GetMethodID(enumerationClass, "hasMoreElements", "()Z");
    jmethodID nextElementMethod = env->GetMethodID(enumerationClass, "nextElement", "()Ljava/lang/Object;");
    //获取证书信息
    while (env->CallBooleanMethod(aliasesEnumeration, hasMoreElementsMethod)) {
        jstring alias = (jstring)env->CallObjectMethod(aliasesEnumeration, nextElementMethod);
        //获取证书
        jmethodID getCertificateMethod = env->GetMethodID(keyStoreClass, "getCertificate", "(Ljava/lang/String;)Ljava/security/cert/Certificate;");
        jobject certificate = env->CallObjectMethod(keyStore, getCertificateMethod, alias);
        //获取X509Certificate类
        jclass x509Class = env->FindClass("java/security/cert/X509Certificate");
        if (x509Class == nullptr) {
            return env->NewStringUTF("错误：无法找到 X509Certificate 类");
        }
        //检查证书是否为X509Certificate
        if (certificate != nullptr && env->IsInstanceOf(certificate, x509Class)) {
            //获取证书使用者
            jmethodID getSubjectDNMethod = env->GetMethodID(x509Class, "getSubjectDN", "()Ljava/security/Principal;");
            jobject subjectDN = env->CallObjectMethod(certificate, getSubjectDNMethod);
            jmethodID getNameMethod = env->GetMethodID(env->GetObjectClass(subjectDN), "getName", "()Ljava/lang/String;");
            jstring subjectDNString = (jstring)env->CallObjectMethod(subjectDN, getNameMethod);
            //获取证书颁发者
            jmethodID getIssuerDNMethod = env->GetMethodID(x509Class, "getIssuerDN", "()Ljava/security/Principal;");
            jobject issuerDN = env->CallObjectMethod(certificate, getIssuerDNMethod);
            jstring issuerDNString = (jstring)env->CallObjectMethod(issuerDN, getNameMethod);
            //获取证书发布日期
            jmethodID getNotBeforeMethod = env->GetMethodID(x509Class, "getNotBefore", "()Ljava/util/Date;");
            jobject notBeforeDate = env->CallObjectMethod(certificate, getNotBeforeMethod);
            jmethodID toStringMethod = env->GetMethodID(env->GetObjectClass(notBeforeDate), "toString", "()Ljava/lang/String;");
            jstring notBeforeString = (jstring)env->CallObjectMethod(notBeforeDate, toStringMethod);
            //获取证书失效日期
            jmethodID getNotAfterMethod = env->GetMethodID(x509Class, "getNotAfter", "()Ljava/util/Date;");
            jobject notAfterDate = env->CallObjectMethod(certificate, getNotAfterMethod);
            jstring notAfterString = (jstring)env->CallObjectMethod(notAfterDate, toStringMethod);
            //拼接证书信息
            const char *aliasCStr = env->GetStringUTFChars(alias, nullptr);
            const char *subjectCStr = env->GetStringUTFChars(subjectDNString, nullptr);
            const char *issuerCStr = env->GetStringUTFChars(issuerDNString, nullptr);
            const char *validityCStr = env->GetStringUTFChars(notBeforeString, nullptr);
            const char *validityEndCStr = env->GetStringUTFChars(notAfterString, nullptr);
            //将证书信息添加到结果字符串
            snprintf(certInfo + strlen(certInfo), mallocsize - strlen(certInfo),
                     "Alias: %s\nSubject: %s\nIssuer: %s\nValidity: %s - %s\n---------------------------------\n",
                     aliasCStr, subjectCStr, issuerCStr, validityCStr, validityEndCStr);
            //释放C字符串
            env->ReleaseStringUTFChars(alias, aliasCStr);
            env->ReleaseStringUTFChars(subjectDNString, subjectCStr);
            env->ReleaseStringUTFChars(issuerDNString, issuerCStr);
            env->ReleaseStringUTFChars(notBeforeString, validityCStr);
            env->ReleaseStringUTFChars(notAfterString, validityEndCStr);
        }
        env->DeleteLocalRef(alias);
    }
    // 清理和释放资源
    env->DeleteLocalRef(aliasesEnumeration);
    env->DeleteLocalRef(keyStore);
    env->DeleteLocalRef(keyStoreClass);
    env->DeleteLocalRef(enumerationClass);
    // 返回结果
    return env->NewStringUTF(certInfo);
};

//-----------------------------------------------获取支持软硬件------------------------------------------------------
JNIEXPORT jstring JNICALL Java_com_example_app1_fingerprintjni_getdevicefeatures(JNIEnv *env, jobject){
    // 获取Activity Thread的实例对象
    jclass activityThread = env->FindClass("android/app/ActivityThread");
    jmethodID currentActivityThread = env->GetStaticMethodID(activityThread, "currentActivityThread", "()Landroid/app/ActivityThread;");
    jobject at = env->CallStaticObjectMethod(activityThread, currentActivityThread);

    // 获取Application，也就是全局的Context
    jmethodID getApplication = env->GetMethodID(activityThread, "getApplication", "()Landroid/app/Application;");
    jobject context = env->CallObjectMethod(at, getApplication);

    // 获取PackageManager
    jclass contextClass = env->GetObjectClass(context);
    jmethodID getPackageManagerMethod = env->GetMethodID(contextClass, "getPackageManager", "()Landroid/content/pm/PackageManager;");
    jobject packageManager = env->CallObjectMethod(context, getPackageManagerMethod);

    // 获取系统可用特性
    jclass packageManagerClass = env->GetObjectClass(packageManager);
    jmethodID getSystemAvailableFeaturesMethod = env->GetMethodID(packageManagerClass, "getSystemAvailableFeatures", "()[Landroid/content/pm/FeatureInfo;");
    jobjectArray featuresArray = (jobjectArray)env->CallObjectMethod(packageManager, getSystemAvailableFeaturesMethod);

    // 初始化特性字符串
    jint bufferSize = 65535;
    char *featureList = (char *)malloc(bufferSize);
    if (featureList == NULL) {
        return env->NewStringUTF("错误：内存分配失败");
    }
    snprintf(featureList, bufferSize, "支持软硬件：\n");

    // 迭代特性并获取名称
    jsize featureCount = env->GetArrayLength(featuresArray);
    for (int i = 0; i < featureCount; i++) {
        jobject featureInfo = env->GetObjectArrayElement(featuresArray, i);
        jclass featureInfoClass = env->GetObjectClass(featureInfo);

        // 获取特征名称
        jfieldID nameField = env->GetFieldID(featureInfoClass, "name", "Ljava/lang/String;");
        jstring featureName = (jstring)env->GetObjectField(featureInfo, nameField);
        if (featureName != NULL) {
            const char *nameCStr = env->GetStringUTFChars(featureName, NULL);
            snprintf(featureList + strlen(featureList), bufferSize - strlen(featureList), "%s\n", nameCStr);
            env->ReleaseStringUTFChars(featureName, nameCStr); // 释放字符串
            env->DeleteLocalRef(featureName); // 释放局部引用
        }
        env->DeleteLocalRef(featureInfo); // 释放局部引用
        env->DeleteLocalRef(featureInfoClass); // 释放局部引用
    }

    // 清理和释放资源
    env->DeleteLocalRef(featuresArray);
    env->DeleteLocalRef(packageManagerClass);
    env->DeleteLocalRef(packageManager);
    env->DeleteLocalRef(contextClass);
    env->DeleteLocalRef(context);
    env->DeleteLocalRef(at);

    // 创建返回的字符串
    jstring result = env->NewStringUTF(featureList);
    free(featureList); // 释放分配的内存

    return result;
};


#ifdef __cplusplus
}
#endif