package org.cf.apkfile;

import com.google.gson.Gson;
import org.cf.apkfile.apk.ApkFile;
import org.cf.apkfile.apk.ApkFileFactory;
import org.cf.apkfile.apk.JarFileExclusionStrategy;
import org.cf.apkfile.utils.Utils;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Main {

    private static final String LOG_FILE = "log.txt";

    public static void main(String[] args) throws IOException, ParseException {
        configureLog();

        if (args.length != 1) {
            System.out.println("Usage: main <apk path>");
            System.exit(-1);
        }

        String apkPath = args[0];
        ApkFile apkFile = new ApkFileFactory().build(apkPath);

        Gson gson = Utils.getTroveAwareGsonBuilder()
                .disableHtmlEscaping()
                .serializeSpecialFloatingPointValues()
                .setExclusionStrategies(new JarFileExclusionStrategy())
                .setPrettyPrinting()
                .create();
        Writer writer = new OutputStreamWriter(System.out);
        gson.toJson(apkFile, writer);

        writer.close();
        apkFile.close();
    }

    private static void configureLog() {
        Configurator.defaultConfig()
                .writer(new org.pmw.tinylog.writers.FileWriter(LOG_FILE))
                .level(Level.INFO).activate();
    }
}
