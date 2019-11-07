package org.cf.apkfile;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.cf.apkfile.apk.ApkFile;
import org.cf.apkfile.apk.ApkFileFactory;
import org.cf.apkfile.res.Chunk;
import org.cf.apkfile.res.ResourceFile;
import org.cf.apkfile.res.ResourceTableChunk;

import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.attribute.FileTime;
import java.security.CodeSigner;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class BlamerTest {
    public static void main(String[] args) throws Exception {
        //        ApkFile apkFile = new ApkFile(args[0]);
        //        System.out.println("wait up: " + apkFile);

        //getDates3(args[0]);
    }

    private static void getDates(String path) throws IOException, ParseException {
        ApkFile apkFile = new ApkFileFactory().build(path);
        FileTime apkModifiedTime = FileTime.from(new File(path).lastModified(), TimeUnit.MILLISECONDS);
        System.out.println(apkFile.getDexEntries().keySet());
        FileTime dexModifiedTime = FileTime.from(apkFile.getDexEntries().get("classes.dex").getLastModifiedTime(), TimeUnit.MILLISECONDS);
        FileTime certModifiedTime = FileTime.from(apkFile.getCertificateEntry().getLastModifiedTime(), TimeUnit.MILLISECONDS);
        System.out.println("APK modified time: " + apkModifiedTime);
        System.out.println("DEX modified time: " + dexModifiedTime);
        System.out.println("CERT modified time: " + certModifiedTime);
    }

    private static void printCertInfo(String jarPath) throws Exception {
        // verify = true is key here
        JarFile apkFile = new JarFile(jarPath, true);
        JarEntry androidManifestEntry = apkFile.getJarEntry("AndroidManifest.xml");
        if (androidManifestEntry == null) {
            System.err.println("APK has no AndroidManifest.xml");
            System.exit(-1);
        }

        // Need to fully read stream to verify cert
        System.out.println("Android manifest probably signed: " + androidManifestEntry.getLastModifiedTime());

        ByteStreams.copy(apkFile.getInputStream(androidManifestEntry), ByteStreams.nullOutputStream());
        // Assuming only signed with a single cert (not always true, but you get the idea)
        X509Certificate cert = (X509Certificate) androidManifestEntry.getCertificates()[0];
        //androidManifestEntry.getCodeSigners()[0].getTimestamp()
        System.out.println("Android manifest cert probably created: " + cert.getNotBefore());
        System.out.println("Full cert: " + cert.toString());
    }

    private static void getDates2(String path) throws IOException, ParseException {
        JarFile jar = new JarFile(path, true);
        // Need each entry so that future calls to entry.getCodeSigners will return anything
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            //jar.getInputStream(entry)
            ByteStreams.copy(jar.getInputStream(entry), ByteStreams.nullOutputStream());
        }

        // Now check each entry that is not a signature file
        entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String fileName = entry.getName().toUpperCase(Locale.ENGLISH);
            if (!fileName.endsWith(".SF")
                    && !fileName.endsWith(".DSA")
                    && !fileName.endsWith(".EC")
                    && !fileName.endsWith(".RSA")) {

                CodeSigner[] css = entry.getCodeSigners();
                System.out.println(css.length);
            }
        }
        jar.close();
    }

    private static void doit(String path) throws Exception {
        ApkFile apkFile = new ApkFileFactory().build(path);
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.disableHtmlEscaping().serializeSpecialFloatingPointValues().setPrettyPrinting().create();
        String json = gson.toJson(apkFile);
        System.out.println(json);

    }

    private static void old(String path) throws Exception {
        ApkFile apkFile = new ApkFileFactory()
                .skipParsingResources()
                .skipParsingAndroidManifest()
                .skipParsingResources()
                .skipParsingDexFiles()
                .build(path);

        FileHeader resourcesEntry = apkFile.getEntry("resources.arsc");
        InputStream resourcesStream = apkFile.getInputStream(resourcesEntry);

        ResourceFile arscRF = ResourceFile.fromInputStream(resourcesStream);
        ResourceTableChunk resourceTable = (ResourceTableChunk) arscRF.getChunks().get(0);

        FileHeader manifestEntry = apkFile.getEntry("AndroidManifest.xml");
        InputStream manifestStream = apkFile.getInputStream(manifestEntry);
        //        manifestRF = ResourceFile.fromInputStream(manifestStream);
        byte[] buf = ByteStreams.toByteArray(manifestStream);
        ByteBuffer buffer = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        Chunk manifestChunk = Chunk.newInstance(buffer, resourceTable);

        System.out.println(manifestChunk);
    }
}
