package com.zip.tool;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.CRC32;
import java.util.zip.ZipException;

public class ZipOutputStream extends DeflaterOutputStream implements ZipConstants {
    private ZipEntry entry;
    private Vector entries = new Vector();
    private Hashtable names = new Hashtable();
    private CRC32 crc = new CRC32();
    private long written = 0L;
    private long locoff = 0L;
    private String comment;
    private int method = 8;
    private boolean finished;
    private boolean closed = false;
    public static final int STORED = 0;
    public static final int DEFLATED = 8;

    private void ensureOpen() throws IOException {
        if(this.closed) {
            throw new IOException("Stream closed");
        }
    }

    public ZipOutputStream(OutputStream out) {
        super(out, new Deflater(-1, true));
        this.usesDefaultDeflater = true;
    }

    public void setComment(String comment) {
        if(comment != null && comment.length() > 21845 && getUTF8Length(comment) > '\uffff') {
            throw new IllegalArgumentException("ZIP file comment too long.");
        } else {
            this.comment = comment;
        }
    }

    public void setMethod(int method) {
        if(method != 8 && method != 0) {
            throw new IllegalArgumentException("invalid compression method");
        } else {
            this.method = method;
        }
    }

    public void setLevel(int level) {
        this.def.setLevel(level);
    }

    public void putNextEntry(ZipEntry e) throws IOException {
        this.ensureOpen();
        if(this.entry != null) {
            this.closeEntry();
        }

        if(e.time == -1L) {
            e.setTime(System.currentTimeMillis());
        }

        if(e.method == -1) {
            e.method = this.method;
        }

        switch(e.method) {
        case 0:
            if(e.size == -1L) {
                e.size = e.csize;
            } else if(e.csize == -1L) {
                e.csize = e.size;
            } else if(e.size != e.csize) {
                throw new ZipException("STORED entry where compressed != uncompressed size");
            }

            if(e.size != -1L && e.crc != -1L) {
                e.version = 10;
                e.flag = 0;
                break;
            }

            throw new ZipException("STORED entry missing size, compressed size, or crc-32");
        case 8:
            if(e.size != -1L && e.csize != -1L && e.crc != -1L) {
                if(e.size == -1L || e.csize == -1L || e.crc == -1L) {
                    throw new ZipException("DEFLATED entry missing size, compressed size, or crc-32");
                }

                e.flag = 0;
            } else {
                e.flag = 8;
            }

            e.version = 20;
            break;
        default:
            throw new ZipException("unsupported compression method");
        }

        e.offset = this.written;
        if(this.names.put(e.name, e) != null) {
            throw new ZipException("duplicate entry: " + e.name);
        } else {
            this.writeLOC(e);
            this.entries.addElement(e);
            this.entry = e;
        }
    }

    public void closeEntry() throws IOException {
        this.ensureOpen();
        ZipEntry e = this.entry;
        if(e != null) {
            switch(e.method) {
            case 0:
                if(e.size != this.written - this.locoff) {
                    throw new ZipException("invalid entry size (expected " + e.size + " but got " + (this.written - this.locoff) + " bytes)");
                }

                if(e.crc != this.crc.getValue()) {
                    throw new ZipException("invalid entry crc-32 (expected 0x" + Long.toHexString(e.crc) + " but got 0x" + Long.toHexString(this.crc.getValue()) + ")");
                }
                break;
            case 8:
                this.def.finish();

                while(!this.def.finished()) {
                    this.deflate();
                }

                if((e.flag & 8) == 0) {
                    if(e.size != this.def.getBytesRead()) {
                        throw new ZipException("invalid entry size (expected " + e.size + " but got " + this.def.getBytesRead() + " bytes)");
                    }

                    if(e.csize != this.def.getBytesWritten()) {
                        throw new ZipException("invalid entry compressed size (expected " + e.csize + " but got " + this.def.getBytesWritten() + " bytes)");
                    }

                    if(e.crc != this.crc.getValue()) {
                        throw new ZipException("invalid entry CRC-32 (expected 0x" + Long.toHexString(e.crc) + " but got 0x" + Long.toHexString(this.crc.getValue()) + ")");
                    }
                } else {
                    e.size = this.def.getBytesRead();
                    e.csize = this.def.getBytesWritten();
                    e.crc = this.crc.getValue();
                    this.writeEXT(e);
                }

                this.def.reset();
                this.written += e.csize;
                break;
            default:
                throw new InternalError("invalid compression method");
            }

            this.crc.reset();
            this.entry = null;
        }

    }

    public synchronized void write(byte[] b, int off, int len) throws IOException {
        this.ensureOpen();
        if(off >= 0 && len >= 0 && off <= b.length - len) {
            if(len != 0) {
                if(this.entry == null) {
                    throw new ZipException("no current ZIP entry");
                } else {
                    switch(this.entry.method) {
                    case 0:
                        this.written += (long)len;
                        if(this.written - this.locoff > this.entry.size) {
                            throw new ZipException("attempt to write past end of STORED entry");
                        }

                        this.out.write(b, off, len);
                        break;
                    case 8:
                        super.write(b, off, len);
                        break;
                    default:
                        throw new InternalError("invalid compression method");
                    }

                    this.crc.update(b, off, len);
                }
            }
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public void finish() throws IOException {
        this.ensureOpen();
        if(!this.finished) {
            if(this.entry != null) {
                this.closeEntry();
            }

            if(this.entries.size() < 1) {
                throw new ZipException("ZIP file must have at least one entry");
            } else {
                long off = this.written;
                Enumeration e = this.entries.elements();

                while(e.hasMoreElements()) {
                    this.writeCEN((ZipEntry)e.nextElement());
                }

                this.writeEND(off, this.written - off);
                this.finished = true;
            }
        }
    }

    public void close() throws IOException {
        if(!this.closed) {
            super.close();
            this.closed = true;
        }

    }

    private void writeLOC(ZipEntry e) throws IOException {
        this.writeInt(67324752L);
        this.writeShort(e.version);
        this.writeShort(e.flag);
        this.writeShort(e.method);
        this.writeInt(e.time);
        if((e.flag & 8) == 8) {
            this.writeInt(0L);
            this.writeInt(0L);
            this.writeInt(0L);
        } else {
            this.writeInt(e.crc);
            this.writeInt(e.csize);
            this.writeInt(e.size);
        }

        byte[] nameBytes = getUTF8Bytes(e.name);
        this.writeShort(nameBytes.length);
        this.writeShort(e.extra != null?e.extra.length:0);
        this.writeBytes(nameBytes, 0, nameBytes.length);
        if(e.extra != null) {
            this.writeBytes(e.extra, 0, e.extra.length);
        }

        this.locoff = this.written;
    }

    private void writeEXT(ZipEntry e) throws IOException {
        this.writeInt(134695760L);
        this.writeInt(e.crc);
        this.writeInt(e.csize);
        this.writeInt(e.size);
    }

    private void writeCEN(ZipEntry e) throws IOException {
        this.writeInt(33639248L);
        this.writeShort(e.version);
        this.writeShort(e.version);
        this.writeShort(e.flag);
        this.writeShort(e.method);
        this.writeInt(e.time);
        this.writeInt(e.crc);
        this.writeInt(e.csize);
        this.writeInt(e.size);
        byte[] nameBytes = getUTF8Bytes(e.name);
        this.writeShort(nameBytes.length);
        this.writeShort(e.extra != null?e.extra.length:0);
        byte[] commentBytes;
        if(e.comment != null) {
            commentBytes = getUTF8Bytes(e.comment);
            this.writeShort(commentBytes.length);
        } else {
            commentBytes = (byte[])null;
            this.writeShort(0);
        }

        this.writeShort(0);
        this.writeShort(0);
        this.writeInt(0L);
        this.writeInt(e.offset);
        this.writeBytes(nameBytes, 0, nameBytes.length);
        if(e.extra != null) {
            this.writeBytes(e.extra, 0, e.extra.length);
        }

        if(commentBytes != null) {
            this.writeBytes(commentBytes, 0, commentBytes.length);
        }

    }

    private void writeEND(long off, long len) throws IOException {
        this.writeInt(101010256L);
        this.writeShort(0);
        this.writeShort(0);
        this.writeShort(this.entries.size());
        this.writeShort(this.entries.size());
        this.writeInt(len);
        this.writeInt(off);
        if(this.comment != null) {
            byte[] b = getUTF8Bytes(this.comment);
            this.writeShort(b.length);
            this.writeBytes(b, 0, b.length);
        } else {
            this.writeShort(0);
        }

    }

    private void writeShort(int v) throws IOException {
        OutputStream out = this.out;
        out.write(v >>> 0 & 255);
        out.write(v >>> 8 & 255);
        this.written += 2L;
    }

    private void writeInt(long v) throws IOException {
        OutputStream out = this.out;
        out.write((int)(v >>> 0 & 255L));
        out.write((int)(v >>> 8 & 255L));
        out.write((int)(v >>> 16 & 255L));
        out.write((int)(v >>> 24 & 255L));
        this.written += 4L;
    }

    private void writeBytes(byte[] b, int off, int len) throws IOException {
        super.out.write(b, off, len);
        this.written += (long)len;
    }

    static int getUTF8Length(String s) {
        int count = 0;

        for(int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            if(ch <= 127) {
                ++count;
            } else if(ch <= 2047) {
                count += 2;
            } else {
                count += 3;
            }
        }

        return count;
    }

    private static byte[] getUTF8Bytes(String s) {
        char[] c = s.toCharArray();
        int len = c.length;
        int count = 0;

        for(int b = 0; b < len; ++b) {
            char off = c[b];
            if(off <= 127) {
                ++count;
            } else if(off <= 2047) {
                count += 2;
            } else {
                count += 3;
            }
        }

        byte[] var8 = new byte[count];
        int var9 = 0;

        for(int i = 0; i < len; ++i) {
            char ch = c[i];
            if(ch <= 127) {
                var8[var9++] = (byte)ch;
            } else if(ch <= 2047) {
                var8[var9++] = (byte)(ch >> 6 | 192);
                var8[var9++] = (byte)(ch & 63 | 128);
            } else {
                var8[var9++] = (byte)(ch >> 12 | 224);
                var8[var9++] = (byte)(ch >> 6 & 63 | 128);
                var8[var9++] = (byte)(ch & 63 | 128);
            }
        }

        return var8;
    }
}
