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

## Install package maven local repository

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

## IntelliJ IDEA Configuration

In order for IntelliJ IDEA to recognize and compile any code generated under `generated-sources` (for example, JAXB, CXF, annotation processors, etc.), you must mark those directories as **Source Folders**.

![idea64_87xSJpD3Zl](https://github.com/user-attachments/assets/c324060f-11e0-4e38-bfc7-3e274e9ed62a)

1. **Open the Project Structure dialog**

   * From the main menu, choose **File -> Project Structure…** (or press `Ctrl + Alt + Shift + S` on Windows/Linux, `⌘ ;` on macOS).

2. **Select your module**

   * In the left-hand pane of the Project Structure dialog, click on **Modules**.
   * In the upper-center area, select the module that corresponds to your SDK (e.g. `wsfe`).

3. **Switch to the “Sources” tab**

   * With your module selected, click the **Sources** tab in the right-hand pane.

4. **Locate and mark each generated-sources folder as a Source Root**

   * In the folder tree, expand `src` (or the root of your project) until you see `generated-sources/annotations` and `generated-sources/cxf` (or whichever subfolders hold your generated classes).
   * Select each generated-sources directory one at a time, then click the blue **Sources** button (or right-click and choose **Mark Directory as -> Sources Root**).
   * Once marked, IntelliJ will highlight those folders in blue and list them under **Source Folders** on the right.

5. **Apply and save**

   * Click **Apply** and then **OK** to close the Project Structure dialog.
   * IntelliJ will now treat all code inside those `generated-sources` directories as part of the main source set, and it will compile or index them automatically.

After completing these steps, any classes or files that your build tool (Maven, Gradle, etc.) places into `target/generated-sources` (or another designated location) will be correctly recognized by IntelliJ IDEA. If you ever regenerate sources, simply refresh your project (e.g. via **Build -> Rebuild Project** or by re-importing the Maven/Gradle project) and verify that IntelliJ still lists each generated-sources folder under **Source Folders**.

## Using the SDK as a Library in IntelliJ IDEA

![PzQFdT5hT4](https://github.com/user-attachments/assets/c502c4fa-c3a7-42d9-8a82-dc271f838adc)

### Objective
To use this SDK in another project during development without the need to compile it into a JAR. This approach allows real-time updates and easy debugging.

### Benefits
- Immediate access to SDK features.
- Real-time updates when modifying the SDK.
- No need to package the SDK as a JAR.

### Step-by-Step Guide
1. **Open the project where you want to use the SDK.**
2. **Add the SDK as a library:**
   - Go to **File > Project Structure > Libraries**.
   - Click the **+** button and select **Java**.
   - Choose the root folder of the SDK project (e.g., `wsfe-spring-sdk`).
   - Include the `src/main/java` and any other relevant directories, such as `target/generated-sources`.
3. **Apply changes:**
   - Click **Apply** and **OK** to save the configuration.
4. **Start using the SDK:**
   - Import the necessary classes and start coding!

---

# Docs

- https://www.mojohaus.org/jaxb2-maven-plugin/Documentation/v3.1.0/index.html
- 

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
