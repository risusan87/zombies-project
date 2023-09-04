# zombies-project
A minecraft spigot server plugin that adds zombies minigame to the server.

# Development Environment
This plugin is (almost) 100% written in java.
You will need latest version of Apache Maven(v3.9.0) and Java Development Kit v1.8 installed.
Make sure to have correct versions:
```terminal
# Check maven version (3.9.0)
$mvn -v

# Check Java version (1.8.0_xxx)
$javac -version
```
To accept org.bukkit dependency written in pom.xml, you will manually need to build and add craftbukkit api locally.<br>
First, visit the spigot website to download the buildtools: https://www.spigotmc.org/wiki/buildtools/#running-buildtools<br>
It is recommanded to move the file somewhere independent and obvious location.<br>
Open terminal at where this file exists and run
```
$java -jar Buildtools.jar --rev 1.12.2
```
This will build the server jar as well as the craftbukkit api and may take around 10 min to finish. Grab a cup of coffee with patience.<br>
Once the build is done, add CraftBukkit/target/craftbukkit-1.12.2-R0.1-SNAPSHOT.jar file to the local maven repository:
```
$mvn install:install-file -Dfile=.../CraftBukkit/target/craftbukkit-1.12.2-R0.1-SNAPSHOT.jar -DgroupId=org.bukkit -DartifactId=craftbukkit -Dversion=1.12.2-R0.1-SNAPSHOT -Dpackaging=jar
```
To build the release of package, run
```
$mvn package -f ".../pom.xml"
```
