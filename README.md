# ICEDroid：Intrusion Countermeasure Electronics Droid
ICE（Intrusion Countermeasure Electronics：侵入対抗電子機器）： An Android app environment detection SDK, responsible for countering hooks and collecting risk control environment data.

**使用AAR**

**项目语言：Java，kotlin**

### **step1：导入aar项目**

将aar文件放置在project目录格式中app文件夹下（与src同级）的libs文件夹（没有可新建）内。

### **step2：修改配置文件**

在settings.gradle.kst文件（与app文件夹同级）的`repositories`部分中添加代码：

```kotlin
flatDir {
            dirs("libs")
        }
```

完整代码：

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()

        flatDir {
            dirs("libs")
        }

    }
}
```

在app文件夹下的build.gradle.kts文件中的`dependencies`部分添加依赖：

```kotlin
implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
```

完整代码：

```kotlin
dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


}
```

添加完成后点击上方`sync now`进行同步。

### **step3：引用aar内的类**

使用impor导入需要引用的类：

```java
import com.example.app1.result;
```

在程序中使用：

```java
result r = new result();
String s;
try {
    s = r.rootCheck();
} catch (IOException e) {
    throw new RuntimeException(e);
}
ystem.out.println(s);
```
