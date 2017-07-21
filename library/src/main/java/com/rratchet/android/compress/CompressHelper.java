package com.rratchet.android.compress;

import com.zip.tool.ZipEntry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

/**
 * <pre>
 *
 * 作者:      ASLai(gdcpljh@126.com).
 * 日期:      17-7-21
 * 版本:      V1.0
 * 描述:      解压缩助手
 *
 * </pre>
 */
public class CompressHelper {

    private CompressHelper() {}

    /**
     * The Tag.
     */
    static final String TAG = CompressHelper.class.getSimpleName();

    /**
     * The Suffix rar.
     */
    static final String SUFFIX_RAR = ".rar";
    /**
     * The Suffix zip.
     */
    static final String SUFFIX_ZIP = ".zip";

    private static boolean isRARFile(String compressFilePath) {
        return compressFilePath != null && compressFilePath.endsWith(SUFFIX_RAR);
    }

    private static boolean isZIPFile(String compressFilePath) {
        return compressFilePath != null && compressFilePath.endsWith(SUFFIX_ZIP);
    }

    /**
     * 提取压缩文件.
     *
     * @param compressFilePath the compress file path
     * @param outPathString    the out path string
     * @throws Exception the exception
     */
    public static void extract(String compressFilePath, String outPathString) throws Exception {

        // check file exists
        File zipFile = new File(compressFilePath);

        if (!zipFile.exists()) {
            throw new IOException("file not exists");
        }

        if (isRARFile(compressFilePath)) {
            RarUtils.unrarFolder(compressFilePath, outPathString);
            return;
        }

        if (isZIPFile(compressFilePath)) {
            ZipUtils.unzipFile(compressFilePath, outPathString);
            return;
        }
    }


    /**
     * Rar方式压缩文件.
     *
     * @param filePath     the zip file path
     * @param outRarString the out rar string
     * @return the string
     * @throws Exception the exception
     */
    public static String compressRarFile(String filePath, String outRarString) throws Exception {
        return RarUtils.compress(filePath, outRarString);
    }

    /**
     * zip方式压缩文件.
     *
     * @param targetFileList the target file list
     * @param targetZipFile  the target zip file
     * @throws Exception the exception
     */
    public static void compressZipFile(Collection<File> targetFileList, File targetZipFile) throws Exception {
        ZipUtils.zipFiles(targetFileList, targetZipFile);
    }

    /**
     * zip方式压缩文件.
     *
     * @param targetFileList the target file list
     * @param targetZipFile  the target zip file
     * @param comment        the comment
     * @throws Exception the exception
     */
    public static void compressZipFile(Collection<File> targetFileList, File targetZipFile, String comment) throws Exception {
        ZipUtils.zipFiles(targetFileList, targetZipFile, comment);
    }

    /**
     * 获得Zip压缩文件内文件列表.
     *
     * @param zipFile the zip file
     * @return the entries names
     * @throws Exception the exception
     */
    public static ArrayList<String> getEntriesNames(File zipFile) throws Exception {
        return ZipUtils.getEntriesNames(zipFile);
    }

    /**
     * 获得Zip压缩文件内压缩文件对象以取得其属性.
     *
     * @param zipFile the zip file
     * @return the entries enumeration
     * @throws Exception the exception
     */
    public static Enumeration<?> getEntriesEnumeration(File zipFile) throws Exception {
        return ZipUtils.getEntriesEnumeration(zipFile);
    }

    /**
     * 取得Zip压缩文件对象的注释.
     *
     * @param entry the entry
     * @return the entry comment
     * @throws Exception the exception
     */
    public static String getEntryComment(ZipEntry entry) throws Exception {
        return ZipUtils.getEntryComment(entry);
    }

    /**
     * 取得Zip压缩文件对象的名称.
     *
     * @param entry the entry
     * @return the entry name
     * @throws Exception the exception
     */
    public static String getEntryName(ZipEntry entry) throws Exception {
        return ZipUtils.getEntryName(entry);
    }
}
