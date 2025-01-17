package com.germanfica.wsfe.net;

import static com.germanfica.wsfe.utils.ArcaWSAAUtils.convertXmlToObject;

public abstract class ApiResource {
    /**
     * Método genérico para mapear un XML a un DTO.
     *
     * @param xml El XML como cadena.
     * @param clazz La clase del tipo objetivo.
     * @param <T> Tipo genérico.
     * @return Instancia del DTO mapeada desde el XML.
     * @throws Exception Si ocurre un error durante el mapeo.
     */
    public static <T> T mapToDto(String xml, Class<T> clazz) throws Exception {
        try {
            return convertXmlToObject(xml, clazz);
        } catch (Exception e) {
            throw new Exception("Error al mapear XML a DTO: " + e.getMessage(), e);
        }
    }
}
