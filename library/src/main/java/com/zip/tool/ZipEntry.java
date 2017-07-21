package com.zip.tool;

import com.zip.tool.ZipConstants;
import com.zip.tool.ZipOutputStream;
import java.util.Date;

public class ZipEntry implements ZipConstants, Cloneable {
    String name;
    long time = -1L;
    long crc = -1L;
    long size = -1L;
    long csize = -1L;
    int method = -1;
    byte[] extra;
    String comment;
    int flag;
    int version;
    long offset;
    public static final int STORED = 0;
    public static final int DEFLATED = 8;

    public ZipEntry(String name) {
        if(name == null) {
            throw new NullPointerException();
        } else if(name.length() > '\uffff') {
            throw new IllegalArgumentException("entry name too long");
        } else {
            this.name = name;
        }
    }

    public ZipEntry(ZipEntry e) {
        this.name = e.name;
        this.time = e.time;
        this.crc = e.crc;
        this.size = e.size;
        this.csize = e.csize;
        this.method = e.method;
        this.extra = e.extra;
        this.comment = e.comment;
    }

    ZipEntry(String name, long jzentry) {
        this.name = name;
        this.initFields(jzentry);
    }

    private native void initFields(long var1);

    ZipEntry(long jzentry) {
        this.initFields(jzentry);
    }

    public String getName() {
        return this.name;
    }

    public void setTime(long time) {
        this.time = javaToDosTime(time);
    }

    public long getTime() {
        return this.time != -1L?dosToJavaTime(this.time):-1L;
    }

    public void setSize(long size) {
        if(size >= 0L && size <= 4294967295L) {
            this.size = size;
        } else {
            throw new IllegalArgumentException("invalid entry size");
        }
    }

    public long getSize() {
        return this.size;
    }

    public long getCompressedSize() {
        return this.csize;
    }

    public void setCompressedSize(long csize) {
        this.csize = csize;
    }

    public void setCrc(long crc) {
        if(crc >= 0L && crc <= 4294967295L) {
            this.crc = crc;
        } else {
            throw new IllegalArgumentException("invalid entry crc-32");
        }
    }

    public long getCrc() {
        return this.crc;
    }

    public void setMethod(int method) {
        if(method != 0 && method != 8) {
            throw new IllegalArgumentException("invalid compression method");
        } else {
            this.method = method;
        }
    }

    public int getMethod() {
        return this.method;
    }

    public void setExtra(byte[] extra) {
        if(extra != null && extra.length > '\uffff') {
            throw new IllegalArgumentException("invalid extra field length");
        } else {
            this.extra = extra;
        }
    }

    public byte[] getExtra() {
        return this.extra;
    }

    public void setComment(String comment) {
        if(comment != null && comment.length() > 21845 && ZipOutputStream.getUTF8Length(comment) > '\uffff') {
            throw new IllegalArgumentException("invalid entry comment length");
        } else {
            this.comment = comment;
        }
    }

    public String getComment() {
        return this.comment;
    }

    public boolean isDirectory() {
        return this.name.endsWith("/");
    }

    public String toString() {
        return this.getName();
    }

    private static long dosToJavaTime(long dtime) {
        Date d = new Date((int)((dtime >> 25 & 127L) + 80L), (int)((dtime >> 21 & 15L) - 1L), (int)(dtime >> 16 & 31L), (int)(dtime >> 11 & 31L), (int)(dtime >> 5 & 63L), (int)(dtime << 1 & 62L));
        return d.getTime();
    }

    private static long javaToDosTime(long time) {
        Date d = new Date(time);
        int year = d.getYear() + 1900;
        return year < 1980?2162688L:(long)(year - 1980 << 25 | d.getMonth() + 1 << 21 | d.getDate() << 16 | d.getHours() << 11 | d.getMinutes() << 5 | d.getSeconds() >> 1);
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public Object clone() {
        try {
            ZipEntry e = (ZipEntry)super.clone();
            e.extra = this.extra == null?null:(byte[])this.extra.clone();
            return e;
        } catch (CloneNotSupportedException var2) {
            throw new InternalError();
        }
    }
}
