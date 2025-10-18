package com.germanfica.wsfe.examples;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SimpleNtpExample {
    private static final String NTP_SERVER = "time.afip.gov.ar";
    private static final int NTP_PORT = 123;
    // Offset entre epoch 1900 (NTP) y 1970 (Unix) en segundos
    private static final long DIFF_1900_TO_1970 = 2208988800L;

    public static ZonedDateTime getTime() throws Exception {
        byte[] buf = new byte[48];
        buf[0] = 0x1B; // LI=0, VN=3, Mode=3 (client)

        InetAddress address = InetAddress.getByName(NTP_SERVER);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, NTP_PORT);
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(5000);
        socket.send(packet);

        // recibir respuesta
        DatagramPacket response = new DatagramPacket(buf, buf.length);
        socket.receive(response);
        socket.close();

        // El timestamp de transmit (bytes 40..47) es el tiempo del servidor
        ByteBuffer bb = ByteBuffer.wrap(response.getData());
        long seconds = Integer.toUnsignedLong(bb.getInt(40));
        long fraction = Integer.toUnsignedLong(bb.getInt(44));

        long ms = (seconds - DIFF_1900_TO_1970) * 1000L + (fraction * 1000L) / 0x100000000L;
        Instant instant = Instant.ofEpochMilli(ms);
        return ZonedDateTime.ofInstant(instant, ZoneId.of("America/Argentina/Salta"));
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Hora NTP: " + getTime());
    }
}
