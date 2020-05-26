package org.jf.dexlib2.writer.io;

import com.google.common.io.ByteStreams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;

/**
 * A deferred output stream that uses a file as its backing store, with a in-memory intermediate buffer.
 */
public class FileDeferredOutputStream extends DeferredOutputStream {
    private static final int DEFAULT_BUFFER_SIZE = 4 * 1024;

    @Nonnull private final File backingFile;
    @Nonnull private final NakedBufferedOutputStream output;
    private int writtenBytes;

    public FileDeferredOutputStream(final @Nonnull File backingFile) throws FileNotFoundException {
        this(backingFile, DEFAULT_BUFFER_SIZE);
    }

    public FileDeferredOutputStream(final @Nonnull File backingFile, final int bufferSize) throws FileNotFoundException {
        this.backingFile = backingFile;
        output = new NakedBufferedOutputStream(new FileOutputStream(backingFile), bufferSize);
    }

    @Override public void writeTo(final @Nonnull OutputStream dest) throws IOException {
        byte[] outBuf = output.getBuffer();
        int count = output.getCount();
        output.resetBuffer();
        output.close();

        // did we actually write something out to disk?
        if (count != writtenBytes) {
            InputStream fis = new FileInputStream(backingFile);
            ByteStreams.copy(fis, dest);
            backingFile.delete();
        }

        dest.write(outBuf, 0, count);
    }

    @Override public void write(final int i) throws IOException {
        output.write(i);
        writtenBytes++;
    }

    @Override public void write(final byte[] bytes) throws IOException {
        output.write(bytes);
        writtenBytes += bytes.length;
    }

    @Override public void write(final byte[] bytes, final int off, final int len) throws IOException {
        output.write(bytes, off, len);
        writtenBytes += len;
    }

    @Override public void flush() throws IOException {
        output.flush();
    }

    @Override public void close() throws IOException {
        output.close();
    }

    private static class NakedBufferedOutputStream extends BufferedOutputStream {
        public NakedBufferedOutputStream(final OutputStream outputStream) {
            super(outputStream);
        }

        public NakedBufferedOutputStream(final OutputStream outputStream, final int i) {
            super(outputStream, i);
        }

        public int getCount() {
            return count;
        }

        public void resetBuffer() {
            count = 0;
        }

        public byte[] getBuffer() {
            return buf;
        }
    }

    @Nonnull
    public static DeferredOutputStreamFactory getFactory(final @Nullable File containingDirectory) {
        return getFactory(containingDirectory, DEFAULT_BUFFER_SIZE);
    }

    @Nonnull
    public static DeferredOutputStreamFactory getFactory(@Nullable final File containingDirectory,
                                                         final int bufferSize) {
        return new DeferredOutputStreamFactory() {
            @Override public DeferredOutputStream makeDeferredOutputStream() throws IOException {
                File tempFile = File.createTempFile("dexlibtmp", null, containingDirectory);
                return new FileDeferredOutputStream(tempFile, bufferSize);
            }
        };
    }
}
