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
        <version>5.2.0.0</version>
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
        compileOnly("tfagaming.projects.minecraft.homestead:homestead:5.2.0.0:api")
    }
    ```

All versions are available in [GitHub Packages](https://github.com/TayebYassine/Homestead/packages/2787077/versions).

## JavaDoc

Documentation for every class and methods are located right [here](https://tayebyassine.github.io/Homestead/javadoc/).

### Quick References
- Events: [Click here](https://tayebyassine.github.io/Homestead/javadoc/tfagaming/projects/minecraft/homestead/api/events/package-summary.html)
- Managers: [Click here](https://tayebyassine.github.io/Homestead/javadoc/tfagaming/projects/minecraft/homestead/managers/package-summary.html)
- Models: [Click here](https://tayebyassine.github.io/Homestead/javadoc/tfagaming/projects/minecraft/homestead/models/package-summary.html)
