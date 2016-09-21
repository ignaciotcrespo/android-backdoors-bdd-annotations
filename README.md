# android-backdoors-bdd-annotations
Annotations to easily add backdoors for calabash in android projects.

With this library you can avoid writing lots of calabash backdoors in the application and/or activity.

## How to use it

### Set the entry point for all the backdoors
Use annotation `@BackdoorsContext`

```java
@BackdoorsContext
public class StartApp extends Application {
}
```

### Add the backdoors
In your methods use annotation `@Backdoor` or `@Backdoors`. Current restrictions, the method must be `public` and `static`

E.g. a method exposed to calabash with the backdoor "first_backdoor_method1", use annotation `@Backdoor`
```java
@Backdoor("first_backdoor_method1")
public static void method1(){
  //do something
}
```

E.g. a method exposed to calabash with more than one backdoor, use annotation `@Backdoors`
```java
@Backdoors({
        "multiple_backdoors_in_one_method",
        "another_backdoor_for_this_method"
})
public static void method5() {
  //do something
}
```
### In manifest, set the entry point for all the backdoors

Your application class is marked with the annotation `@BackdoorsContext`, then change in manifest the same class name with the postfix "Backdoors".
```xml
<application
        android:name=".StartAppBackdoors"
        android:allowBackup="true"
```

## Gradle

To allow the project do changes at compile time add in the build.gradle file at root folder the classpath `com.neenbedankt.gradle.plugins:android-apt:1.8`
Due to this project is still on an early stage add the repositories for sonatype snapshots
```
buildscript {
    repositories {
        jcenter()
        maven {
            url "https://oss.sonatype.org/content/repositories/snapshots"
        }
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.1.2'

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files

        // the latest version of the android-apt plugin
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}

allprojects {
    repositories {
        jcenter()
        maven {
            url "https://oss.sonatype.org/content/repositories/snapshots"
        }
    }
}
```

and in the build.gradle of your app add the dependencies. Check one of the dependencies use the `apt` scope, it is the library in charge of generating the code at compile time.
```
compile 'com.github.ignaciotcrespo:android-backdoor-bdd-annotations:0.0.2-SNAPSHOT'
apt 'com.github.ignaciotcrespo:android-backdoor-bdd-compiler:0.0.3-SNAPSHOT'
```

## How it works

At compile time a new class is generated with all the backdoors found in all methods. In this case the class will be
```java
public class StartAppBackdoors extends StartApp {
  public void first_backdoor_method1() {
    com.github.ignaciotcrespo.android_backdoorsbdd_annotations.example.utils.Example1.method1();
  }

  public void multiple_backdoors_in_one_method() {
    com.github.ignaciotcrespo.android_backdoorsbdd_annotations.example.utils.Example2.method5();
  }

  public void another_backdoor_for_this_method() {
    com.github.ignaciotcrespo.android_backdoorsbdd_annotations.example.utils.Example2.method5();
  }
}
```
