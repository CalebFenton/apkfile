package org.cf.apkfile;

import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.cf.apkfile.res.Chunk;
import org.cf.apkfile.res.ResourceFile;
import org.cf.apkfile.res.ResourceTableChunk;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.ZipEntry;

public class BlamerTest {
    public static void main(String[] args) throws Exception {
        ApkFile apkFile = new ApkFile(args[0]);
        //        old(args[0]);

        //doit(args[0]);
        System.out.println("wait up: " + apkFile);
    }

    private static void doit(String path) throws Exception {
        ApkFile apkFile = new ApkFile(path);
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeSpecialFloatingPointValues().setPrettyPrinting().create();
        String json = gson.toJson(apkFile);
        System.out.println(json);

    }

    private static void old(String path) throws Exception {
        ApkFile apkFile = new ApkFile(path, false, false, false);

        ZipEntry resourcesEntry = apkFile.getEntry("resources.arsc");
        InputStream resourcesStream = apkFile.getZipFile().getInputStream(resourcesEntry);

        ResourceFile arscRF = ResourceFile.fromInputStream(resourcesStream);
        ResourceTableChunk resourceTable = (ResourceTableChunk) arscRF.getChunks().get(0);

        ZipEntry manifestEntry = apkFile.getEntry("AndroidManifest.xml");
        InputStream manifestStream = apkFile.getZipFile().getInputStream(manifestEntry);
        //        manifestRF = ResourceFile.fromInputStream(manifestStream);
        byte[] buf = ByteStreams.toByteArray(manifestStream);
        ByteBuffer buffer = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        Chunk manifestChunk = Chunk.newInstance(buffer, resourceTable);

        System.out.println(manifestChunk);
    }
}
