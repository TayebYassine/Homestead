plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.6.1"
}

group = "me.tayebyassine.homestead"
version = "5.2.3.0"
description = "A chunk-based land claiming plugin"

repositories {
    mavenLocal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/creatorfromhell/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.bluecolored.de/releases")
    maven("https://repo.mikeprimm.com/")
    maven("https://api.modrinth.com/maven/")
    maven("https://libraries.minecraft.net")
    maven("https://eldonexus.de/repository/maven-releases/")
    maven("https://repo.william278.net/releases")
    maven("https://repo.thenextlvl.net/releases")
    maven("https://repo.nexomc.com/releases/")
    maven("https://repo.momirealms.net/releases/")
}

java {
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = "UTF-8"
}

dependencies {
    testImplementation("junit:junit:4.13.1")

    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.20")
    compileOnly("de.bluecolored:bluemap-api:2.7.6")
    compileOnly("com.flowpowered:flow-math:1.0.3")
    compileOnly("us.dynmap:DynmapCoreAPI:3.7-beta-6")
    compileOnly("maven.modrinth:pl3xmap:1.21.10-538")
    compileOnly("xyz.jpenilla:squaremap-api:1.3.11")
    compileOnly("com.github.GriefPrevention:GriefPrevention:16.18.2")
    compileOnly("biz.princeps:landlord-api:4.365")
    compileOnly("com.cjburkey.claimchunk:claimchunk:0.0.25-FIX3")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.4.0")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.14")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.15.0")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.15.0") {
        exclude(group = "com.fastasyncworldedit", module = "FastAsyncWorldEdit-Core")
    }
    compileOnly("com.github.angeschossen:LandsAPI:7.25.4")
    compileOnly("net.william278.huskclaims:huskclaims-bukkit:1.5.10")
    compileOnly("com.nexomc:nexo:1.21.0") {
        exclude(group = "dev.triumphteam", module = "triumph-gui")
    }
    compileOnly("beer.devs:itemsadder-api:4.0.18-beta-10")
    compileOnly("net.momirealms:craft-engine-core:26.7")
    compileOnly("net.momirealms:craft-engine-bukkit:26.7")

    implementation("commons-io:commons-io:2.18.0")
    implementation("org.postgresql:postgresql:42.7.10")
    implementation("org.mongodb:mongodb-driver-sync:5.6.5")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.8")
    implementation("com.technicjelle:BMUtils:5.0.1") {
        exclude(group = "com.flowpowered", module = "flow-math")
    }
    implementation("me.lucko:commodore:2.2")
    implementation("dev.faststats.metrics:bukkit:0.22.0")
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("com.technicjelle.BMUtils", "me.tayebyassine.homestead.libs.bmutils")
    relocate("me.lucko.commodore", "me.tayebyassine.homestead.libs.commodore")
    relocate("dev.faststats", "me.tayebyassine.homestead.libs.faststats")
    manifest {
        attributes["Main-Class"] = "me.tayebyassine.homestead.Homestead"
    }
}

tasks.jar {
    enabled = false
}

val apiJar by tasks.registering(Jar::class) {
    archiveClassifier.set("api")
    from(sourceSets.main.get().output)
}

tasks.assemble {
    dependsOn(apiJar)
}

tasks.javadoc {
    options.encoding = "UTF-8"
}

publishing {
    publications {
        create<MavenPublication>("homestead") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(tasks.shadowJar)
            artifact(tasks.named("apiJar"))
            artifact(tasks.named("sourcesJar"))
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/TayebYassine/Homestead")
            credentials(PasswordCredentials::class)
        }
    }
}
