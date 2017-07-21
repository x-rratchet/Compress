package com.zip.tool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class CompresszZipFile {
    static final int BUFFER = 2048;

    public CompresszZipFile() {
    }

    public void ReadZip(String zipfilepath, String unzippath) {
        try {
            BufferedOutputStream e = null;
            FileInputStream fis = new FileInputStream(zipfilepath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);

            while(true) {
                ZipEntry entry;
                byte[] date;
                do {
                    if((entry = zis.getNextEntry()) == null) {
                        zis.close();
                        return;
                    }

                    System.out.println("====" + entry.getName());
                    date = new byte[2048];
                } while(entry.isDirectory());

                int begin = zipfilepath.lastIndexOf("\\") + 1;
                int end = zipfilepath.lastIndexOf(".") + 1;
                String zipRealName = zipfilepath.substring(begin, end);
                e = new BufferedOutputStream(new FileOutputStream(this.getRealFileName(unzippath + "\\" + zipRealName, entry.getName())));

                int count;
                while((count = zis.read(date)) != -1) {
                    e.write(date, 0, count);
                }

                e.flush();
                e.close();
            }
        } catch (Exception var13) {
            var13.printStackTrace();
        }
    }

    private File getRealFileName(String zippath, String absFileName) {
        String[] dirs = absFileName.split("/", absFileName.length());
        File file = new File(zippath);
        if(dirs.length > 1) {
            for(int i = 0; i < dirs.length - 1; ++i) {
                file = new File(file, dirs[i]);
            }
        }

        if(!file.exists()) {
            file.mkdirs();
        }

        file = new File(file, dirs[dirs.length - 1]);
        return file;
    }
}
