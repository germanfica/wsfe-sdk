package com.germanfica.wsfe.examples;

import com.germanfica.wsfe.cms.Cms;
import com.germanfica.wsfe.util.CMSSignedInspector;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class InspectSignedCmsBase64Example {
    private static final Properties properties = new Properties();
    public static void main(String[] args) {
        try {
            try (FileInputStream input = new FileInputStream("src/main/resources/application.properties")) {
                properties.load(input);
            } catch (IOException e) {
                throw new RuntimeException("Error al cargar el archivo de configuración", e);
            }

            // 1) Armar CMS para WSAA
            String signedCmsBase64 = properties.getProperty("wsaa.cms.signed-cms-base64");
            Cms cms = Cms.create(signedCmsBase64);

            CMSSignedInspector cmsSignedInspector = new CMSSignedInspector();
            CMSSignedInspector.CmsTimestamps cmsTimestamps = cmsSignedInspector.inspect(signedCmsBase64);

            System.out.println("signingTime: " + cmsTimestamps.signingTime().toString());
            System.out.println("generationTime: "+ cmsTimestamps.generationTime().toString());
            System.out.println("cms certNotBefore:" + cmsTimestamps.certNotBefore().toString());
            System.out.println("cms certNotAfter" + cmsTimestamps.certNotAfter().toString());

        } catch (Exception e) {
            System.err.println("Error desconocido: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
