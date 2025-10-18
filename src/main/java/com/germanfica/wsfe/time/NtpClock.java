package com.germanfica.wsfe.time;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * A {@link Clock} implementation that queries an NTP server on each call to {@link #instant()}.
 * This clock does not cache responses and compensates network latency using the
 * standard four-timestamp exchange defined in RFC 5905 (Network Time Protocol Version 4).
 *
 * <p>If the NTP query fails for any reason, the clock falls back to the provided
 * {@code fallbackClock} to ensure time continuity.</p>
 */
public class NtpClock extends Clock {
    private final String server;
    private final Clock fallbackClock;

    // NTP constants
    private static final int NTP_PORT = 123;
    private static final long DIFF_1900_TO_1970 = 2208988800L; // segundos

    public NtpClock(String server, Clock fallbackClock) {
        this.server = server;
        this.fallbackClock = fallbackClock == null ? Clock.systemDefaultZone() : fallbackClock;
    }

    @Override
    public ZoneId getZone() {
        return fallbackClock.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new NtpClock(server, Clock.system(zone));
    }

    @Override
    public Instant instant() {
        try {
            long offset = queryServerOffsetMillis(server);
            long corrected = System.currentTimeMillis() + offset;
            return Instant.ofEpochMilli(corrected);
        } catch (Exception e) {
            // Fallback: si falla la consulta NTP devolvemos la hora del fallbackClock
            return fallbackClock.instant();
        }
    }

    /**
     * Performs a single NTP query and computes the time offset using the
     * four timestamps defined in RFC 5905, Section 8:
     *
     * <pre>
     * offset = ((t2 - t1) + (t3 - t4)) / 2
     * </pre>
     *
     * where:
     *  - t1 = client transmit time (before sending request)
     *  - t2 = server receive time (when request arrived)
     *  - t3 = server transmit time (when reply sent)
     *  - t4 = client receive time (after receiving reply)
     *
     * The result represents the difference (server - local) in milliseconds.
     *
     * @param server the hostname or IP address of the NTP server
     * @return the offset in milliseconds to adjust local time to the server time
     * @throws Exception if any I/O or network error occurs
     */
    private long queryServerOffsetMillis(String server) throws Exception {
        byte[] buf = new byte[48];
        buf[0] = 0x1B; // LI=0 VN=3 Mode=3 (client)

        InetAddress address = InetAddress.getByName(server);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, NTP_PORT);

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3000);

            long t1 = System.currentTimeMillis();
            socket.send(packet);

            DatagramPacket response = new DatagramPacket(buf, buf.length);
            socket.receive(response);
            long t4 = System.currentTimeMillis();

            ByteBuffer bb = ByteBuffer.wrap(response.getData());

            // t2: bytes 32..39 (receive timestamp)
            long secondsT2 = Integer.toUnsignedLong(bb.getInt(32));
            long fractionT2 = Integer.toUnsignedLong(bb.getInt(36));
            long t2Millis = (secondsT2 - DIFF_1900_TO_1970) * 1000L
                + (fractionT2 * 1000L) / 0x100000000L;

            // t3: bytes 40..47 (transmit timestamp)
            long secondsT3 = Integer.toUnsignedLong(bb.getInt(40));
            long fractionT3 = Integer.toUnsignedLong(bb.getInt(44));
            long t3Millis = (secondsT3 - DIFF_1900_TO_1970) * 1000L
                + (fractionT3 * 1000L) / 0x100000000L;

            // RFC 5905 offset calculation (ms)
            long offset = ( (t2Millis - t1) + (t3Millis - t4) ) / 2L;

            return offset;
        }
    }
}
