## Build

Use the script to build the whole benchmark
```
./scripts/dist.py
```
If some jars are not found, it is because they are packaged as wars. Try to change the corresponding names in the script to wars.

## Running in White-Box Mode

Let's call the base directory of this project `EMB_BASE`, e.g.,
```
export EMB_BASE=/home/rkh/25spring/aster/EMBv3.4.0
```

Place the agent `realtime-jacoco-agent-1.0-SNAPSHOT.jar` in `EMB_BASE`.

### Genome Nexus

Switch to `$EMB_BASE/jdk_8_maven/em/embedded/rest/genome-nexus`

Run
```
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
```
to write the whole list of dependencies to `cp.txt`.

Set up necessary env vars:

```
export jacocoAgentPort=8000
export projectTargetPath=$EMB_BASE/jdk_8_maven/cs/rest-gui/genome-nexus/web/target/
export projectPackage=org/cbioportal/genome_nexus
```

Run the service:
```
java -Djdk.attach.allowAttachSelf=true -javaagent:"${EMB_BASE}/realtime-jacoco-agent-1.0-SNAPSHOT.jar" -cp "./target/classes:$(cat cp.txt)" em.embedded.org.cbioportal.genome_nexus.EmbeddedEvoMasterController
```

The service should be running at port `12345` with jacoco agent on 8000. Modify line 81 in `jdk_8_maven/em/embedded/rest/genome-nexus/src/main/java/em/embedded/org/cbioportal/genome_nexus/EmbeddedEvoMasterController.java` to change the service port.

### Feature-Services

Switch to `$EMB_BASE/jdk_8_maven/em/embedded/rest/features-service`

Run
```
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
```
to write the whole list of dependencies to `cp.txt`.

Set up necessary env vars:

```
export jacocoAgentPort=8000
export projectTargetPath=$EMB_BASE/jdk_8_maven/cs/rest/original/features-service/target/
export projectPackage=org/javiermf/features
```

Run the service:
```
java -Djdk.attach.allowAttachSelf=true -javaagent:"${EMB_BASE}/realtime-jacoco-agent-1.0-SNAPSHOT.jar" -cp "./target/classes:$(cat cp.txt)" em.embedded.org.javiermf.features.EmbeddedEvoMasterController
```

The service should be running at port `12345` with jacoco agent on 8000. Modify line 67 in `jdk_8_maven/em/embedded/rest/features-service/src/main/java/em/embedded/org/javiermf/features/EmbeddedEvoMasterController.java` to change the service port.

The spec is available at `http://localhost:12345/swagger.json`. To check if it is running, send a request:
```
curl -i http://localhost:12345/products
```

### restcountries

Switch to `$EMB_BASE/jdk_8_maven/cs/rest/original/restcountries`

Set up necessary env vars:

```
export jacocoAgentPort=8000
export projectTargetPath=$EMB_BASE/jdk_8_maven/cs/rest/original/restcountries/target/
export projectPackage=eu/fayder/restcountries
```

Run
```
mvn clean install -DskipTest liberty:run
```

Service is running on `http://localhost:9080/restcountries-2.0.6-SNAPSHOT/`

### Language Tool

Switch to `$EMB_BASE/jdk_8_maven/cs/rest/original/languagetool/languagetool-server`

Set up necessary env vars:

```
export jacocoAgentPort=8000
export projectTargetPath=$EMB_BASE/jdk_8_maven/cs/rest/original/languagetool/languagetool-server/target/
export projectPackage=org/languagetool
```

Run the service:
```
java -Djdk.attach.allowAttachSelf=true -javaagent:"${EMB_BASE}/realtime-jacoco-agent-1.0-SNAPSHOT.jar"  -jar target/languagetool-sut.jar
```

The service should be running at port `8081` with jacoco agent on 8000. 

The spec is available at `http://localhost:8081/v2/swagger`. To check if it is running, send a request:
```
curl -i http://localhost:8081/v2/languages
```