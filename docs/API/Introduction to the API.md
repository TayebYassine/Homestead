# Introduction to the API

The Homestead API provides easy access for developers to manage regions, chunks, members, flags, and everything related to the plugin.

You can develop a third-party plugin to implement more features using the API!

## Installation

=== "Maven"
    
    ```xml
    <repository>
        <id>homestead-github</id>
        <url>https://maven.pkg.github.com/TayebYassine/Homestead</url>
    </repository>

    <dependency>
        <groupId>tfagaming.projects.minecraft.homestead</groupId>
        <artifactId>homestead</artifactId>
        <version>{LATEST_VERSION}</version>
        <classifier>api</classifier>
        <scope>provided</scope>
    </dependency>
    ```

=== "Gradle"

    ```kotlin
    repositories {
        maven("https://maven.pkg.github.com/TayebYassine/Homestead")
    }

    dependencies {
        compileOnly("tfagaming.projects.minecraft.homestead:homestead:{LATEST_VERSION}:api")
    }
    ```

Replace `{LATEST_VERSION}` with the latest available version, check [GitHub Packages](https://github.com/TayebYassine/Homestead/packages/2787077/versions).

