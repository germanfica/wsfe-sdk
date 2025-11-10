# wsfe

The WSFE SDK is a developer-focused tool designed to simplify interactions with ARCA's SOAP API by providing a high-level abstraction layer. Its primary goal is to transform the complexity of SOAP communication into a straightforward interface, enabling developers to work with Java objects and methods instead of directly handling SOAP calls or XML manipulation.

This design eliminates the need for developers to engage with the underlying SOAP protocol or XML structures. By automatically converting SOAP responses into easy-to-use Java objects, the SDK streamlines development, allowing developers to concentrate on implementing business logic rather than managing low-level communication details.

The SDK is not intended to serve as a comprehensive SOAP API client. Instead, it prioritizes usability by abstracting the intricacies of SOAP requests and responses, removing the requirement for maintaining schema files such as .xsd or .wsdl. This approach reduces the maintenance overhead and ensures the SDK remains flexible and adaptable to API changes.

By focusing on high-level abstraction and simplifying the integration process, the SDK enhances developer productivity and facilitates efficient interaction with the ARCA SOAP API, making it a practical and developer-friendly solution.

## Requirements

- Java 17 or later

## Architecture Overview

The WSFE SDK is built on the **service-based pattern**, meaning that all API interactions happen through immutable, configurable client instances rather than through global or static methods. This approach ensures thread-safety, testability, and clean separation of concerns.

## Maven goals

```maven
mvn clean
mvn generate-sources
mvn clean generate-sources -DskipTests
```

## Install package in local Maven repository

(1) First build the package.
```bash
mvn clean package -DskipTests 
```

(2) Second install the package in maven local repository.
```bash
mvn install:install-file -Dfile=target/wsfe-0.0.1-SNAPSHOT.jar -DgroupId=com.germanfica -DartifactId=wsfe -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
```

or

```bash
mvn clean package -DskipTests install:install-file -Dfile=target/wsfe-0.0.1-SNAPSHOT.jar -DgroupId=com.germanfica -DartifactId=wsfe -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
```

(3) After that, you can add the dependency in another project:

```xml
<dependency>
  <groupId>com.germanfica</groupId>
  <artifactId>wsfe-sdk</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Docs

- https://www.mojohaus.org/jaxb2-maven-plugin/Documentation/v3.1.0/index.html

## Examples

The SDK provides two parallel example trees, one per environment:

* `com.germanfica.wsfe.examples.homo.*` — AFIP homologation (testing)
* `com.germanfica.wsfe.examples.prod.*` — AFIP production

### Why

Examples in an SDK are pedagogical assets. They are optimized for clarity and immediate execution rather than for minimizing duplication. Keeping separate examples for each environment removes the need to edit code or toggle comments when switching between homologation and production.

In other words: the value of examples lies in being **plug-and-play**. A developer returning after months, or a beginner setting up for the first time, should be able to run a homo or prod example without adjustments.

Unlike the SDK core, examples are not required to follow strict *Don't Repeat Yourself (DRY)* principles. Their primary goal is to maximize readability and accessibility, ensuring that functionality can be demonstrated in the simplest and most direct way possible.

## Known issues

When working with IntelliJ IDEA, there are occasional issues where the code and controllers may break after performing a `mvn clean` operation.

**Recommendation:**
To resolve this, use the "Reload from Disk" option to refresh the project and restore its correct state.

![9jQBmZ2UVq](https://github.com/user-attachments/assets/a51ec044-2f7a-41a0-b01a-c3bca0244265)

## Disclaimer

Este software y sus desarrolladores no tienen ninguna relación con ARCA (anteriormente AFIP). Este proyecto es una herramienta independiente desarrollada con fines educativos o funcionales y no está respaldado, aprobado ni afiliado de ninguna manera con la ARCA.
