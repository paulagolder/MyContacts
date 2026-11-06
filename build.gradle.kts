// Build description for MyContacts.
//
// Everyday commands (run from the project folder):
//   ./gradlew test      run the automated tests (test/java)
//   ./gradlew run       compile and start the app (the Contacts viewer first)
//   ./gradlew runFull   start it on the full MyContacts window
//   ./gradlew runSandbox  start it on made-up test data in build/sandbox, to
//                       try things (an import, say) away from your contacts
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
        // icons etc., put in the jar alongside the classes
        resources.setSrcDirs(listOf("resources"))
    }
    // automated tests (./gradlew test) and the files they use
    test {
        java.setSrcDirs(listOf("test/java"))
        resources.setSrcDirs(listOf("test/resources"))
    }
}

// Each line is "group:name:version", as listed on https://central.sonatype.com
// Upgrading a library means changing its version number here.
dependencies {
    // built from ../mywidgets (see includeBuild in settings.gradle.kts);
    // it brings the FlatLaf look and feel with it
    implementation("org.lerot:mywidgets")

    implementation("org.xerial:sqlite-jdbc:3.53.4.0")          // database driver
    implementation("org.dom4j:dom4j:2.2.0")                     // label/address templates
    implementation("jaxen:jaxen:2.0.6")                         // XPath for dom4j
    implementation("org.apache.commons:commons-lang3:3.21.0")    // StringUtils
    implementation("org.apache.commons:commons-text:1.15.0")     // XML escaping, WordUtils
    implementation("commons-codec:commons-codec:1.23.0")       // vCard decoding
    implementation("com.sun.mail:javax.mail:1.6.2")            // dropped emails
    implementation("com.itextpdf:itextpdf:5.5.13.6")           // PDF labels
    implementation("org.odftoolkit:simple-odf:0.9.0")           // letters (.odt)
    implementation("com.googlecode.ez-vcard:ez-vcard:0.12.2")   // vCard import/export
    implementation("com.formdev:flatlaf:3.7.2")                 // the look (the Contacts viewer styles it directly)
    implementation("com.formdev:flatlaf-extras:3.7.2")          // the viewer's SVG icons
    implementation("com.miglayout:miglayout-swing:11.5.1")      // the viewer's layouts
    // odfdom logs through SLF4J; discard its messages (and the startup notice)
    runtimeOnly("org.slf4j:slf4j-nop:1.7.36")

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
    // list each use of a deprecated (outdated) method, not just a summary
    options.compilerArgs.add("-Xlint:deprecation")
}

application {
    // opens the Contacts viewer, with the full window a click away
    mainClass = "org.lerot.mycontacts.Main"
}

// ./gradlew runFull starts on the full window instead of the viewer
tasks.register<JavaExec>("runFull") {
    group = "application"
    description = "Runs MyContacts on the full window"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = application.mainClass
    args("--full")
}

// ./gradlew runSandbox runs MyContacts on a copy of the made-up test data in
// build/sandbox, kept between runs; ./gradlew runSandbox -Preset starts afresh
tasks.register<JavaExec>("runSandbox") {
    group = "application"
    description = "Runs MyContacts on a copy of the test data in build/sandbox"
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "org.lerot.mycontacts.Sandbox"
    args(layout.buildDirectory.dir("sandbox").get().asFile.path)
    if (providers.gradleProperty("reset").isPresent) args("reset")
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

// Screenshots of every screen, using the made-up test data, for checking
// layout changes: take them, compare with the accepted baseline, and accept
// the new ones once they look right.
val screenshotsDir = layout.buildDirectory.dir("screenshots")

// ./gradlew screenshots -PuiScale=1.5 takes them as on a screen set to 150%
// and writes build/screenshots/scale-1.5 instead
val uiScale = providers.gradleProperty("uiScale").orNull
val shotsFolder = if (uiScale != null) "scale-$uiScale" else "current"

tasks.register<JavaExec>("screenshots") {
    group = "verification"
    description = "Saves a PNG of every screen to build/screenshots/current"
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "org.lerot.mycontacts.Screenshots"
    // ./gradlew screenshots -PjswLayout=legacy uses the old mywidgets layout
    // code and writes build/screenshots/legacy instead
    val legacy = providers.gradleProperty("jswLayout").orNull == "legacy"
    if (legacy) systemProperty("jsw.layout", "legacy")
    if (uiScale != null) systemProperty("flatlaf.uiScale", uiScale)
    args(screenshotsDir.get().dir(if (legacy) "legacy" else shotsFolder).asFile.path,
        screenshotsDir.get().dir("home").asFile.path)
}

// myContactsLite's screens, added to the same folder as lite-*.png
tasks.register<JavaExec>("liteScreenshots") {
    group = "verification"
    description = "Saves PNGs of myContactsLite to build/screenshots/current"
    mustRunAfter("screenshots")
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "org.lerot.mycontacts.lite.LiteScreenshots"
    if (uiScale != null) systemProperty("flatlaf.uiScale", uiScale)
    args(screenshotsDir.get().dir(shotsFolder).asFile.path,
        screenshotsDir.get().dir("home-lite").asFile.path)
}
tasks.named("screenshots") { finalizedBy("liteScreenshots") }

tasks.register<JavaExec>("compareScreenshots") {
    group = "verification"
    description = "Compares the current screenshots with screenshots/baseline"
    dependsOn("screenshots", "liteScreenshots")
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass = "org.lerot.mycontacts.ScreenshotCompare"
    args(file("screenshots/baseline").path,
        screenshotsDir.get().dir("current").asFile.path,
        screenshotsDir.get().dir("diff").asFile.path)
}

tasks.register<Sync>("acceptScreenshots") {
    group = "verification"
    description = "Makes the current screenshots the new baseline"
    from(screenshotsDir.map { it.dir("current") })
    into("screenshots/baseline")
}

tasks.register<Copy>("deploy") {
    group = "distribution"
    description = "Builds MyContacts.jar and copies it to ~/applications"
    from(tasks.jar)
    into(layout.dir(providers.systemProperty("user.home").map { File(it, "applications") }))
}

// ./gradlew packageApp builds an installer that brings its own Java, so it
// runs on a computer without Java: a .deb on Linux, an .msi on Windows
// (jpackage can only make a package for the system it runs on; the GitHub
// workflow .github/workflows/package.yml builds both). It goes to
// build/package. Options:
//   -PappVersion=1.2.0          the version shown to the system (default 1.0.0)
//   -PpackageType=app-image     just the runnable folder, no installer, to try
val appVersion = providers.gradleProperty("appVersion").getOrElse("1.0.0")
val windows = System.getProperty("os.name").startsWith("Windows")

tasks.register("packageApp") {
    group = "distribution"
    description = "Builds an installer with its own Java in build/package"
    dependsOn(tasks.jar)
    // local copies: the task must not refer to the build script itself
    val windows = windows
    val version = appVersion
    val jpackage = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(21) }
        .map { it.metadata.installationPath.file(if (windows) "bin/jpackage.exe" else "bin/jpackage").asFile.path }
    val type = providers.gradleProperty("packageType").getOrElse(if (windows) "msi" else "deb")
    val out = layout.buildDirectory.dir("package").get().asFile
    val input = layout.buildDirectory.dir("package-input").get().asFile
    val jar = tasks.jar.get().archiveFile.get().asFile
    val icon = file(if (windows) "packaging/mycontacts.ico" else "packaging/mycontacts.png").path
    doLast {
        out.deleteRecursively()
        input.deleteRecursively()
        jar.copyTo(File(input, jar.name))
        val args = mutableListOf(jpackage.get(),
            "--type", type,
            "--name", "MyContacts",
            "--app-version", version,
            "--vendor", "lerot",
            "--description", "Contacts, labels and letters",
            "--input", input.path,
            "--main-jar", jar.name,
            "--main-class", "org.lerot.mycontacts.Main",
            "--dest", out.path,
            // the parts of Java the app and its libraries use (from jdeps),
            // plus mail lookups, old vCard character sets and French dates
            "--add-modules", listOf("java.base", "java.desktop", "java.logging", "java.net.http",
                "java.prefs", "java.rmi", "java.scripting", "java.security.sasl", "java.sql",
                "java.xml", "java.xml.crypto", "jdk.unsupported", "jdk.xml.dom", "java.naming",
                "jdk.charsets", "jdk.localedata", "jdk.zipfs").joinToString(","),
            "--jlink-options", "--strip-debug --no-man-pages --no-header-files --include-locales=en,fr")
        args += listOf("--icon", icon)
        if (type != "app-image") args += if (windows) listOf(
            "--win-menu", "--win-menu-group", "MyContacts", "--win-shortcut",
            // installs for the user only, so no administrator is needed
            "--win-per-user-install", "--win-dir-chooser",
            // the same for every version, so a new one replaces the old
            "--win-upgrade-uuid", "e94bf614-b95f-413b-9c5b-cf813d6fa551")
        else listOf(
            "--linux-shortcut", "--linux-menu-group", "Office", "--linux-app-category", "office",
            "--linux-package-name", "mycontacts")
        val result = ProcessBuilder(args).inheritIO().start().waitFor()
        if (result != 0) throw GradleException("jpackage failed ($result)")
        println("package in " + out)
    }
}
