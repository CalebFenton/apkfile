package org.cf.apkfile.manifest;

import java.io.IOException;

class AndroidManifestParsingException extends IOException {

    AndroidManifestParsingException(Throwable throwable) {
        super(throwable.getMessage(), throwable);
    }
}
