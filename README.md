# Gurukulams Engine

Core Engine for Gurukulams

## Setup

1. You need [JDK 23 or higher](https://jdk.java.net/) running in your machibe
2. Download the [latest release](https://github.com/gurukulams/engine/releases)
3. Run below command

```shell
java -jar engine-*.jar
```

## Development

Set JAVA_HOME variable pointing to [JDK 23 or higher](https://jdk.java.net/). 

### Build

~~~
./mvnw clean package
~~~

### Load Questions

~~~
./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.seed.folder=../11th-botany"
~~~

### Exclude Upgrades

We need dependencies to be excluded at `.mvn/jvm.config`

```shell
-Dexcludes=org.springframework:*
```
