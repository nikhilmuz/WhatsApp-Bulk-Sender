package ga.nikhilkumar.whatsappsender.sender.model;

import android.content.Context;

import java.io.File;

import ga.nikhilkumar.whatsappsender.sender.Utils;

public class WMessage {
    String text;
    String mime;
    File file;
    MessageType type;
    double lat, lng;

    public WMessage(String text, File file, Context context) {
        this.text = text;
        this.file = file;
        if (file != null) {
            mime = Utils.getMimeType(context, file);
            if (mime == null)
                type = MessageType.TEXT;
            else if (mime.contains("video"))
                type = MessageType.VIDEO;
            else if (mime.contains("audio"))
                type = MessageType.AUDIO;
            else if (mime.contains("image"))
                type = MessageType.IMAGE;

        } else
            type = MessageType.TEXT;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getMime() {
        return mime;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WMessage wMessage = (WMessage) o;

        if (text != null ? !text.equals(wMessage.text) : wMessage.text != null) return false;
        if (mime != null ? !mime.equals(wMessage.mime) : wMessage.mime != null) return false;
        if (file != null ? !file.equals(wMessage.file) : wMessage.file != null) return false;
        return type == wMessage.type;

    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (mime != null ? mime.hashCode() : 0);
        result = 31 * result + (file != null ? file.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WMessage{" +
                "text='" + text + '\'' +
                ", mime='" + mime + '\'' +
                ", file=" + file +
                ", type=" + type +
                '}';
    }

    public enum MessageType {TEXT, VIDEO, IMAGE, AUDIO}
}
