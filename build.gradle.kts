// Build description for MyContacts.
//
// Everyday commands (run from the project folder):
//   ./gradlew test      run the automated tests (test/java)
//   ./gradlew run       compile and start the app
//   ./gradlew build     compile and build the single runnable jar
//   ./gradlew deploy    build it and copy it to ~/applications/MyContacts.jar
//   ./gradlew dependencies --configuration runtimeClasspath
//                       list every library, including the ones pulled in
//                       automatically by the libraries listed below

plugins {
    // "application" adds the java plugin (compile, jar) plus the run task
    application
}

java {
    // compile for Java 21, whatever Java version runs Gradle
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// Libraries are downloaded from Maven Central and cached in ~/.gradle
repositories {
    mavenCentral()
}

// The source lives directly in src/ rather than Gradle's usual
// src/main/java, so say so here.
sourceSets {
    main {
        java.setSrcDirs(listOf("src"))
        resources.setSrcDirs(emptyList<String>())
    }
    // automated tests (./gradlew test) and the files they use
    test {
        java.setSrcDirs(listOf("test/java"))
        resources.setSrcDirs(listOf("test/resources"))
    }
}

// mywidgets is not published anywhere yet, so use the jar built from the
// sibling project. Override the location with
//   ./gradlew build -PmywidgetsJar=/path/to/mywidgets.jar
val mywidgetsJar = providers.gradleProperty("mywidgetsJar")
    .orElse("../../lib/mywidgets.jar")

// Each line is "group:name:version", as listed on https://central.sonatype.com
// Upgrading a library means changing its version number here.
dependencies {
    implementation(files(mywidgetsJar))

    implementation("org.xerial:sqlite-jdbc:3.7.15-M1")          // database driver
    implementation("dom4j:dom4j:1.6.1")                         // label/address templates
    implementation("jaxen:jaxen:1.1.6")                         // XPath for dom4j
    implementation("org.apache.commons:commons-lang3:3.3.2")    // text escaping
    implementation("commons-codec:commons-codec:1.10")          // vCard decoding
    implementation("javax.mail:mail:1.4.7")                     // dropped emails
    implementation("com.itextpdf:itextpdf:5.5.3")               // PDF labels
    implementation("org.odftoolkit:simple-odf:0.6.6")           // letters (.odt)
    implementation("com.googlecode.ez-vcard:ez-vcard:0.12.1")   // vCard import/export
    implementation("com.formdev:flatlaf:3.7.2")                 // look and feel (used by mywidgets)

    // JUnit, for the tests only; it is not put in MyContacts.jar
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    // show each test's result, and the output of failing tests
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

application {
    mainClass = "org.lerot.mycontacts.MyContacts"
}

// Build one self-contained jar (the app plus every library unpacked into
// it), like the IntelliJ artifact did, so it runs with "java -jar".
tasks.jar {
    archiveFileName = "MyContacts.jar"
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
    val runtimeJars = configurations.runtimeClasspath
    dependsOn(runtimeJars)
    from({ runtimeJars.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) } })
    // signature files from signed libraries would make the merged jar
    // fail its signature check, so leave them out
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.EC")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Copy>("deploy") {
    group = "distribution"
    description = "Builds MyContacts.jar and copies it to ~/applications"
    from(tasks.jar)
    into(layout.dir(providers.systemProperty("user.home").map { File(it, "applications") }))
}
