package org.cf.apkfile.apk;

import org.cf.apkfile.res.ResourceFile;
import org.cf.apkfile.res.ResourceTableChunk;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Resources {

    // https://github.com/google/android-arscblamer
    public static final String[] ENTRY_STRING_HEADERS = new String[]{"Configs"};
    public static final String[] ENTRY_NUMERIC_HEADERS = new String[]{"Private Size", "Shared Size", "Proportional Size", "Config Count"};

    // https://developer.android.com/guide/topics/resources/providing-resources.html
    public static final String[] CONFIG_STRING_HEADERS = new String[]{"Keys", "MCC", "MNC", "LANGUAGE_STRING", "REGION_STRING", "SCREEN_LAYOUT_DIRECTION", "SMALLEST_SCREEN_WIDTH_DP", "SCREEN_WIDTH_DP", "SCREEN_HEIGHT_DP", "SCREEN_LAYOUT_SIZE", "SCREEN_LAYOUT_LONG", "SCREEN_LAYOUT_ROUND", "ORIENTATION", "UI_MODE_TYPE", "UI_MODE_NIGHT", "DENSITY_DPI", "TOUCHSCREEN", "KEYBOARD_HIDDEN", "KEYBOARD", "NAVIGATION_HIDDEN", "NAVIGATION", "SDK_VERSION"};
    public static final String[] CONFIG_NUMERIC_HEADERS = new String[]{"Size", "Null Entries", "Entry Count", "Density",};

    private final transient ResourceTableChunk resourceTable;
    private final Map<String, Map<String, Object>> entries;
    private final Map<String, Map<String, Object>> resourceConfigs;

    public Resources(InputStream resourcesStream) throws IOException {
        ResourceFile arscRF = ResourceFile.fromInputStream(resourcesStream);
        resourceTable = (ResourceTableChunk) arscRF.getChunks().get(0);

        entries = new HashMap<>();
        resourceConfigs = new HashMap<>();
    }

    public Map<String, Map<String, Object>> getEntries() {
        return entries;
    }

    public Map<String, Map<String, Object>> getResourceConfigs() {
        return resourceConfigs;
    }

    public ResourceTableChunk getResourceTable() {
        return resourceTable;
    }

    private static Map<String, Map<String, Object>> buildEntries(
            Collection<Map<String, String>> entryRows) throws IOException {
        /*
        "Type","Name","Private Size","Shared Size","Proportional Size","Config Count","Configs"
        "string","app_name","162","0","263.0000000000","1","default"
        "style","AppBaseTheme","212","93","384.7500000000","3","default v11 v14"
        "style","AppTheme","35","12","164.2500000000","1","default"
        "drawable","ic_launcher","406","99","574.0000000000","3","hdpi-v4 mdpi-v4 xhdpi-v4"
         */
        Map<String, Map<String, Object>> entries = new HashMap<>();
        for (Map<String, String> row : entryRows) {
            String key = row.get("Type") + "_" + row.get("Name");
            Map<String, Object> values = new HashMap<>();
            for (String header : ENTRY_STRING_HEADERS) {
                values.put(header, row.get(header));
            }
            for (String header : ENTRY_NUMERIC_HEADERS) {
                values.put(header, Double.parseDouble(row.get(header)));
            }
            entries.put(key, values);
        }

        return entries;
    }

    private static Map<String, Map<String, Object>> buildResourceConfigs(
            Collection<Map<String, String>> configRows) throws IOException {
        /*
        "Type","Config","Size","Null Entries","Entry Count","Density","Keys","MCC","MNC","LANGUAGE_STRING","REGION_STRING","SCREEN_LAYOUT_DIRECTION","SMALLEST_SCREEN_WIDTH_DP","SCREEN_WIDTH_DP","SCREEN_HEIGHT_DP","SCREEN_LAYOUT_SIZE","SCREEN_LAYOUT_LONG","SCREEN_LAYOUT_ROUND","ORIENTATION","UI_MODE_TYPE","UI_MODE_NIGHT","DENSITY_DPI","TOUCHSCREEN","KEYBOARD_HIDDEN","KEYBOARD","NAVIGATION_HIDDEN","NAVIGATION","SDK_VERSION"
        "drawable","mdpi-v4","92","0","1","1.0000","ic_launcher","","","","","","","","","","","","","","","mdpi","","","","","","v4"
        "drawable","hdpi-v4","92","0","1","1.0000","ic_launcher","","","","","","","","","","","","","","","hdpi","","","","","","v4"
        "drawable","xhdpi-v4","92","0","1","1.0000","ic_launcher","","","","","","","","","","","","","","","xhdpi","","","","","","v4"
        "string","default","92","0","1","1.0000","app_name","","","","","","","","","","","","","","","","","","","","",""
        "style","v14","96","1","1","0.5000","AppBaseTheme","","","","","","","","","","","","","","","","","","","","","v14"
        "style","v11","96","1","1","0.5000","AppBaseTheme","","","","","","","","","","","","","","","","","","","","","v11"
        "style","default","112","0","2","1.0000","AppBaseTheme AppTheme","","","","","","","","","","","","","","","","","","","","",""
         */
        Map<String, Map<String, Object>> resourceConfigs = new HashMap<>();
        for (Map<String, String> row : configRows) {
            String key = row.get("Type") + "_" + row.get("Config");
            Map<String, Object> values = new HashMap<>();
            for (String header : CONFIG_STRING_HEADERS) {
                String value = row.get(header);
                if (value.isEmpty()) {
                    continue;
                }
                values.put(header, value);
            }
            for (String header : CONFIG_NUMERIC_HEADERS) {
                String value = row.get(header);
                if (value.isEmpty()) {
                    continue;
                }
                values.put(header, Double.parseDouble(value));
            }
            resourceConfigs.put(key, values);
        }

        return resourceConfigs;
    }

    private static List<Map<String, String>> parseCSV(String csv) {
        String[] lines = csv.split("\n");
        List<String> headers = splitCSVLine(lines[0]);
        //        System.out.println("Headers: " + String.join(",", headers));
        return Stream.of(lines).skip(1) // header
                .map(line -> zipCSV(headers, splitCSVLine(line)))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private static List<String> splitCSVLine(String line) {
        List<String> split = Arrays.asList(line.split("\",\""));
        int size = split.size();
        if (size > 0) {
            String first = split.get(0);
            split.set(0, first.substring(1));
            String last = split.get(size - 1);
            split.set(size - 1, last.substring(0, last.length() - 1));
        }

        return split;
    }

    private static Map<String, String> zipCSV(List<String> headers, List<String> columns) {
        //        System.out.println("Row: " + String.join(",", columns));
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            map.put(headers.get(i), columns.get(i));
        }

        return map;
    }
}
