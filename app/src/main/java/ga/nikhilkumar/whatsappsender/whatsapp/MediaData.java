package ga.nikhilkumar.whatsappsender.whatsapp;

import java.io.File;
import java.io.Serializable;

public class MediaData implements Serializable {

    private static final long serialVersionUID = -3211751283609594L;

    public File file;

    public long fileSize, progress, trimFrom, trimTo;
    public int width, height, faceX, faceY, failErrorCode, suspiciousContent;
    public String uploadUrl;

    public boolean transcoded, transferred, autodownloadRetryEnabled;

    byte[] hmacKey, iv, mediaKey, refKey;

    public MediaData() {

    }
}
