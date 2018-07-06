# apkfile

ApkFile is a library which creates a representation of an APK composed of Java objects which can be easily inspected for analysis or serialized into JSON. The goal of the library is to provide a robust way to inspect hostile malware samples, but it's general purpose enough to be used for other stuff too.

This library and [APKiD](https://github.com/rednaga/APKiD) provide the machine learning feature extraction for [Judge](http://judge.rednaga.io/), an Android malware detection engine. 

## Usage

The main object is `ApkFile` and it extends `java.io.File`:

```java
ApkFile apkFile = new ApkFileFactory().build('ApiDemos.apk');
AndroidManifest androidManifest = apkFile.getAndroidManifest();
String packageName = androidManifest.getPackageName(); // com.example.android.apis
```

`ApkFile` provides objects for:

* Android manifest
* Resources via modified & hardened [ArscBlamer](https://github.com/google/android-arscblamer)
* Signing certificate
* DEX files via [dexlib2](https://github.com/JesusFreke/smali/tree/master/dexlib2)
    * Classes, methods, etc.
* APK entries

## Serializing to JSON

Below is an example of how to stream an `ApkFile` to JSON:

```java
ApkFile apkFile = new ApkFileFactory().build("ignore/DroidSwarm-1.0.1.apk");
// ApkFile uses Trove library for speed. This needs some type adapters to
// properly convert into JSON.
Gson gson = Utils.getTroveAwareGsonBuilder()
        .disableHtmlEscaping()
        .serializeSpecialFloatingPointValues()
        .setExclusionStrategies(new JarFileExclusionStrategy())
        .setPrettyPrinting()
        .create();

// Since the JSON is usually very large and takes up a lot of memory, stream it out.
Writer writer = new OutputStreamWriter(System.out);
gson.toJson(apkFile, writer);
writer.close();
apkFile.close();
```

Below is the highly abbreviated output from the code above. A full version can be found [here](doc/droidswarm.json).

```json
{
  "androidManifest": {
    "application": {
      "activities": [
        {
          "allowEmbedded": false,
          "allowTaskReparenting": false,
          "alwaysRetainTaskState": false,
          "autoRemoveFromRecents": false,
          "banner": "",
          "clearTaskOnLaunch": false,
          "configChanges": 0,
          "documentLaunchMode": 0,
          "excludeFromRecents": false,
          "finishOnTaskLaunch": false,
          "hardwareAccelerated": false,
          "launchMode": 0,
          "maxRecents": 16,
          "multiprocess": false,
          "noHistory": false,
          "parentActivityName": "",
          "persistableMode": 0,
          "relinquishTaskIdentity": false,
          "resizeableActivity": false,
          "screenOrientation": 0,
          "showForAllUsers": false,
          "stateNotNeeded": false,
          "supportsPictureInPicture": false,
          "taskAffinity": "",
          "theme": "",
          "uiOptions": 0,
          "windowSoftInputMode": 0,
          "intentFilters": [
            {
              "actions": [
                "android.intent.action.MAIN"
              ],
              "categories": [
                "android.intent.category.LAUNCHER"
              ],
              "data": [],
              "icon": "",
              "label": "",
              "priority": 0
            }
          ],
          "metaData": [],
          "directBootAware": false,
          "enabled": true,
          "exported": false,
          "icon": "",
          "label": "DroidSwarm",
          "name": "com.soong.droidswarm.Main",
          "permission": "",
          "process": ""
        },
        // ** SNIP **
      ],
      "activityAliases": [],
      "allowTaskReparenting": false,
      "allowBackup": true,
      "backupAgent": "",
      "backupInForeground": false,
      "banner": "",
      "debuggable": true,
      "description": "",
      "directBootAware": false,
      "enabled": true,
      "extractNativeLibs": true,
      "fullBackupContent": "",
      "fullBackupOnly": false,
      "hardwareAccelerated": true,
      "hasCode": true,
      "icon": "res/drawable-mdpi/ic_launcher.png",
      "isGame": false,
      "killAfterRestore": true,
      "largeHeap": false,
      "label": "DroidSwarm",
      "logo": "",
      "manageSpaceActivity": "",
      "name": "",
      "networkSecurityConfig": "",
      "permission": "",
      "persistent": false,
      "process": "",
      "providers": [],
      "receivers": [],
      "restoreAnyVersion": false,
      "requiredAccountType": "",
      "resizableActivity": false,
      "supportsRtl": false,
      "services": [
        {
          "isolatedProcess": false,
          "intentFilters": [],
          "metaData": [],
          "directBootAware": false,
          "enabled": true,
          "exported": false,
          "icon": "",
          "label": "",
          "name": "com.soong.droidswarm.SwarmService",
          "permission": "",
          "process": ""
        }
      ],
      "taskAffinity": "",
      "theme": "",
      "uiOptions": 0,
      "usesCleartextTraffic": true,
      "vmSafeMode": false,
      "usesLibraries": []
    },
    "compatibleScreens": [],
    "hasResources": true,
    "installLocation": 0,
    "instrumentations": [],
    "maxSdkVersion": 0,
    "minSdkVersion": 7,
    "packageName": "com.soong.droidswarm",
    "permissionGroups": [],
    "permissionTrees": [],
    "permissions": [],
    "platformBuildVersionCode": -1,
    "platformBuildVersionName": "",
    "sharedUserId": "",
    "sharedUserLabel": "",
    "supportsGlTextures": [],
    "targetSdkVersion": 0,
    "usesConfigurations": [],
    "usesFeatures": [],
    "usesPermissions": [
      "android.permission.INTERNET",
      "android.permission.WRITE_EXTERNAL_STORAGE",
      "android.permission.ACCESS_NETWORK_STATE",
      "android.permission.CHANGE_WIFI_STATE"
    ],
    "versionCode": 5,
    "versionName": "0.9.2"
  },
  "certificate": {
    "allRdns": [
      {
        "issuerRdns": {
          "C": "US",
          "CN": "Android Debug",
          "O": "Android"
        },
        "subjectRdns": {
          "C": "US",
          "CN": "Android Debug",
          "O": "Android"
        }
      }
    ]
  },
  "entryNameToDex": {
    "classes.dex": {
      "apiCounts": {
        "Landroid/text/Editable;->toString()Ljava/lang/String;": 8,
        "Ljava/util/concurrent/ThreadPoolExecutor;-><init>(IIJLjava/util/concurrent/TimeUnit;Ljava/util/concurrent/BlockingQueue;Ljava/util/concurrent/RejectedExecutionHandler;)V": 1,
        "Landroid/content/pm/PackageManager;->getPackageInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;": 2,
        "Ljava/lang/Object;->toString()Ljava/lang/String;": 1,
        "Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;": 2,
        "Landroid/widget/CheckBox;->isChecked()Z": 1,
        "Landroid/widget/ListView;->setAdapter(Landroid/widget/ListAdapter;)V": 2,
        "Ljava/lang/CharSequence;->toString()Ljava/lang/String;": 1,
        "Ljava/util/Iterator;->hasNext()Z": 9,
        "Ljava/lang/String;->toLowerCase()Ljava/lang/String;": 1,
        "Landroid/widget/SeekBar;->getMax()I": 1,
        "Landroid/app/AlertDialog;->setMessage(Ljava/lang/CharSequence;)V": 1,
        // ** SNIP **
      },
      "classAccessorCounts": {
        "interface": 2,
        "final": 7,
        "protected": 0,
        "private": 0,
        "synchronized": 0,
        "abstract": 2,
        "native": 0,
        "volatile": 0,
        "transient": 0,
        "public": 22,
        "static": 0,
        "strict": 0
      },
      "classPathToClass": {
        // * SNIP **
        "Lcom/soong/droidswarm/SwarmMonitor;": {
          "apiCounts": {
            "Ljava/lang/Thread;->sleep(J)V": 1,
            "Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;": 2,
            "Ljava/io/PrintStream;->println(Ljava/lang/String;)V": 1,
            "Ljava/util/ArrayList;->get(I)Ljava/lang/Object;": 1,
            "Ljava/util/concurrent/ThreadPoolExecutor;->getMaximumPoolSize()I": 1,
            "Ljava/util/concurrent/ThreadPoolExecutor;->execute(Ljava/lang/Runnable;)V": 1,
            "Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;": 5,
            "Ljava/util/concurrent/ThreadPoolExecutor;->getActiveCount()I": 1,
            "Ljava/util/concurrent/ThreadPoolExecutor;->getTaskCount()J": 1,
            "Ljava/lang/Object;-><init>()V": 1,
            "Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;": 1,
            "Ljava/util/concurrent/ThreadPoolExecutor;->getPoolSize()I": 2,
            "Ljava/util/concurrent/ThreadPoolExecutor;->isShutdown()Z": 3,
            "Ljava/util/concurrent/ThreadPoolExecutor;->getCorePoolSize()I": 1,
            "Ljava/util/concurrent/ThreadPoolExecutor;->getCompletedTaskCount()J": 1,
            "Ljava/util/concurrent/ThreadPoolExecutor;->isTerminated()Z": 1
          },
          "classAccessors": {
            "interface": 0,
            "final": 0,
            "protected": 0,
            "private": 0,
            "synchronized": 0,
            "abstract": 0,
            "native": 0,
            "volatile": 0,
            "transient": 0,
            "public": 1,
            "static": 0,
            "strict": 0
          },
          "fieldReferenceCounts": {
            "Ljava/lang/System;->out:Ljava/io/PrintStream;": 1
          },
          "methodAccessorCounts": {
            "interface": 0,
            "final": 0,
            "protected": 0,
            "private": 0,
            "synchronized": 0,
            "abstract": 0,
            "native": 0,
            "volatile": 0,
            "transient": 0,
            "public": 2,
            "static": 0,
            "strict": 0
          },
          "methodSignatureToMethod": {
            "run()V": {
              "apiCounts": {
                "Ljava/util/concurrent/ThreadPoolExecutor;->getActiveCount()I": 1,
                "Ljava/lang/Thread;->sleep(J)V": 1,
                "Ljava/util/concurrent/ThreadPoolExecutor;->execute(Ljava/lang/Runnable;)V": 1,
                "Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;": 2,
                "Ljava/util/concurrent/ThreadPoolExecutor;->getCompletedTaskCount()J": 1,
                "Ljava/util/ArrayList;->get(I)Ljava/lang/Object;": 1,
                "Ljava/util/concurrent/ThreadPoolExecutor;->getMaximumPoolSize()I": 1,
                "Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;": 5,
                "Ljava/util/concurrent/ThreadPoolExecutor;->getTaskCount()J": 1,
                "Ljava/util/concurrent/ThreadPoolExecutor;->isTerminated()Z": 1,
                "Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;": 1,
                "Ljava/util/concurrent/ThreadPoolExecutor;->getPoolSize()I": 2,
                "Ljava/util/concurrent/ThreadPoolExecutor;->getCorePoolSize()I": 1,
                "Ljava/io/PrintStream;->println(Ljava/lang/String;)V": 1,
                "Ljava/util/concurrent/ThreadPoolExecutor;->isShutdown()Z": 3
              },
              "fieldReferenceCounts": {
                "Ljava/lang/System;->out:Ljava/io/PrintStream;": 1
              },
              "methodAccessors": {
                "interface": 0,
                "final": 0,
                "protected": 0,
                "private": 0,
                "synchronized": 0,
                "abstract": 0,
                "native": 0,
                "volatile": 0,
                "transient": 0,
                "public": 1,
                "static": 0,
                "strict": 0
              },
              "opCounts": {
                "MOVE_RESULT": 12,
                "IF_NEZ": 1,
                "ADD_INT_LIT8": 1,
                "IF_GE": 1,
                "IF_LEZ": 1,
                "MOVE_EXCEPTION": 1,
                "SGET_OBJECT": 1,
                "MOVE_RESULT_WIDE": 2,
                "REM_INT": 1,
                "GOTO": 4,
                "IF_EQZ": 5,
                "CHECK_CAST": 1,
                "CONST_STRING": 1,
                "NEW_ARRAY": 1,
                "INVOKE_DIRECT": 1,
                "IF_LE": 1,
                "MOVE_RESULT_OBJECT": 9,
                "INVOKE_STATIC": 9,
                "IGET_OBJECT": 17,
                "IGET_BOOLEAN": 2,
                "RETURN_VOID": 1,
                "APUT_OBJECT": 7,
                "INVOKE_VIRTUAL": 17,
                "NEW_INSTANCE": 1,
                "MOVE": 4,
                "CONST_4": 12,
                "CONST_WIDE_16": 1
              },
              "stringReferenceCounts": {
                "[swarm-mon] [%d/%d] active:%d, completed:%d task:%d isShutdown:%s isTerminated:%s": 1
              },
              "annotationCount": 0,
              "cyclomaticComplexity": 19,
              "debugItemCount": 31,
              "instructionCount": 115,
              "registerCount": 11,
              "tryCatchCount": 1
            },
            "<init>(Ljava/util/concurrent/ThreadPoolExecutor;Lcom/soong/droidswarm/TargetList;)V": {
              "apiCounts": {
                "Ljava/lang/Object;-><init>()V": 1
              },
              "fieldReferenceCounts": {},
              "methodAccessors": {
                "interface": 0,
                "final": 0,
                "protected": 0,
                "private": 0,
                "synchronized": 0,
                "abstract": 0,
                "native": 0,
                "volatile": 0,
                "transient": 0,
                "public": 1,
                "static": 0,
                "strict": 0
              },
              "opCounts": {
                "RETURN_VOID": 1,
                "INVOKE_DIRECT": 1,
                "CONST_4": 1,
                "IPUT_BOOLEAN": 1,
                "IPUT_OBJECT": 2
              },
              "stringReferenceCounts": {},
              "annotationCount": 0,
              "cyclomaticComplexity": 1,
              "debugItemCount": 6,
              "instructionCount": 6,
              "registerCount": 4,
              "tryCatchCount": 0
            }
          },
          "opCounts": {
            "MOVE_RESULT": 12,
            "IF_NEZ": 1,
            "ADD_INT_LIT8": 1,
            "IF_GE": 1,
            "IF_LEZ": 1,
            "MOVE_EXCEPTION": 1,
            "SGET_OBJECT": 1,
            "MOVE_RESULT_WIDE": 2,
            "REM_INT": 1,
            "GOTO": 4,
            "IF_EQZ": 5,
            "CHECK_CAST": 1,
            "NEW_ARRAY": 1,
            "INVOKE_DIRECT": 2,
            "IPUT_BOOLEAN": 1,
            "IF_LE": 1,
            "NEW_INSTANCE": 1,
            "MOVE_RESULT_OBJECT": 9,
            "INVOKE_STATIC": 9,
            "IGET_OBJECT": 17,
            "IPUT_OBJECT": 2,
            "IGET_BOOLEAN": 2,
            "RETURN_VOID": 2,
            "APUT_OBJECT": 7,
            "INVOKE_VIRTUAL": 17,
            "CONST_STRING": 1,
            "MOVE": 4,
            "CONST_4": 13,
            "CONST_WIDE_16": 1
          },
          "stringReferenceCounts": {
            "[swarm-mon] [%d/%d] active:%d, completed:%d task:%d isShutdown:%s isTerminated:%s": 1
          },
          "annotationCount": 0,
          "cyclomaticComplexity": 10.0,
          "debugItemCount": 37,
          "fieldCount": 3,
          "instructionCount": 121,
          "registerCount": 15,
          "tryCatchCount": 1,
          "failedMethods": 2
        },
      },
      "fieldReferenceCounts": {
        "Landroid/widget/AdapterView$AdapterContextMenuInfo;->position:I": 1,
        "Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;": 1,
        "Ljava/util/concurrent/TimeUnit;->SECONDS:Ljava/util/concurrent/TimeUnit;": 1,
        "Ljava/lang/Integer;->TYPE:Ljava/lang/Class;": 1,
        "Landroid/app/Notification;->flags:I": 2,
        "Ljava/lang/Boolean;->TYPE:Ljava/lang/Class;": 2,
        "Ljava/lang/System;->out:Ljava/io/PrintStream;": 2,
        "Landroid/os/Message;->what:I": 1,
        "Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;": 2,
        "Landroid/content/pm/PackageInfo;->versionName:Ljava/lang/String;": 2,
        "Landroid/os/Message;->obj:Ljava/lang/Object;": 2
      },
      "methodAccessorCounts": {
        "interface": 0,
        "final": 0,
        "protected": 12,
        "private": 28,
        "synchronized": 0,
        "abstract": 2,
        "native": 0,
        "volatile": 0,
        "transient": 0,
        "public": 66,
        "static": 23,
        "strict": 0
      },
      "methodDescriptorToMethod": {
        "Lcom/soong/droidswarm/ExternalStorage$1;->onReceive(Landroid/content/Context;Landroid/content/Intent;)V": {
          "apiCounts": {
            "Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I": 1,
            "Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;": 1,
            "Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V": 1,
            "Landroid/content/Intent;->getData()Landroid/net/Uri;": 1,
            "Ljava/lang/StringBuilder;->toString()Ljava/lang/String;": 1
          },
          "fieldReferenceCounts": {},
          "methodAccessors": {
            "interface": 0,
            "final": 0,
            "protected": 0,
            "private": 0,
            "synchronized": 0,
            "abstract": 0,
            "native": 0,
            "volatile": 0,
            "transient": 0,
            "public": 1,
            "static": 0,
            "strict": 0
          },
          "opCounts": {
            "INVOKE_DIRECT": 1,
            "NEW_INSTANCE": 1,
            "INVOKE_STATIC": 2,
            "MOVE_RESULT_OBJECT": 3,
            "INVOKE_VIRTUAL": 3,
            "RETURN_VOID": 1,
            "CONST_STRING": 2
          },
          "stringReferenceCounts": {
            "DroidSwarm": 1,
            "Watching storage: ": 1
          },
          "annotationCount": 0,
          "cyclomaticComplexity": 1,
          "debugItemCount": 4,
          "instructionCount": 13,
          "registerCount": 6,
          "tryCatchCount": 0
        },
      },
      "opCounts": {
        "INVOKE_INTERFACE": 39,
        "FILL_ARRAY_DATA": 1,
        "NEW_ARRAY": 10,
        "SUB_LONG_2ADDR": 1,
        "IF_EQZ": 39,
        "IGET_BOOLEAN": 5,
        "MOVE_OBJECT": 6,
        // ** SNIP **
        "IF_GE": 2,
        "PACKED_SWITCH": 1
      },
      "stringReferenceCounts": {
        "Service disconnected.": 1,
        "File not found.": 2,
        "Importing target ": 1,
        "name": 2,
        "No": 1,
        "Name required.": 1,
        "http://": 1,
        "Received start id ": 1,
        // ** SNIP **
        "OVER 9000!!": 1,
        "": 9
      },
      "annotationCount": 2,
      "cyclomaticComplexity": 1.888722,
      "debugItemCount": 1310,
      "fieldCount": 161,
      "instructionCount": 2684,
      "registerCount": 604,
      "tryCatchCount": 45,
      "failedClasses": 39
    }
  },
  "entryNameToZipEntry": {
    // ** SNIP **
    "AndroidManifest.xml": {
      "name": "AndroidManifest.xml",
      "xdostime": 1128830327,
      "crc": 3833663256,
      "size": 2672,
      "csize": 846,
      "method": 8,
      "flag": 2056
    },
   "classes.dex": {
      "name": "classes.dex",
      "xdostime": 1128830327,
      "crc": 2086375952,
      "size": 54324,
      "csize": 21960,
      "method": 8,
      "flag": 2056
    },
    "META-INF/MANIFEST.MF": {
      "name": "META-INF/MANIFEST.MF",
      "xdostime": 1128830327,
      "crc": 1715315727,
      "size": 1314,
      "csize": 622,
      "method": 8,
      "flag": 2056
    }
  },
  "path": "ignore/DroidSwarm-1.0.1.apk"
}
```

## License

```
Copyright 2018 RedNaga. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
