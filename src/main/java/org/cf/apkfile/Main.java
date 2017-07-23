package org.cf.apkfile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import gnu.trove.map.TObjectIntMap;
import org.cf.apkfile.apk.JarFileExclusionStrategy;
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
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonSerializer<TObjectIntMap> serializer = (src, typeOfSrc, context) -> {
            JsonObject jsonMerchant = new JsonObject();
            for (Object key : src.keys()) {
                int value = src.get(key);
                jsonMerchant.addProperty(key.toString(), value);
            }
            return jsonMerchant;
        };
        gsonBuilder.registerTypeAdapter(TObjectIntMap.class, serializer);
        Gson gson = gsonBuilder
                .disableHtmlEscaping()
                .serializeSpecialFloatingPointValues()
                .setExclusionStrategies(new JarFileExclusionStrategy())
                .setPrettyPrinting()
                .create();
        Writer writer = new OutputStreamWriter(System.out);
        ApkFile apkFile = new ApkFile(apkPath, true, true, true, true, false);
        gson.toJson(apkFile, writer);

        writer.close();
        apkFile.close();
    }

    private static void configureLog() {
        Configurator.defaultConfig().writer(new org.pmw.tinylog.writers.FileWriter(LOG_FILE))
                .level(Level.INFO).activate();
    }
}
