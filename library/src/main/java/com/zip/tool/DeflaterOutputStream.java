package com.zip.tool;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DeflaterOutputStream extends FilterOutputStream {
    protected Deflater def;
    protected byte[] buf;
    private boolean closed;
    boolean usesDefaultDeflater;

    public DeflaterOutputStream(OutputStream out, Deflater def, int size) {
        super(out);
        this.closed = false;
        this.usesDefaultDeflater = false;
        if(out != null && def != null) {
            if(size <= 0) {
                throw new IllegalArgumentException("buffer size <= 0");
            } else {
                this.def = def;
                this.buf = new byte[size];
            }
        } else {
            throw new NullPointerException();
        }
    }

    public DeflaterOutputStream(OutputStream out, Deflater def) {
        this(out, def, 512);
    }

    public DeflaterOutputStream(OutputStream out) {
        this(out, new Deflater());
        this.usesDefaultDeflater = true;
    }

    public void write(int b) throws IOException {
        byte[] buf = new byte[]{(byte)(b & 255)};
        this.write(buf, 0, 1);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if(this.def.finished()) {
            throw new IOException("write beyond end of stream");
        } else if((off | len | off + len | b.length - (off + len)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if(len != 0) {
            if(!this.def.finished()) {
                int stride = this.buf.length;

                for(int i = 0; i < len; i += stride) {
                    this.def.setInput(b, off + i, Math.min(stride, len - i));

                    while(!this.def.needsInput()) {
                        this.deflate();
                    }
                }
            }

        }
    }

    public void finish() throws IOException {
        if(!this.def.finished()) {
            this.def.finish();

            while(!this.def.finished()) {
                this.deflate();
            }
        }

    }

    public void close() throws IOException {
        if(!this.closed) {
            this.finish();
            if(this.usesDefaultDeflater) {
                this.def.end();
            }

            this.out.close();
            this.closed = true;
        }

    }

    protected void deflate() throws IOException {
        int len = this.def.deflate(this.buf, 0, this.buf.length);
        if(len > 0) {
            this.out.write(this.buf, 0, len);
        }

    }
}
