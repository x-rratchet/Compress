package com.zip.tool;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

public class InflaterInputStream extends FilterInputStream {
    protected Inflater inf;
    protected byte[] buf;
    protected int len;
    private boolean closed;
    private boolean reachEOF;
    boolean usesDefaultInflater;
    private byte[] singleByteBuf;
    private byte[] b;

    private void ensureOpen() throws IOException {
        if(this.closed) {
            throw new IOException("Stream closed");
        }
    }

    public InflaterInputStream(InputStream in, Inflater inf, int size) {
        super(in);
        this.closed = false;
        this.reachEOF = false;
        this.usesDefaultInflater = false;
        this.singleByteBuf = new byte[1];
        this.b = new byte[512];
        if(in != null && inf != null) {
            if(size <= 0) {
                throw new IllegalArgumentException("buffer size <= 0");
            } else {
                this.inf = inf;
                this.buf = new byte[size];
            }
        } else {
            throw new NullPointerException();
        }
    }

    public InflaterInputStream(InputStream in, Inflater inf) {
        this(in, inf, 512);
    }

    public InflaterInputStream(InputStream in) {
        this(in, new Inflater());
        this.usesDefaultInflater = true;
    }

    public int read() throws IOException {
        this.ensureOpen();
        return this.read(this.singleByteBuf, 0, 1) == -1?-1:this.singleByteBuf[0] & 255;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        this.ensureOpen();
        if((off | len | off + len | b.length - (off + len)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if(len == 0) {
            return 0;
        } else {
            try {
                int e;
                while((e = this.inf.inflate(b, off, len)) == 0) {
                    if(this.inf.finished() || this.inf.needsDictionary()) {
                        this.reachEOF = true;
                        return -1;
                    }

                    if(this.inf.needsInput()) {
                        this.fill();
                    }
                }

                return e;
            } catch (DataFormatException var6) {
                String s = var6.getMessage();
                throw new ZipException(s != null?s:"Invalid ZLIB data format");
            }
        }
    }

    public int available() throws IOException {
        this.ensureOpen();
        return this.reachEOF?0:1;
    }

    public long skip(long n) throws IOException {
        if(n < 0L) {
            throw new IllegalArgumentException("negative skip length");
        } else {
            this.ensureOpen();
            int max = (int)Math.min(n, 2147483647L);

            int total;
            int len;
            for(total = 0; total < max; total += len) {
                len = max - total;
                if(len > this.b.length) {
                    len = this.b.length;
                }

                len = this.read(this.b, 0, len);
                if(len == -1) {
                    this.reachEOF = true;
                    break;
                }
            }

            return (long)total;
        }
    }

    public void close() throws IOException {
        if(!this.closed) {
            if(this.usesDefaultInflater) {
                this.inf.end();
            }

            this.in.close();
            this.closed = true;
        }

    }

    protected void fill() throws IOException {
        this.ensureOpen();
        this.len = this.in.read(this.buf, 0, this.buf.length);
        if(this.len == -1) {
            throw new EOFException("Unexpected end of ZLIB input stream");
        } else {
            this.inf.setInput(this.buf, 0, this.len);
        }
    }

    public boolean markSupported() {
        return false;
    }

    public synchronized void mark(int readlimit) {
    }

    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
}
