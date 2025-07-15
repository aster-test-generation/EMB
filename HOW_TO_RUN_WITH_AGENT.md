## Build

For EMB, we need to add a GitHub token so that it can pull the docker container from GitHub. Please refer to the readme of EMB (using this repository): https://github.com/WebFuzzing/EMB?tab=readme-ov-file#using-this-repository

Use the script to build the whole benchmark
```
./scripts/dist.py
```
If some jars are not found, it is because they are packaged as wars. Try to change the corresponding names in the script to wars.

## Running in Black-Box Mode

Let's call the base directory of this project `EMB_BASE`, e.g.,
```
export EMB_BASE=/home/rkh/25spring/aster/EMBv3.4.0
```

Place the agent `realtime-jacoco-agent-1.0-SNAPSHOT.jar` in `EMB_BASE`.


## Using mitmproxy to record traffics

Install mitmproxy: https://docs.mitmproxy.org/stable/overview/installation/. `apt` or `pip` installation doesn't work on my machine. So probably you want to download the binaries directly.
Dumping to `har` format (essentially a JSON) seems to be supported only in newer versions, so make sure to download the latest release.

Set up the reverse proxy to recrod all packets:
```
./mitmdump -p 9001 --mode reverse:http://localhost:12345 --set hardump=feature-services_203.har
```

Here `12345` is the port the REST service is running on, and `9001` is the port your testing tool is sending requests to. `feature-services_203.har` is the path you want to save the packets to a file.

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

The service should be running at port `12345` with jacoco agent on `8000`. Modify line 81 in `jdk_8_maven/em/embedded/rest/genome-nexus/src/main/java/em/embedded/org/cbioportal/genome_nexus/EmbeddedEvoMasterController.java` to change the service port.

The spec is available at `hhtp://localhost:12345/v2/api-docs`.

Start EvoMaster in black-box mode (change the seed 100 and outputFolder if necessary):
```
"/usr/lib/jvm/java-8-openjdk-amd64/jre/"/bin/java -Xms1G -Xmx4G -jar ~/aster-exp/wb/evomaster.jar --blackBox true --maxTime 3600s --bbSwaggerUrl http://localhost:12345/v2/api-docs --bbTargetUrl http://localhost:12345 --seed 100 --showProgress=true --testSuiteSplitType=NONE  --outputFormat JAVA_JUNIT_4 --outputFolder "/home/rhuang329/aster-exp/bb/tests/genome-nexus_evomaster_bb_v2__S100_10420"
```

#### Option 2: (No DB coincection?)
Change the pom.xml of genome-nexus/web/pom.xml by the below content.
```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

  <modelVersion>4.0.0</modelVersion>
  <name>Genome Nexus Web App</name>
  <description>Genome Nexus Web App Module</description>
  <artifactId>web</artifactId>
  <!--<packaging>jar</packaging>-->
  <packaging>war</packaging>

  <parent>
    <groupId>org.cbioportal.genome_nexus</groupId>
    <artifactId>genome-nexus</artifactId>
    <!-- project version is generated through git or can be passed as
         PROJECT_VERSION env variable (see version.sh) -->
    <version>1.1.49-SNAPSHOT</version>
  </parent>

  <profiles>
    <profile>
      <id>war</id>
<!--      <activation>-->
<!--        <activeByDefault>true</activeByDefault>-->
<!--      </activation>-->
      <properties>
        <packaging.type>war</packaging.type>
      </properties>
    </profile>
    <profile>
      <id>jar</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
      <properties>
        <packaging.type>jar</packaging.type>
      </properties>
    </profile>
  </profiles>

  <dependencies>
    <dependency>
      <groupId>org.cbioportal.genome_nexus</groupId>
      <artifactId>persistence</artifactId>
      <version>${project.version}</version>
    </dependency>
    <dependency>
      <groupId>org.cbioportal.genome_nexus</groupId>
      <artifactId>service</artifactId>
      <version>${project.version}</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-tomcat</artifactId>
      <scope>provided</scope>
      </dependency>
    <dependency>
      <groupId>io.springfox</groupId>
      <artifactId>springfox-swagger2</artifactId>
      <version>2.7.0</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>io.springfox</groupId>
      <artifactId>springfox-swagger-ui</artifactId>
      <version>2.7.0</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>io.sentry</groupId>
      <artifactId>sentry-spring</artifactId>
      <version>1.6.1</version>
    </dependency>
    <dependency>
      <groupId>de.flapdoodle.embed</groupId>
      <artifactId>de.flapdoodle.embed.mongo</artifactId>
      <version>2.2.0</version>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
      <pluginManagement>
          <plugins>
              <plugin>
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-maven-plugin</artifactId>
                  <version>2.2.1.RELEASE</version>
                  <executions>
                      <execution>
                          <goals>
                              <goal>repackage</goal>
                          </goals>
                          <configuration>
                              <finalName>genome-nexus</finalName>
                              <classifier>sut</classifier>
                              <mainClass>org.cbioportal.genome_nexus.GenomeNexusAnnotation</mainClass>
                          </configuration>
                      </execution>
                  </executions>
              </plugin>
          </plugins>
      </pluginManagement>

    <plugins>
<!--      <plugin>-->
<!--        <groupId>org.apache.maven.plugins</groupId>-->
<!--        <artifactId>maven-dependency-plugin</artifactId>-->
<!--        <version>2.3</version>-->
<!--        <executions>-->
<!--          &lt;!&ndash; unpack genome-nexus-frontend &ndash;&gt;-->
<!--          <execution>-->
<!--            <id>unpack</id>-->
<!--            &lt;!&ndash; generate before copying over resources &ndash;&gt;-->
<!--            <phase>generate-resources</phase>-->
<!--            <goals>-->
<!--              <goal>unpack</goal>-->
<!--            </goals>-->
<!--            <configuration>-->
<!--              <artifactItems>-->
<!--                <artifactItem>-->
<!--                  <groupId>com.github.genome-nexus</groupId>-->
<!--                  <artifactId>genome-nexus-frontend</artifactId>-->
<!--                  <version>744d3666a407c918673a7611157756404bfbd1a1</version>-->
<!--                  <type>jar</type>-->
<!--                  <outputDirectory>src/main/resources/static</outputDirectory>-->
<!--                  <overWrite>true</overWrite>-->
<!--                </artifactItem>-->
<!--              </artifactItems>-->
<!--            </configuration>-->
<!--          </execution>-->
<!--        </executions>-->
<!--      </plugin>-->
      <!-- required to build an executable jar -->
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <version>2.2.1.RELEASE</version>
      </plugin>
      <plugin>
        <groupId>pl.project13.maven</groupId>
        <artifactId>git-commit-id-plugin</artifactId>
        <!-- heroku unf does not have git directory available, so ignore -->
        <configuration>
          <failOnNoGitDirectory>false</failOnNoGitDirectory>
        </configuration>
      </plugin>
      <!-- copy maven project properties to system variables -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.0.0-M3</version>
        <configuration>
          <forkCount>3</forkCount>
          <reuseForks>true</reuseForks>
          <argLine>-Xmx1024m -XX:MaxPermSize=256m</argLine>
          <systemPropertyVariables>
            <projectVersion>1.0</projectVersion>
          </systemPropertyVariables>
        </configuration>
      </plugin>
        <plugin>
            <groupId>io.openliberty.tools</groupId>
            <artifactId>liberty-maven-plugin</artifactId>
            <version>3.8.2</version>
            <configuration>
                <copyDependencies>
                    <dependencyGroup>
                        <location>${project.build.directory}/liberty/wlp/usr/shared/resources</location>
                        <dependency>
                            <groupId>org.hsqldb</groupId>
                            <artifactId>hsqldb</artifactId>
                        </dependency>
                    </dependencyGroup>
                </copyDependencies>
                <jvmOptions>
                    <option>-javaagent:${project.basedir}/../../../../../realtime-jacoco-agent-1.0-SNAPSHOT.jar</option>
                </jvmOptions>
            </configuration>
            <executions>
                <execution>
                    <id>install-liberty</id>
                    <phase>compile</phase>
                    <goals>
                        <goal>create</goal>
                        <goal>install-feature</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
      <!-- copy application.properties.EXAMPLE if it doesn't exist -->
      <plugin>
        <artifactId>maven-antrun-plugin</artifactId>
        <version>1.8</version>
        <!-- only needs to be executed for parent pom -->
        <inherited>false</inherited>
        <executions>
          <execution>
            <phase>validate</phase>
            <goals>
              <goal>run</goal>
            </goals>
            <configuration>
              <target>
                <taskdef resource="net/sf/antcontrib/antlib.xml" classpathref="maven.dependency.classpath" />
                <if>
                  <not>
                    <available file="src/main/resources/application.properties" />
                  </not>
                  <then>
                    <copy
                      file="src/main/resources/application.properties.EXAMPLE"
                      tofile="src/main/resources/application.properties" />
                  </then>
                </if>
              </target>
            </configuration>
          </execution>
        </executions>
        <dependencies>
          <dependency>
            <groupId>ant-contrib</groupId>
            <artifactId>ant-contrib</artifactId>
            <version>1.0b3</version>
            <exclusions>
                <exclusion>
                    <groupId>ant</groupId>
                    <artifactId>ant</artifactId>
                </exclusion>
            </exclusions>
          </dependency>
          <dependency>
            <groupId>org.apache.ant</groupId>
            <artifactId>ant-nodeps</artifactId>
            <version>1.8.1</version>
          </dependency>
        </dependencies>
      </plugin>
      <!-- don't alter static fonts & favicon -->
      <!-- https://stackoverflow.com/questions/34037051/ -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-resources-plugin</artifactId>
        <version>3.1.0</version>
        <configuration>
          <nonFilteredFileExtensions>
            <nonFilteredFileExtension>ttf</nonFilteredFileExtension>
            <nonFilteredFileExtension>woff</nonFilteredFileExtension>
            <nonFilteredFileExtension>woff2</nonFilteredFileExtension>
            <nonFilteredFileExtension>ico</nonFilteredFileExtension>
          </nonFilteredFileExtensions>
        </configuration>
      </plugin>
    </plugins>
    <resources>
      <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
        <excludes>
          <exclude>*.EXAMPLE</exclude>
          <!-- don't alter self signed ssl keys (used for dev) -->
          <!-- https://stackoverflow.com/questions/17298126/ -->
          <exclude>**/*.p12</exclude>
        </excludes>
      </resource>
      <resource>
        <directory>src/main/resources</directory>
        <filtering>false</filtering>
        <includes>
          <!-- copy unaltered self signed ssl keys (used for dev) -->
          <!-- https://stackoverflow.com/questions/17298126/ -->
          <include>**/*.p12</include>
        </includes>
    </resource>
    </resources>
  </build>

</project>
```
Run ``mvn clean package liberty:run -DskipTests``

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

The service should be running at port `12345` with jacoco agent on `8000`. Modify line 67 in `jdk_8_maven/em/embedded/rest/features-service/src/main/java/em/embedded/org/javiermf/features/EmbeddedEvoMasterController.java` to change the service port.

The spec is available at `http://localhost:12345/swagger.json`. To check if it is running, send a request:
```
curl -i http://localhost:12345/products
```

Start EvoMaster in black-box mode (change the seed 101 and the 101 in outputFolder if necessary):
```
"/usr/lib/jvm/java-8-openjdk-amd64/jre/"/bin/java -Xms1G -Xmx4G -jar ~/aster-exp/wb/evomaster.jar --blackBox true --maxTime 3600s --bbSwaggerUrl http://localhost:12345/swagger.json --bbTargetUrl http://localhost:12345 --seed 101 --showProgress=true --testSuiteSplitType=NONE  --outputFormat JAVA_JUNIT_4 --outputFolder "/home/rhuang329/aster-exp/bb/tests/features-service_evomaster_bb_v2__S101_10420"
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

Service is running on `http://localhost:9080/restcountries-2.0.6-SNAPSHOT/`. Modify ports in `$EMB_BASE/jdk_8_maven/cs/rest/original/restcountries/src/main/liberty/config/boostrap.properties` to change the service port.

The spec is available at `http://localhost:9080/restcountries-2.0.6-SNAPSHOT/openapi.yaml`. To check if it is running, send a request:
```
curl -i "http://localhost:9080/restcountries-2.0.6-SNAPSHOT/rest/v2"
```


Start EvoMaster in black-box mode (change the seed 105 and the 105 in outputFolder if necessary):
```
"/usr/lib/jvm/java-8-openjdk-amd64/jre/"/bin/java -Xms1G -Xmx4G -jar evomaster.jar --blackBox true --maxTime 3600s --bbSwaggerUrl ~/EMBv3.4.0/jdk_8_maven/cs/rest/origal/restcountries/src/main/resources/static/openapi.yaml --bbTargetUrl http://localhost:9080/restcountries-2.0.6-SNAPSHOT/ --seed 105 --showProgress=true --testSuiteSplitType=NONE  --outputFormat JAVA_JUNIT_4 --outputFolder "/home/rhuang329/aster-exp/bb/tests/restcountries_evomaster_bb_v2__S105_11890"
```

### Language Tool

Switch to `$EMB_BASE/jdk_8_maven/em/embedded/rest/languagetool`

Run
```
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
```
to write the whole list of dependencies to `cp.txt`.

Set up necessary env vars:

```
export jacocoAgentPort=8000
export projectTargetPath=$EMB_BASE/jdk_8_maven/cs/rest/original/languagetool/languagetool-server/target/
export projectPackage=org/languagetool
```

Run the service:
```
java -Djdk.attach.allowAttachSelf=true -javaagent:"${EMB_BASE}/realtime-jacoco-agent-1.0-SNAPSHOT.jar" -cp "./target/classes:$(cat cp.txt)" em.embedded.org.languagetool.EmbeddedEvoMasterController
```

The service should be running at port `8081` with jacoco agent on `8000`. Modify line 65 in `jdk_8_maven/em/embedded/rest/languagetool/src/main/java/em/embedded/org/languagetool/EmbeddedEvoMasterController.java` to change the service port.

The spec is available at `http://localhost:8081/v2/swagger`. To check if it is running, send a request:
```
curl -i http://localhost:8081/v2/languages
```

Start EvoMaster in black-box mode (change the seed 105 and the 105 in outputFolder if necessary):
```
"/usr/lib/jvm/java-8-openjdk-amd64/jre/"/bin/java -Xms1G -Xmx4G -jar evomaster.jar --blackBox true --maxTime 3600s --bbSwaggerUrl http://localhost:8081/v2/swagger --bbTargetUrl http://localhost:8081/ --seed 105 --showProgress=true --testSuiteSplitType=NONE  --outputFormat JAVA_JUNIT_4 --outputFolder "/home/rhuang329/aster-exp/bb/tests/languagetools_evomaster_bb_v2__S105_11890"
```

### Ohsome API

Please refer to [HOW_TO_RUN.md](https://github.com/aster-test-generation/ohsome-api/blob/master/HOW_TO_RUN.md) in Ohsome API repo.


### ofbiz-framework

Please refer to [HOW_TO_RUN_WITH_AGENT.md](https://github.com/aster-test-generation/ofbiz-framework/blob/trunk/HOW_TO_RUN_WITH_AGENT.md) in ofbiz-framework API repo.

### Market

#### Using original Jacco agent

Switch to `$EMB_BASE/jdk_11_maven/em/embedded/rest/market`

Run
```
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
```
to write the whole list of dependencies to `cp.txt`.

Run the service:
```
java -Djdk.attach.allowAttachSelf=true -javaagent:${JACOCO_LIB}/jacocoagent.jar=output=tcpserver,port=6300,destfile=jacoco.exec  -cp "./target/classes:$(cat cp.txt)" em.embedded.market.EmbeddedEvoMasterController
```

The service should be running at port `12345` with jacoco agent on `6300`. Modify line 69 in `jdk_11_maven/em/embedded/rest/market/src/main/java/em/embedded/market/EmbeddedEvoMasterController.java` to change the service port.

The spec is available at `http://localhost:12345/v2/api-docs/`. To check if it is running, send a request:
```
curl -i http://localhost:12345/products
```

Start EvoMaster in black-box mode (change the seed 103 and the 103 in outputFolder if necessary):
```
"/usr/lib/jvm/java-8-openjdk-amd64/jre/"/bin/java -Xms1G -Xmx4G -jar ~/aster-exp/wb1/evomaster.jar --blackBox true --maxTime 3600s --bbSwaggerUrl http://localhost:12345/v2/api-docs/  --bbTargetUrl http://localhost:12345/  --seed 103 --showProgress=true --testSuiteSplitType=NONE  --outputFormat JAVA_JUNIT_4 --outputFolder /home/rhuang329/JacocoLive/market_jacoco/market_103/
```


### Session-Service

Switch to `$EMB_BASE/jdk_8_maven/em/embedded/rest/session-service`

Run
```
mvn clean package
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
```
to write the whole list of dependencies to `cp.txt`.

Set up necessary env vars:

```
export jacocoAgentPort=8000
export projectTargetPath=$EMB_BASE/jdk_8_maven/cs/rest/original/session-service/target/
export projectPackage=org/cbioportal
```

Run the service:
```
java -Djdk.attach.allowAttachSelf=true -javaagent:"${EMB_BASE}/realtime-jacoco-agent-1.0-SNAPSHOT.jar" -cp "./target/classes:$(cat cp.txt)" em.embedded.org.cbioportal.session_service.EmbeddedEvoMasterController
```

The service should be running at port `12345` with jacoco agent on `8000`. Modify line 71 in `jdk_8_maven/em/embedded/rest/session-service/src/main/java/em/embedded/org/cbioportal/session_service/EmbeddedEvoMasterController.java` to change the service port.

The spec is available at `hhtp://localhost:12345/v2/api-docs`. To check if it is running, send a request:
```
curl -X GET "http://localhost:12345/info" -H "accept: */*"
```
or
```
curl -H "Content-Type: application/json" --user user:pass -X POST http://localhost:12345/api/sessions/test_portal/main_session --data '{"title": "my main portal session", "description": "this is an example"}'
```

Start EvoMaster in black-box mode (change the seed 100 and outputFolder if necessary):
```
"/usr/lib/jvm/java-8-openjdk-amd64/jre/"/bin/java -Xms1G -Xmx4G -jar ~/aster-exp/wb/evomaster.jar --blackBox true --maxTime 3600s --bbSwaggerUrl http://localhost:12345/v2/api-docs --bbTargetUrl http://localhost:12345 --seed 100 --showProgress=true --testSuiteSplitType=NONE  --outputFormat JAVA_JUNIT_4 --outputFolder "/home/rhuang329/aster-exp/bb/tests/session_service_evomaster_bb_v2__S100_10420"
```