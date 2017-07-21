package com.rratchet.android.compress;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;

import static com.rratchet.android.compress.CompressHelper.TAG;

/**
 * <pre>
 *
 * 作者:      ASLai(gdcpljh@126.com).
 * 日期:      17-7-21
 * 版本:      V1.0
 * 描述:      description
 *
 * </pre>
 */
class RarUtils {

    /**
     * Unrar folder string.
     *
     * @param zipFileString the zip file string
     * @param outPathString the out path string
     * @return the string
     */
    public static String unrarFolder(String zipFileString, String outPathString) {
        Log.d(TAG, "uncompress rar file start time = " + System.currentTimeMillis() + "");
        File srcFile = new File(zipFileString);
        if (null == outPathString || "".equals(outPathString)) {
            outPathString = srcFile.getParentFile().getPath();
        }
        // 保证文件夹路径最后是"/"或者"\"
        char lastChar = outPathString.charAt(outPathString.length() - 1);
        if (lastChar != '/' && lastChar != '\\') {
            outPathString += File.separator;
        }
        Log.d(TAG, "uncompress rar file to :" + outPathString);


        FileOutputStream fileOut = null;
        Archive rarFile = null;

        try {
            rarFile = new Archive(srcFile);
            FileHeader fh = null;
            final int total = rarFile.getFileHeaders().size();
            for (int i = 0; i < rarFile.getFileHeaders().size(); i++) {
                fh = rarFile.getFileHeaders().get(i);
                String entrypath = "";
                if (fh.isUnicode()) {//解決中文乱码
                    entrypath = fh.getFileNameW().trim();
                } else {
                    entrypath = fh.getFileNameString().trim();
                }
                entrypath = entrypath.replaceAll("\\\\", "/");

                File file = new File(outPathString, entrypath);
                Log.d(TAG, "uncompress entry file :" + file.getPath());

                if (fh.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    fileOut = new FileOutputStream(file);
                    rarFile.extractFile(fh, fileOut);
                    fileOut.close();
                }
            }
            rarFile.close();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                    fileOut = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (rarFile != null) {
                try {
                    rarFile.close();
                    rarFile = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(TAG, "uncompress finish end time = " + System.currentTimeMillis() + "");
        return outPathString;
    }

    /**
     * Compress string.
     *
     * @param zipFilePath  the zip file path
     * @param outZipString the out zip string
     * @return the string
     * @throws Exception the exception
     */
    public static String compress(String zipFilePath, String outZipString) throws Exception {
        //create ZIP
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outZipString));
        //create the file
        File file = new File(zipFilePath);
        //compress
        compress(file.getParent() + File.separator, file.getName(), zipOutputStream);
        //finish and close
        zipOutputStream.finish();
        zipOutputStream.close();
        return outZipString;
    }

    private static void compress(String folderString, String fileString, ZipOutputStream zipOutputSteam) throws Exception {
        if (zipOutputSteam == null)
            return;
        File file = new File(folderString + fileString);
        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(fileString);
            FileInputStream inputStream = new FileInputStream(file);
            zipOutputSteam.putNextEntry(zipEntry);
            int len;
            byte[] buffer = new byte[4096];
            while ((len = inputStream.read(buffer)) != -1) {
                zipOutputSteam.write(buffer, 0, len);
            }
            zipOutputSteam.closeEntry();
        } else {
            //folder
            String fileList[] = file.list();
            //no child file and compress
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
                zipOutputSteam.putNextEntry(zipEntry);
                zipOutputSteam.closeEntry();
            }
            //child files and recursion
            for (int i = 0; i < fileList.length; i++) {
                compress(folderString, fileString + File.separator + fileList[i], zipOutputSteam);
            }//end of for
        }
    }
}
