package com.germanfica.wsfe.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Parser y contenedor ultra-ligero para archivos <code>.ini</code>.
 *
 * • Sin dependencias externas.
 * • Respeta el orden original de secciones y claves (LinkedHashMap).
 * • No altera comentarios ni espacios: los rebobina tal cual al guardar.
 *
 * <pre>
 * [default]
 * token = 012345
 * sign  = ABCDEF
 *
 * [meta]
 * cuit = 20304050607
 * </pre>
 *
 * Uso rápido:
 * <pre>{@code
 * Path iniPath = Paths.get(System.getProperty("user.home"), ".wsfe", "ta.ini");
 * SimpleIni ini = SimpleIni.load(iniPath);
 *
 * String token = ini.get("default", "token");
 * ini.put("meta", "expirationTime", "2025-06-02T12:00:00-03:00");
 * ini.save(iniPath);                         // sobrescribe
 * }</pre>
 */
public final class SimpleIni {

    /** Secciones -> (clave -> valor) –  LinkedHashMap para preservar orden. */
    private final Map<String, Map<String, String>> data = new LinkedHashMap<>();

    /* ----------- API pública ----------- */

    /** Devuelve el valor o <code>null</code> si no existe. */
    public String get(String section, String key) {
        Map<String, String> sec = data.getOrDefault(normalize(section), null);
        return sec != null ? sec.get(key) : null;
    }

    /** Inserta o reemplaza <code>key=value</code>. */
    public void put(String section, String key, String value) {
        data.computeIfAbsent(normalize(section), s -> new LinkedHashMap<>())
            .put(Objects.requireNonNull(key), Objects.requireNonNull(value));
    }

    /** Carga un archivo <code>.ini</code> desde disco.  Si no existe, devuelve instancia vacía. */
    public static SimpleIni load(Path path) throws IOException {
        SimpleIni ini = new SimpleIni();
        if (!Files.exists(path)) return ini;

        String currentSection = "";
        for (String raw : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String line = raw.stripTrailing();                   // dejamos espacios iniciales tal cual
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) {
                ini.storeRaw(line);                              // comentario / línea en blanco
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {     // [seccion]
                currentSection = line.substring(1, line.length() - 1);
                ini.storeRaw(line);
                continue;
            }
            int eq = line.indexOf('=');
            if (eq > 0) {
                String key   = line.substring(0, eq).trim();     // key    (trim derivado de convención ini)
                String value = line.substring(eq + 1);           // value  (sin trim – **no normalizamos**)
                ini.put(currentSection, key, value);
                ini.storeRaw(line);
            } else {
                ini.storeRaw(line);                              // línea extraña: conservar
            }
        }
        return ini;
    }

    /** Guarda el contenido tal cual se leyó/modificó, preservando comentarios y formato. */
    public void save(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, render(), StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /* ----------- Internals para conservar líneas originales ----------- */

    private final java.util.List<String> rawLines = new java.util.ArrayList<>();

    private void storeRaw(String line) {
        rawLines.add(line);
    }

    /** Reconstruye el archivo; agrega pares nuevos al final de su sección. */
    private String render() {
        StringBuilder out = new StringBuilder();
        java.util.Set<String> rendered = new java.util.HashSet<>();

        String currentSection = "";
        for (String line : rawLines) {
            if (isSection(line)) currentSection = line.substring(1, line.length() - 1);
            if (isKeyValue(line)) {
                String key = line.substring(0, line.indexOf('=')).trim();
                rendered.add(sig(currentSection, key));
                String value = data.getOrDefault(normalize(currentSection), Map.of()).get(key);
                line = key + "=" + (value == null ? "" : value);
            }
            out.append(line).append('\n');
        }

        /* Escribe keys nuevas no presentes en rawLines */
        data.forEach((section, kv) -> kv.forEach((k, v) -> {
            if (!rendered.contains(sig(section, k))) {
                if (!section.isEmpty() && !out.toString().contains("[" + section + "]\n")) {
                    out.append("[").append(section).append("]\n");
                }
                out.append(k).append("=").append(v).append('\n');
            }
        }));
        return out.toString();
    }

    private static boolean isSection(String l)   { return l.startsWith("[") && l.endsWith("]"); }
    private static boolean isKeyValue(String l)  { return l.contains("=") && !isSection(l); }
    private static String  normalize(String s)   { return s == null ? "" : s; }
    private static String  sig(String s, String k) { return normalize(s) + "§" + k; }
}
