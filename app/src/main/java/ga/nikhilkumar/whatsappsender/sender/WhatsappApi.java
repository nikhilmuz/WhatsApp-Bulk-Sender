package ga.nikhilkumar.whatsappsender.sender;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import eu.chainfire.libsuperuser.Shell;
import ga.nikhilkumar.whatsappsender.sender.exception.WhatsappNotInstalledException;
import ga.nikhilkumar.whatsappsender.sender.liseteners.GetContactsListener;
import ga.nikhilkumar.whatsappsender.sender.liseteners.SendMessageListener;
import ga.nikhilkumar.whatsappsender.sender.model.WContact;
import ga.nikhilkumar.whatsappsender.sender.model.WMessage;
import ga.nikhilkumar.whatsappsender.whatsapp.MediaData;

public class WhatsappApi {
    Context launchContext = null;
    private static WhatsappApi instance;
    private static String imgFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Images/Sent";
    private static String vidFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Video/Sent";
    private static String audFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Audio/Sent";
    boolean isRootAvailable;
    private SQLiteDatabase db;

    private WhatsappApi() {

        boolean suAvailable = Shell.SU.available();
        if (suAvailable) {
            Shell.SU.run("am force-stop com.whatsapp");
            Shell.SU.run("mount -o -R rw,remount /data/data/com.whatsapp");
            Shell.SU.run("mount -o rw,remount /data/data/com.whatsapp/databases");
            Shell.SU.run("chmod 777 /data/data/com.whatsapp/databases");
            Shell.SU.run("chmod 777 /data/data/com.whatsapp/files");
            Shell.SU.run("chmod 777 /data/data/com.whatsapp/shared_prefs");
            Shell.SU.run("chmod 777 /data/data/com.whatsapp/databases/msgstore.db");
            Shell.SU.run("chmod 777 /data/data/com.whatsapp/databases/msgstore.db-wal");
            Shell.SU.run("chmod 777 /data/data/com.whatsapp/databases/msgstore.db-shm");
            Shell.SU.run("chmod 777 /data/data/com.whatsapp/databases/wa.db");
            Shell.SU.run("chmod 777 /data/data/com.whatsapp/databases/wa.db-wal");
            Shell.SU.run("chmod 777 /data/data/com.whatsapp/databases/wa.db-shm");
            Shell.SU.run("ls -l /data/data/com.whatsapp/databases/msgstore.db-shm");
            isRootAvailable = true;

        } else {

            isRootAvailable = false;
        }


    }

    public static WhatsappApi getInstance() {
        if (instance == null)
            instance = new WhatsappApi();
        return instance;
    }

    public boolean isWhatsappInstalled() {
        File file = new File("/data/data/com.whatsapp/");
        return file.exists();
    }

    public void sendMessage(WContact contact, WMessage message, Context context, SendMessageListener listener) throws WhatsappNotInstalledException {
        List<WContact> contacts = new LinkedList<>();
        contacts.add(contact);
        List<WMessage> messages = new LinkedList<>();
        messages.add(message);
        sendMessage(contacts, messages, context, listener);
    }

    @SuppressLint("StaticFieldLeak")
    public synchronized void sendMessage(final List<WContact> contacts, final List<WMessage> messages, final Context context, final SendMessageListener listener) throws WhatsappNotInstalledException {
        launchContext = context;
        if (isWhatsappInstalled()) {
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    boolean result = true;
                    Shell.SU.run("am force-stop com.whatsapp");
                    Shell.SU.run("mv /data/data/com.whatsapp/databases/msgstore.db " + context.getDatabasePath("msgstore.db").getAbsolutePath());
//                    Shell.SU.run("mv /data/data/com.whatsapp/databases/msgstore.db-wal " + context.getDatabasePath("msgstore.db").getAbsolutePath() + "-wal");
//                    Shell.SU.run("mv /data/data/com.whatsapp/databases/msgstore.db-shm " + context.getDatabasePath("msgstore.db").getAbsolutePath() + "-shm");
                    Shell.SU.run("chmod 777 " + context.getDatabasePath("msgstore.db").getAbsolutePath());
//                    Shell.SU.run("chmod 777 " + context.getDatabasePath("msgstore.db").getAbsolutePath() + "-wal");
//                    Shell.SU.run("chmod 777 " + context.getDatabasePath("msgstore.db").getAbsolutePath() + "-shm");
                    try {
                        db = SQLiteDatabase.openOrCreateDatabase(new File(context.getDatabasePath("msgstore.db").getAbsolutePath()), null);
                        for (int i = 0; i < contacts.size(); i++) {
                            try {
                                Log.e("MSG", contacts.get(i).toString());
                                sendMessage(contacts.get(i), messages.get(i));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        db.close();
                    } catch (Exception e) {
                        Log.e("SQL", e.getMessage());
                        result = false;
                    }
                    Shell.SU.run("mv " + context.getDatabasePath("msgstore.db").getAbsolutePath() + " /data/data/com.whatsapp/databases/msgstore.db");
//                    Shell.SU.run("mv " + context.getDatabasePath("msgstore.db").getAbsolutePath() + "-wal /data/data/com.whatsapp/databases/msgstore.db-wal");
//                    Shell.SU.run("mv " + context.getDatabasePath("msgstore.db").getAbsolutePath() + "-shm /data/data/com.whatsapp/databases/msgstore.db-shm");
                    Shell.SU.run("chmod 777 /data/data/com.whatsapp/databases/msgstore.db");
                    Shell.SU.run("rm /data/data/com.whatsapp/databases/msgstore.db-wal");
                    Shell.SU.run("rm /data/data/com.whatsapp/databases/msgstore.db-shm");
                    PackageManager pm = context.getPackageManager();
                    Intent intent = pm.getLaunchIntentForPackage("com.whatsapp");
                    context.startActivity(intent);
                    return result;
                }

                @Override
                protected void onPostExecute(Boolean finish) {
                    super.onPostExecute(finish);
                    if (listener != null) {
                        listener.finishSendWMessage(contacts, messages);
                    }
                }
            }.execute();
        } else
            throw new WhatsappNotInstalledException();
    }

    private void sendMessage(WContact contact, WMessage message) throws IOException {

        String name = null;
        Calendar c = null;
        String formattedDate = null;
        SimpleDateFormat df = null;
        File source = null;
        Random rand = null;
        File destination = null;

        switch (message.getType()) {
            case TEXT:
                break;
            case VIDEO:
                name = message.getFile().getPath();
                c = Calendar.getInstance();
                df = new SimpleDateFormat("yyyyMMMdd");
                formattedDate = df.format(c.getTime());
                source = new File(name);
                rand = new Random();
                destination = new File(vidFolder, "VID-" + formattedDate + "-WA" + (rand.nextInt(100) + rand.nextInt(75) + rand.nextInt(50)) + "." + FilenameUtils.getExtension(message.getFile().getName()));
                if (source.exists()) {
                    FileChannel src = new FileInputStream(source).getChannel();
                    FileChannel dst = new FileOutputStream(destination).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
                name = destination.getName();
                break;
            case IMAGE:
                name = message.getFile().getPath();
                c = Calendar.getInstance();
                df = new SimpleDateFormat("yyyyMMdd");
                formattedDate = df.format(c.getTime());
                source = new File(name);
                rand = new Random();
                destination = new File(imgFolder, "IMG-" + formattedDate + "-WA" + (rand.nextInt(100) + rand.nextInt(75) + rand.nextInt(50)) + "." + FilenameUtils.getExtension(message.getFile().getName()));
                if (source.exists()) {
                    FileChannel src = new FileInputStream(source).getChannel();
                    FileChannel dst = new FileOutputStream(destination).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
                name = destination.getName();
                break;
            case AUDIO:
                name = message.getFile().getPath();
                c = Calendar.getInstance();
                df = new SimpleDateFormat("yyyyMMdd");
                formattedDate = df.format(c.getTime());
                source = new File(name);
                rand = new Random();
                destination = new File(audFolder, "AUD-" + formattedDate + "-WA" + (rand.nextInt(100) + rand.nextInt(75) + rand.nextInt(50)) + "." + FilenameUtils.getExtension(message.getFile().getName()));
                if (source.exists()) {
                    FileChannel src = new FileInputStream(source).getChannel();
                    FileChannel dst = new FileOutputStream(destination).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
                name = destination.getName();
                break;
        }
        sendBigMessage(contact.getId(), message.getText(), name, message.getMime());
    }

    @SuppressLint("StaticFieldLeak")
    public synchronized void getContacts(Context context, final GetContactsListener listener) throws WhatsappNotInstalledException {

        if (isWhatsappInstalled()) {
            new AsyncTask<Void, Void, List<WContact>>() {
                @Override
                protected List<WContact> doInBackground(Void... params) {
                    Shell.SU.run("am force-stop com.whatsapp");
                    db = SQLiteDatabase.openOrCreateDatabase(new File("/data/data/com.whatsapp/databases/wa.db"), null);
                    List<WContact> contactList = new LinkedList<>();
                    String selectQuery = "SELECT  jid, display_name FROM wa_contacts where phone_type is not null and is_whatsapp_user = 1";
                    Cursor cursor = db.rawQuery(selectQuery, null);
                    if (cursor.moveToFirst()) {
                        do {
                            WContact contact = new WContact(cursor.getString(1), cursor.getString(0));
                            contactList.add(contact);
                        } while (cursor.moveToNext());
                    }
                    db.close();
                    return contactList;
                }

                @Override
                protected void onPostExecute(List<WContact> contacts) {
                    super.onPostExecute(contacts);
                    if (listener != null) {
                        listener.receiveWhatsappContacts(contacts);
                    }
                }
            }.execute();


        } else
            throw new WhatsappNotInstalledException();


    }

    private void sendBigMessage(String jid, String msg, String file, String mimeType) {
        long l1;
        long l2;
        int k;
        String query2, query1;

        Random localRandom = new Random(20L);
        l1 = System.currentTimeMillis();
        l2 = l1 / 1000L;
        k = localRandom.nextInt();

        int mediaType = 0;

        if (mimeType == null || mimeType.length() < 2)
            mediaType = 0;
        else
            mediaType = (mimeType.contains("video")) ? 3
                    : (mimeType.contains("image")) ? 1
                    : (mimeType.contains("audio")) ? 2
                    : 0;

        ContentValues initialValues = new ContentValues();
        initialValues.put("key_remote_jid", jid);
        initialValues.put("key_from_me", 1);
        initialValues.put("key_id", l2 + "-" + k);
        initialValues.put("status", 1);
        initialValues.put("needs_push", 0);
        initialValues.put("timestamp", l1);
        initialValues.put("media_wa_type", mediaType);
        initialValues.put("media_name", file);
        initialValues.put("latitude", 0.0);
        initialValues.put("longitude", 0.0);
        initialValues.put("received_timestamp", l1);
        initialValues.put("send_timestamp", -1);
        initialValues.put("receipt_server_timestamp", -1);
        initialValues.put("receipt_device_timestamp", -1);
        initialValues.put("raw_data", -1);
        initialValues.put("recipient_count", 0);
        initialValues.put("media_duration", 0);

        if (!TextUtils.isEmpty(file) && !TextUtils.isEmpty(mimeType)) {
            Bitmap bMap = null;
            File spec;
            if (mediaType == 3) {
                spec = new File(vidFolder, file);
                bMap = ThumbnailUtils.createVideoThumbnail(spec.getAbsolutePath(), MediaStore.Video.Thumbnails.MICRO_KIND);
            } else if (mediaType == 2) {
                spec = new File(audFolder, file);
            } else {
                spec = new File(imgFolder, file);
                bMap = BitmapFactory.decodeFile(spec.getAbsolutePath());
            }
            long mediaSize = (file.equals("")) ? 0 : spec.length();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            if (mediaType == 1 || mediaType == 3) {
                bMap = Bitmap.createScaledBitmap(bMap, 100, 59, false);
                bMap.compress(Bitmap.CompressFormat.JPEG, 60, bos);
            }
            byte[] bArray = bos.toByteArray();

            MediaData md = new MediaData();
            md.fileSize = mediaSize;
            md.file = spec;
            md.autodownloadRetryEnabled = true;
            byte[] arr = SerializationUtils.serialize(md);

            initialValues.put("thumb_image", arr);
            initialValues.put("quoted_row_id", 0);
            initialValues.put("raw_data", bArray);
            initialValues.put("media_size", mediaSize);
            initialValues.put("origin", 0);
            initialValues.put("media_caption", msg);
        } else
            initialValues.put("data", msg);

        long idm = db.insert("messages", null, initialValues);

        query1 = " insert into chat_list (key_remote_jid) select '" + jid
                + "' where not exists (select 1 from chat_list where key_remote_jid='" + jid + "');";

        query2 = " update chat_list set message_table_id = (select max(messages._id) from messages) where chat_list.key_remote_jid='" + jid + "';";


        ContentValues values = new ContentValues();
        values.put("docid", idm);
        values.put("c0content", "null  ");
        db.insert("messages_fts_content", null, values);


        db.execSQL(query1 + query2);
    }

    public boolean isRootAvailable() {
        return isRootAvailable;
    }
}
