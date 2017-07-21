package com.rratchet.android.compress;

import android.annotation.SuppressLint;

import com.zip.tool.ZipEntry;
import com.zip.tool.ZipInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * The type Zip utils.
 */
class ZipUtils {

    private static final String I_8859_1 = "8859_1";
    private static final String ISO_8859_1 = "ISO-8859-1";
    private static final String UTF_8 = "UTF-8";

    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte

    /**
     * 批量压缩文件（夹）
     *
     * @param resFileList 要压缩的文件（夹）列表
     * @param zipFile     生成的压缩文件
     * @throws IOException 当压缩过程出错时抛出
     */
    public static void zipFiles(Collection<File> resFileList, File zipFile) throws IOException {

        ZipOutputStream zipout = null;
        try {
            zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile), BUFF_SIZE));
            for (File resFile : resFileList) {
                zipFile(resFile, zipout, "");
            }
        } finally {
            if (zipout != null)
                zipout.close();
        }
    }

    /**
     * 批量压缩文件（夹）
     *
     * @param resFileList 要压缩的文件（夹）列表
     * @param zipFile     生成的压缩文件
     * @param comment     压缩文件的注释
     * @throws IOException 当压缩过程出错时抛出
     */
    public static void zipFiles(Collection<File> resFileList, File zipFile, String comment) throws IOException {
        ZipOutputStream zipout = null;
        try {
            zipout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile), BUFF_SIZE));
            for (File resFile : resFileList) {
                zipFile(resFile, zipout, "");
            }
            zipout.setComment(comment);

        } finally {
            if (zipout != null)
                zipout.close();
        }
    }

    /**
     * 解压zip
     *
     * @param zipFile   the zip file
     * @param targetDir the target dir
     * @throws Exception the exception
     */
    @SuppressLint("NewApi")
    public static void unzipFile(String zipFile, String targetDir) throws Exception {
        //CacheHandler.getInstant().deleteDirFile(targetDir);

        int BUFFER = 4096; //缓冲区使用4KB，
        String strEntry; //保存每个zip的条目名称

        BufferedOutputStream dest = null; //缓冲输出流
        FileInputStream fis = new FileInputStream(zipFile);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry; //每个zip条目的实例

        while ((entry = zis.getNextEntry()) != null) {
            int count;
            byte data[] = new byte[BUFFER];
            strEntry = entry.getName();
            if (entry.isDirectory()) {
                // get the folder name of the widget
                strEntry = strEntry.substring(0, strEntry.length() - 1);
                File folder = new File(targetDir + File.separator + strEntry);
                // File folder = new File(targetDir + "/" + strEntry);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
            } else {
                File entryFile = new File(targetDir + File.separator + strEntry);
                File entryDir = new File(entryFile.getParent());
                if (!entryDir.exists()) {
                    entryDir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(entryFile);
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
        }
        zis.close();
    }

    /**
     * 获得压缩文件内文件列表
     *
     * @param zipFile 压缩文件
     * @return 压缩文件内文件名称 entries names
     * @throws ZipException 压缩文件格式有误时抛出
     * @throws IOException  当解压缩过程出错时抛出
     */
    public static ArrayList<String> getEntriesNames(File zipFile) throws ZipException, IOException {
        ArrayList<String> entryNames = new ArrayList<String>();
        Enumeration<?> entries = getEntriesEnumeration(zipFile);
        while (entries.hasMoreElements()) {
            ZipEntry entry = ((ZipEntry) entries.nextElement());
            entryNames.add(new String(getEntryName(entry).getBytes(UTF_8), ISO_8859_1));
        }
        return entryNames;
    }

    /**
     * 获得压缩文件内压缩文件对象以取得其属性
     *
     * @param zipFile 压缩文件
     * @return 返回一个压缩文件列表 entries enumeration
     * @throws ZipException 压缩文件格式有误时抛出
     * @throws IOException  IO操作有误时抛出
     */
    public static Enumeration<?> getEntriesEnumeration(File zipFile) throws ZipException, IOException {
        ZipFile zf = new ZipFile(zipFile);
        return zf.entries();

    }

    /**
     * 取得压缩文件对象的注释
     *
     * @param entry 压缩文件对象
     * @return 压缩文件对象的注释 entry comment
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public static String getEntryComment(ZipEntry entry) throws UnsupportedEncodingException {
        return new String(entry.getComment().getBytes(UTF_8), ISO_8859_1);
    }

    /**
     * 取得压缩文件对象的名称
     *
     * @param entry 压缩文件对象
     * @return 压缩文件对象的名称 entry name
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public static String getEntryName(ZipEntry entry) throws UnsupportedEncodingException {
        return new String(entry.getName().getBytes(UTF_8), ISO_8859_1);
    }

    /**
     * 压缩文件
     *
     * @param resFile  需要压缩的文件（夹）
     * @param zipout   压缩的目的文件
     * @param rootpath 压缩的文件路径
     * @throws FileNotFoundException 找不到文件时抛出
     * @throws IOException           当压缩过程出错时抛出
     */
    private static void zipFile(File resFile, ZipOutputStream zipout, String rootpath) throws FileNotFoundException, IOException {
        rootpath = rootpath + (rootpath.trim().length() == 0 ? "" : File.separator) + resFile.getName();
        rootpath = new String(rootpath.getBytes(ISO_8859_1), UTF_8);
        BufferedInputStream in = null;
        try {
            if (resFile.isDirectory()) {
                File[] fileList = resFile.listFiles();
                for (File file : fileList) {
                    zipFile(file, zipout, rootpath);
                }
            } else {
                byte buffer[] = new byte[BUFF_SIZE];
                in = new BufferedInputStream(new FileInputStream(resFile), BUFF_SIZE);
                zipout.putNextEntry(new java.util.zip.ZipEntry(rootpath));
                int realLength;
                while ((realLength = in.read(buffer)) != -1) {
                    zipout.write(buffer, 0, realLength);
                }
                in.close();
                zipout.flush();
                zipout.closeEntry();
            }
        } finally {
            if (in != null)
                in.close();
            // if (zipout != null);
            // zipout.close();
        }
    }
}