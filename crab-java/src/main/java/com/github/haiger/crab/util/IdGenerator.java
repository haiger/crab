package com.github.haiger.crab.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.atomic.LongAdder;

/**
 *
 * 这是MongoDB的ObjectID的实现方式，由时间戳+机器标识+进程标识+自增数来构造全局唯一字符串。
 * 这样的ID既能保证全局唯一，也能做到局部有序。
 *
 * @author Haiger
 * @version $Id: IdGenerator.java, v 0.1 2018-01-11 16:09:49 Haiger Exp $
 */
public class IdGenerator implements Serializable {
    private static final long serialVersionUID = -6932971550222015607L;

    static final Logger LOG = LoggerFactory.getLogger(IdGenerator.class);

    private static final int LOW_ORDER_THREE_BYTES = 0x00ffffff;

    private static final String MACHINE_IP;
    private static final int    MACHINE_IDENTIFIER;
    private static final short  PROCESS_IDENTIFIER;
    private static final LongAdder NEXT_COUNTER = new LongAdder();

    private static final char[] HEX_CHARS = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private final int   timestamp;
    private final int   machineIdentifier;
    private final short processIdentifier;
    private final int   counter;

    public static IdGenerator get() {
        return new IdGenerator();
    }

    public static String id() {
        return (new IdGenerator()).toHexString();
    }

    private IdGenerator() {
        this.timestamp = (int) (new Date().getTime() / 1000);
        this.machineIdentifier = MACHINE_IDENTIFIER;
        this.processIdentifier = PROCESS_IDENTIFIER;
        NEXT_COUNTER.increment();
        this.counter = NEXT_COUNTER.intValue() & LOW_ORDER_THREE_BYTES;
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        putToByteBuffer(buffer);
        return buffer.array(); // using .allocate ensures there is a backing array that can be returned
    }

    public void putToByteBuffer(final ByteBuffer buffer) {
        buffer.put(int3(timestamp));
        buffer.put(int2(timestamp));
        buffer.put(int1(timestamp));
        buffer.put(int0(timestamp));

        buffer.put(int2(machineIdentifier));
        buffer.put(int1(machineIdentifier));
        buffer.put(int0(machineIdentifier));

        buffer.put(short1(processIdentifier));
        buffer.put(short0(processIdentifier));

        buffer.put(int2(counter));
        buffer.put(int1(counter));
        buffer.put(int0(counter));
    }

    public String toHexString() {
        char[] chars = new char[24];
        int i = 0;
        for (byte b : toByteArray()) {
            chars[i++] = HEX_CHARS[b >> 4 & 0xF];
            chars[i++] = HEX_CHARS[b & 0xF];
        }
        return new String(chars);
    }

    @Override
    public String toString() {
        return toHexString();
    }

    public String getMachineIp() {
        return MACHINE_IP;
    }

    static {
        try {
            MACHINE_IP = createMachineIp();
            MACHINE_IDENTIFIER = createMachineIdentifier();
            PROCESS_IDENTIFIER = createProcessIdentifier();
            NEXT_COUNTER.add(new SecureRandom().nextInt());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String createMachineIp() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                for (InterfaceAddress address : ni.getInterfaceAddresses()) {
                    if (address.getAddress() instanceof Inet4Address) {
                        Inet4Address inet4Address = (Inet4Address) address.getAddress();
                        if (!inet4Address.isLoopbackAddress()) {
                            ip = inet4Address.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // do nothing
        }
        return ip;
    }

    private static int createMachineIdentifier() {
        // build a 2-byte machine piece based on NICs info
        int machinePiece;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                sb.append(ni.toString());
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    ByteBuffer bb = ByteBuffer.wrap(mac);
                    try {
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                        sb.append(bb.getChar());
                    } catch (BufferUnderflowException shortHardwareAddressException) { //NOPMD
                        // mac with less than 6 bytes. continue
                    }
                }
            }
            machinePiece = sb.toString().hashCode();
        } catch (Throwable t) {
            // exception sometimes happens with IBM JVM, use random
            machinePiece = (new SecureRandom().nextInt());
            LOG.error("Failed to get machine identifier from network interface, using random number instead", t);
        }
        machinePiece = machinePiece & LOW_ORDER_THREE_BYTES;
        return machinePiece;
    }

    // Creates the process identifier.  This does not have to be unique per class loader because
    // NEXT_COUNTER will provide the uniqueness.
    private static short createProcessIdentifier() {
        short processId;
        try {
            String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
            if (processName.contains("@")) {
                processId = (short) Integer.parseInt(processName.substring(0, processName.indexOf('@')));
            } else {
                processId = (short) java.lang.management.ManagementFactory.getRuntimeMXBean().getName().hashCode();
            }

        } catch (Throwable t) {
            processId = (short) new SecureRandom().nextInt();
            LOG.error("Failed to get process identifier from JMX, using random number instead", t);
        }

        return processId;
    }

    private static byte int3(final int x) {
        return (byte) (x >> 24);
    }

    private static byte int2(final int x) {
        return (byte) (x >> 16);
    }

    private static byte int1(final int x) {
        return (byte) (x >> 8);
    }

    private static byte int0(final int x) {
        return (byte) (x);
    }

    private static byte short1(final short x) {
        return (byte) (x >> 8);
    }

    private static byte short0(final short x) {
        return (byte) (x);
    }
}
