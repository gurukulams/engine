# Gurukulams Engine

Core Engine for Gurukulams

## Development

Set JAVA_HOME variable pointing to [JDK 21 or higher](https://jdk.java.net/). 

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
