/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PsrParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Manik
 */
public class PsrParser {

    public void unZipIt(String zipFile, String outputFolder) {
        List<String> fileList;
        byte[] buffer = new byte[1024];
        try {
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }
            //get the zip file content
            ZipInputStream zis
                    = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                System.out.println("file unzip : " + newFile.getAbsoluteFile());
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            System.out.println("Done");

        } catch (IOException ex) {
            System.out.println("Error occured during Unzipping");
        }
    }
}
