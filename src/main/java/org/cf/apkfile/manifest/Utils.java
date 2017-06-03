package org.cf.apkfile.manifest;

class Utils {
    static String ensureFullName(String componentName, String packageName) {
        String name;
        if (componentName.isEmpty()) {
            name = componentName;
        } else if (componentName.startsWith(".")) {
            name = packageName + componentName;
        } else if (!componentName.contains(".")) {
            name = packageName + "." + componentName;
        } else {
            name = componentName;
        }
        return name;
    }
}
