package com.zip.tool;

public class Deflater {
    private long strm;
    private byte[] buf;
    private int off;
    private int len;
    private int level;
    private int strategy;
    private boolean setParams;
    private boolean finish;
    private boolean finished;
    public static final int DEFLATED = 8;
    public static final int NO_COMPRESSION = 0;
    public static final int BEST_SPEED = 1;
    public static final int BEST_COMPRESSION = 9;
    public static final int DEFAULT_COMPRESSION = -1;
    public static final int FILTERED = 1;
    public static final int HUFFMAN_ONLY = 2;
    public static final int DEFAULT_STRATEGY = 0;

    static {
        initIDs();
    }

    public Deflater(int level, boolean nowrap) {
        this.buf = new byte[0];
        this.level = level;
        this.strategy = 0;
        this.strm = init(level, 0, nowrap);
    }

    public Deflater(int level) {
        this(level, false);
    }

    public Deflater() {
        this(-1, false);
    }

    public synchronized void setInput(byte[] b, int off, int len) {
        if(b == null) {
            throw new NullPointerException();
        } else if(off >= 0 && len >= 0 && off <= b.length - len) {
            this.buf = b;
            this.off = off;
            this.len = len;
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public void setInput(byte[] b) {
        this.setInput(b, 0, b.length);
    }

    public synchronized void setDictionary(byte[] b, int off, int len) {
        if(this.strm != 0L && b != null) {
            if(off >= 0 && len >= 0 && off <= b.length - len) {
                setDictionary(this.strm, b, off, len);
            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
        } else {
            throw new NullPointerException();
        }
    }

    public void setDictionary(byte[] b) {
        this.setDictionary(b, 0, b.length);
    }

    public synchronized void setStrategy(int strategy) {
        switch(strategy) {
        case 0:
        case 1:
        case 2:
            if(this.strategy != strategy) {
                this.strategy = strategy;
                this.setParams = true;
            }

            return;
        default:
            throw new IllegalArgumentException();
        }
    }

    public synchronized void setLevel(int level) {
        if((level < 0 || level > 9) && level != -1) {
            throw new IllegalArgumentException("invalid compression level");
        } else {
            if(this.level != level) {
                this.level = level;
                this.setParams = true;
            }

        }
    }

    public boolean needsInput() {
        return this.len <= 0;
    }

    public synchronized void finish() {
        this.finish = true;
    }

    public synchronized boolean finished() {
        return this.finished;
    }

    public synchronized int deflate(byte[] b, int off, int len) {
        if(b == null) {
            throw new NullPointerException();
        } else if(off >= 0 && len >= 0 && off <= b.length - len) {
            return this.deflateBytes(b, off, len);
        } else {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public int deflate(byte[] b) {
        return this.deflate(b, 0, b.length);
    }

    public synchronized int getAdler() {
        this.ensureOpen();
        return getAdler(this.strm);
    }

    public int getTotalIn() {
        return (int)this.getBytesRead();
    }

    public synchronized long getBytesRead() {
        this.ensureOpen();
        return getBytesRead(this.strm);
    }

    public int getTotalOut() {
        return (int)this.getBytesWritten();
    }

    public synchronized long getBytesWritten() {
        this.ensureOpen();
        return getBytesWritten(this.strm);
    }

    public synchronized void reset() {
        this.ensureOpen();
        reset(this.strm);
        this.finish = false;
        this.finished = false;
        this.off = this.len = 0;
    }

    public synchronized void end() {
        if(this.strm != 0L) {
            end(this.strm);
            this.strm = 0L;
            this.buf = null;
        }

    }

    protected void finalize() {
        this.end();
    }

    private void ensureOpen() {
        if(this.strm == 0L) {
            throw new NullPointerException();
        }
    }

    private static native void initIDs();

    private static native long init(int var0, int var1, boolean var2);

    private static native void setDictionary(long var0, byte[] var2, int var3, int var4);

    private native int deflateBytes(byte[] var1, int var2, int var3);

    private static native int getAdler(long var0);

    private static native long getBytesRead(long var0);

    private static native long getBytesWritten(long var0);

    private static native void reset(long var0);

    private static native void end(long var0);
}
