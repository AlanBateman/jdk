/*
 * Copyright (c) 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @run junit Uninterruptible
 * @run junit/othervm -Djdk.nio.channels.FileChannel.closeOnInterrupt=true Uninterruptible
 * @run junit/othervm -Djdk.nio.channels.FileChannel.closeOnInterrupt=false Uninterruptible
 */

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;

class Uninterruptible {
    private static boolean closeOnInterrupt;

    @BeforeAll
    static void setup() {
        closeOnInterrupt = Boolean.getBoolean("jdk.nio.channels.FileChannel.closeOnInterrupt");
    }

    /**
     * Test that Files.newInputStream returns an InputStream that is not closed by interrupt.
     */
    @Test
    void testInputStream() throws IOException {
        Path file = createTempFile(10);
        try (InputStream in = Files.newInputStream(file)) {
            Thread.currentThread().interrupt();
            try {
                int n = in.read(new byte[100]);
                assertTrue(n > 0);
            } finally {
                Thread.interrupted(); // clear interrupt status
            }
        } finally {
            Files.delete(file);
        }
    }

    /**
     * Test that Files.newByteChannel returns a SeekableByteChannel that is not closed by
     * interrupt.
     */
    @Test
    void testByteChannel() throws IOException {
        Path file = createTempFile(10);
        try (SeekableByteChannel ch = Files.newByteChannel(file)) {
            ByteBuffer bb = ByteBuffer.allocate(100);
            Thread.currentThread().interrupt();
            try {
                int n = ch.read(bb);
                assertTrue(n > 0);
            } finally {
                Thread.interrupted(); // clear interrupt status
            }
        } finally {
            Files.delete(file);
        }
    }

    /**
     * Test that FileChannel.open returns a FileChannel that is not closed by
     * interrupt, unless configured by the system property.
     */
    @Test
    void testFileChannel() throws IOException {
        Path file = createTempFile(10);
        try (FileChannel fc = FileChannel.open(file)) {
            ByteBuffer bb = ByteBuffer.allocate(100);
            Thread.currentThread().interrupt();
            try {
                int n = fc.read(bb);
                assertFalse(closeOnInterrupt);
                assertTrue(fc.isOpen());
                assertTrue(n > 0);
            } catch (ClosedByInterruptException e) {
                assertTrue(closeOnInterrupt);
                assertFalse(fc.isOpen());
            } finally {
                Thread.interrupted(); // clear interrupt status
            }
        } finally {
            Files.delete(file);
        }
    }

    /**
     * Test that FileInputStream.getChannel returns a FileChannel that is not closed by
     * interrupt, unless configured by the system property.
     */
    @Test
    void testGetChannel() throws IOException {
        Path file = createTempFile(10);
        try (var fis = new FileInputStream(file.toFile())) {
            FileChannel fc = fis.getChannel();
            ByteBuffer bb = ByteBuffer.allocate(100);
            Thread.currentThread().interrupt();
            try {
                int n = fc.read(bb);
                assertFalse(closeOnInterrupt);
                assertTrue(fc.isOpen());
                assertTrue(n > 0);
            } catch (ClosedByInterruptException e) {
                assertTrue(closeOnInterrupt);
                assertFalse(fc.isOpen());
            } finally {
                Thread.interrupted(); // clear interrupt status
            }
        } finally {
            Files.delete(file);
        }
    }

    private Path createTempFile(int size) throws IOException {
        Path file = Files.createTempFile(Path.of("."), "foo", ".tmp");
        if (size > 0) {
            Files.write(file, new byte[size]);
        }
        return file;
    }
}