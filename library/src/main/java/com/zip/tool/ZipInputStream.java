package com.zip.tool;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

public class ZipInputStream extends InflaterInputStream implements ZipConstants {
    private ZipEntry entry;
    private CRC32 crc = new CRC32();
    private long remaining;
    private byte[] tmpbuf = new byte[512];
    private static final int STORED = 0;
    private static final int DEFLATED = 8;
    private boolean closed = false;
    private boolean entryEOF = false;
    private byte[] b = new byte[256];

    private void ensureOpen() throws IOException {
        if(this.closed) {
            throw new IOException("Stream closed");
        }
    }

    public ZipInputStream(InputStream in) {
        super(new PushbackInputStream(in, 512), new Inflater(true), 512);
        this.usesDefaultInflater = true;
        if(in == null) {
            throw new NullPointerException("in is null");
        }
    }

    public ZipEntry getNextEntry() throws IOException {
        this.ensureOpen();
        if(this.entry != null) {
            this.closeEntry();
        }

        this.crc.reset();
        this.inf.reset();
        if((this.entry = this.readLOC()) == null) {
            return null;
        } else {
            if(this.entry.method == 0) {
                this.remaining = this.entry.size;
            }

            this.entryEOF = false;
            return this.entry;
        }
    }

    public void closeEntry() throws IOException {
        this.ensureOpen();

        while(this.read(this.tmpbuf, 0, this.tmpbuf.length) != -1) {
            ;
        }

        this.entryEOF = true;
    }

    public int available() throws IOException {
        this.ensureOpen();
        return this.entryEOF?0:1;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        this.ensureOpen();
        if(off >= 0 && len >= 0 && off <= b.length - len) {
            if(len == 0) {
                return 0;
            } else if(this.entry == null) {
                return -1;
            } else {
                switch(this.entry.method) {
                case 0:
                    if(this.remaining <= 0L) {
                        this.entryEOF = true;
                        this.entry = null;
                        return -1;
                    } else {
                        if((long)len > this.remaining) {
                            len = (int)this.remaining;
                        }

                        len = this.in.read(b, off, len);
                        if(len == -1) {
                            throw new ZipException("unexpected EOF");
                        }

                        this.crc.update(b, off, len);
                        this.remaining -= (long)len;
                        return len;
                    }
                case 8:
                    len = super.read(b, off, len);
                    if(len == -1) {
                        this.readEnd(this.entry);
                        this.entryEOF = true;
                        this.entry = null;
                    } else {
                        this.crc.update(b, off, len);
                    }

                    return len;
                default:
                    throw new InternalError("invalid compression method");
                }
            }
        } else {
            throw new IndexOutOfBoundsException();
        }
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
                if(len > this.tmpbuf.length) {
                    len = this.tmpbuf.length;
                }

                len = this.read(this.tmpbuf, 0, len);
                if(len == -1) {
                    this.entryEOF = true;
                    break;
                }
            }

            return (long)total;
        }
    }

    public void close() throws IOException {
        if(!this.closed) {
            super.close();
            this.closed = true;
        }

    }

    private ZipEntry readLOC() throws IOException {
        try {
            this.readFully(this.tmpbuf, 0, 30);
        } catch (EOFException var5) {
            return null;
        }

        if(get32(this.tmpbuf, 0) != 67324752L) {
            return null;
        } else {
            int len = get16(this.tmpbuf, 26);
            if(len == 0) {
                throw new ZipException("missing entry name");
            } else {
                int blen = this.b.length;
                if(len > blen) {
                    do {
                        blen *= 2;
                    } while(len > blen);

                    this.b = new byte[blen];
                }

                this.readFully(this.b, 0, len);
                ZipEntry e = this.createZipEntry(getUTF8String(this.b, 0, len));
                e.version = get16(this.tmpbuf, 4);
                e.flag = get16(this.tmpbuf, 6);
                if((e.flag & 1) == 1) {
                    throw new ZipException("encrypted ZIP entry not supported");
                } else {
                    e.method = get16(this.tmpbuf, 8);
                    e.time = get32(this.tmpbuf, 10);
                    if((e.flag & 8) == 8) {
                        if(e.method != 8) {
                            throw new ZipException("only DEFLATED entries can have EXT descriptor");
                        }
                    } else {
                        e.crc = get32(this.tmpbuf, 14);
                        e.csize = get32(this.tmpbuf, 18);
                        e.size = get32(this.tmpbuf, 22);
                    }

                    len = get16(this.tmpbuf, 28);
                    if(len > 0) {
                        byte[] bb = new byte[len];
                        this.readFully(bb, 0, len);
                        e.extra = bb;
                    }

                    return e;
                }
            }
        }
    }

    private static String getUTF8String(byte[] b, int off, int len) {
        try {
            String var10 = new String(b, off, len, "gbk");
            return var10;
        } catch (UnsupportedEncodingException var9) {
            var9.printStackTrace();
            int count = 0;
            int max = off + len;
            int i = off;

            while(i < max) {
                int cs = b[i++] & 255;
                switch(cs >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    ++count;
                    break;
                case 8:
                case 9:
                case 10:
                case 11:
                default:
                    throw new IllegalArgumentException();
                case 12:
                case 13:
                    if((b[i++] & 192) != 128) {
                        throw new IllegalArgumentException();
                    }

                    ++count;
                    break;
                case 14:
                    if((b[i++] & 192) != 128 || (b[i++] & 192) != 128) {
                        throw new IllegalArgumentException();
                    }

                    ++count;
                }
            }

            if(i != max) {
                throw new IllegalArgumentException();
            } else {
                char[] var11 = new char[count];
                i = 0;

                while(off < max) {
                    int c = b[off++] & 255;
                    switch(c >> 4) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        var11[i++] = (char)c;
                        break;
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    default:
                        throw new IllegalArgumentException();
                    case 12:
                    case 13:
                        var11[i++] = (char)((c & 31) << 6 | b[off++] & 63);
                        break;
                    case 14:
                        int t = (b[off++] & 63) << 6;
                        var11[i++] = (char)((c & 15) << 12 | t | b[off++] & 63);
                    }
                }

                return new String(var11, 0, count);
            }
        }
    }

    protected ZipEntry createZipEntry(String name) {
        return new ZipEntry(name);
    }

    private void readEnd(ZipEntry e) throws IOException {
        int n = this.inf.getRemaining();
        if(n > 0) {
            ((PushbackInputStream)this.in).unread(this.buf, this.len - n, n);
        }

        if((e.flag & 8) == 8) {
            this.readFully(this.tmpbuf, 0, 16);
            long sig = get32(this.tmpbuf, 0);
            if(sig != 134695760L) {
                e.crc = sig;
                e.csize = get32(this.tmpbuf, 4);
                e.size = get32(this.tmpbuf, 8);
                ((PushbackInputStream)this.in).unread(this.tmpbuf, 11, 4);
            } else {
                e.crc = get32(this.tmpbuf, 4);
                e.csize = get32(this.tmpbuf, 8);
                e.size = get32(this.tmpbuf, 12);
            }
        }

        if(e.size != this.inf.getBytesWritten()) {
            throw new ZipException("invalid entry size (expected " + e.size + " but got " + this.inf.getBytesWritten() + " bytes)");
        } else if(e.csize != this.inf.getBytesRead()) {
            throw new ZipException("invalid entry compressed size (expected " + e.csize + " but got " + this.inf.getBytesRead() + " bytes)");
        } else if(e.crc != this.crc.getValue()) {
            throw new ZipException("invalid entry CRC (expected 0x" + Long.toHexString(e.crc) + " but got 0x" + Long.toHexString(this.crc.getValue()) + ")");
        }
    }

    private void readFully(byte[] b, int off, int len) throws IOException {
        while(len > 0) {
            int n = this.in.read(b, off, len);
            if(n == -1) {
                throw new EOFException();
            }

            off += n;
            len -= n;
        }

    }

    private static final int get16(byte[] b, int off) {
        return b[off] & 255 | (b[off + 1] & 255) << 8;
    }

    private static final long get32(byte[] b, int off) {
        return (long)get16(b, off) | (long)get16(b, off + 2) << 16;
    }
}
