package com.germanfica.wsfe.service;

import java.util.Set;

public interface DtoConversion<T,DTO> {
    // == utils ==
    Set<DTO> convertToDto(Iterable<T> objects);
    DTO convertToDto(T object);
}
