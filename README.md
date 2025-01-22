# wsfe

The WSFE SDK is a developer-focused tool designed to simplify interactions with ARCA's SOAP API by providing a high-level abstraction layer. Its primary goal is to transform the complexity of SOAP communication into a straightforward interface, enabling developers to work with Java objects and methods instead of directly handling SOAP calls or XML manipulation.

This design eliminates the need for developers to engage with the underlying SOAP protocol or XML structures. By automatically converting SOAP responses into easy-to-use Java objects, the SDK streamlines development, allowing developers to concentrate on implementing business logic rather than managing low-level communication details.

The SDK is not intended to serve as a comprehensive SOAP API client. Instead, it prioritizes usability by abstracting the intricacies of SOAP requests and responses, removing the requirement for maintaining schema files such as .xsd or .wsdl. This approach reduces the maintenance overhead and ensures the SDK remains flexible and adaptable to API changes.

By focusing on high-level abstraction and simplifying the integration process, the SDK enhances developer productivity and facilitates efficient interaction with the ARCA SOAP API, making it a practical and developer-friendly solution.

## Maven goals

```maven
mvn clean
mvn jaxb2:xjc
```

# Docs

- https://www.mojohaus.org/jaxb2-maven-plugin/Documentation/v3.1.0/index.html
- 

## Known issues

When working with IntelliJ IDEA, there are occasional issues where the code and controllers may break after performing a `mvn clean` operation.

**Recommendation:**
To resolve this, use the "Reload from Disk" option to refresh the project and restore its correct state.

![9jQBmZ2UVq](https://github.com/user-attachments/assets/a51ec044-2f7a-41a0-b01a-c3bca0244265)

## Disclaimer

Este software y sus desarrolladores no tienen ninguna relación con ARCA (anteriormente AFIP). Este proyecto es una herramienta independiente desarrollada con fines educativos o funcionales y no está respaldado, aprobado ni afiliado de ninguna manera con la ARCA.
