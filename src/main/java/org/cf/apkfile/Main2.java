package org.cf.apkfile;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

public class Main2 {
    public static void main(String[] args) throws Exception {
        ZipFile zipFile = new ZipFile(args[0]);
        System.out.println("Is valid " + zipFile.isValidZipFile());
        for (Object header : zipFile.getFileHeaders()) {
            System.out.println("Found header: " + ((FileHeader) header).getFileName());
        }
        System.out.println("Getting stupid entry: " + zipFile.getFileHeader("asdflkasdjflskjdderpy"));
    }
}
