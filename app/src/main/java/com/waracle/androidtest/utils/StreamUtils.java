package com.waracle.androidtest.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Riad on 20/05/2015.
 */
public class StreamUtils {
    private static final String TAG = StreamUtils.class.getSimpleName();

    // Can you see what's wrong with this???

    /*
     * I see that it does not used any Stream buffer to read the inputStream,
     * something like what it's wrote now below.
     * readUnknownFully method is in anycase useless because it retrieves the Byte array to decode
     * the image, but in the ImageLoaderHandler it can be use directly BitmapFactory.decodeStream
     * to obtain the image bitmap from the inputStream.
    * */
    public static byte[] readUnknownFully(InputStream stream) throws IOException {

        BufferedInputStream buffStream = new BufferedInputStream(stream);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int current = 0;
        while ((current = buffStream.read()) != -1) {
            buffer.write((byte) current);
        }
        return buffer.toByteArray();



        // Read in stream of bytes
        /*ArrayList<Byte> data = new ArrayList<>();
        while (true) {
            int result = stream.read();
            if (result == -1) {
                break;
            }
            data.add((byte) result);
        }*/

        // Convert ArrayList<Byte> to byte[]
        /*byte[] bytes = new byte[data.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = data.get(i);
        }

        // Return the raw byte array.
        return bytes;*/
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
