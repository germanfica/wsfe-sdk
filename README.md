# wsfe

## Maven goals

```maven
mvn clean
mvn jaxb2:xjc
```

## 

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

## Known issues

When working with IntelliJ IDEA, there are occasional issues where the code and controllers may break after performing a `mvn clean` operation.

**Recommendation:**
To resolve this, use the "Reload from Disk" option to refresh the project and restore its correct state.

![9jQBmZ2UVq](https://github.com/user-attachments/assets/a51ec044-2f7a-41a0-b01a-c3bca0244265)

## Disclaimer

Este software y sus desarrolladores no tienen ninguna relación con ARCA (anteriormente AFIP). Este proyecto es una herramienta independiente desarrollada con fines educativos o funcionales y no está respaldado, aprobado ni afiliado de ninguna manera con la ARCA.
