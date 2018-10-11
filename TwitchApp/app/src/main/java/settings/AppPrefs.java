package settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.preference.PreferenceManager;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.possiblemobile.twitchapp.model.UserInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class AppPrefs {

    public static final String CLIENT_ID =  "vgl10ogqr6s8xqotaxc5256log6txm";
    private String TWITCH_TOKEN = "4q9tjj1klurxptl8e0zz692w44b0qj";
    private static UserInfo userInfo;


    public String getTwitchAccessToken() {
        return TWITCH_TOKEN;
    }

    public  String getTwitchName() {
        return  "varshateratipally";
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public boolean isUserLoggedIn() {
        return true;
    }

    public static int dpToPixels(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }


    public static String urlToJSONString(String urlToRead) {

        URL url;
        HttpURLConnection conn = null;
        Scanner in = null;
        String result = "";

        try {
            url = new URL(urlToRead);

            conn = openConnection(url);

            conn.setReadTimeout(5000);
            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Client-ID", AppPrefs.CLIENT_ID);
            conn.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
            conn.setRequestMethod("GET");
            in = new Scanner(new InputStreamReader(conn.getInputStream()));

            while (in.hasNextLine()) {
                String line = in.nextLine();
                result += line;
            }

            in.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                in.close();
            if (conn != null)
                conn.disconnect();
        }

        if (result.length() == 0 ||( result.length() >1 && result.charAt(0)!='{')) {
            Log.v("URL TO JSON String", result);
            Log.v("URL TO JSON STRING", urlToRead + " did not successfully get read");
            Log.v("URL TO JSON STRING", "Result of reading - " + result);
        }

        return result;
    }

    public static HttpURLConnection openConnection(URL url) throws IOException {

        return (HttpURLConnection) url.openConnection();
    }

    public static Bitmap getImageFromStorage(String key, Context context) throws IOException {
        InputStream fileIn = context.openFileInput(key);
        return BitmapFactory.decodeStream(fileIn);
    }

    public static boolean doesStorageFileExist(String key, Context context) {
        File file = context.getFileStreamPath(key);
        return file.exists();
    }

    public static Bitmap getBitmapFromUrl(String url) {
        Bitmap bitmap = null;

        try {
            HttpURLConnection connection = openConnection(new URL(url));
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            if (url.contains("https")) {
                return getBitmapFromUrl(url.replace("https", "http"));
            }
        }

        return bitmap;
    }

    public static int getColorAttribute(@AttrRes int attribute, @ColorRes int defaultColor, Context context) {
        TypedValue a = new TypedValue();
        context.getTheme().resolveAttribute(attribute, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return a.data;
        } else {
            return ContextCompat.getColor(context, defaultColor);
        }
    }



    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int dpHeight, Context context) {
        try {
            Bitmap.Config mConfig = bm.getConfig() == null ? Bitmap.Config.ARGB_8888 : bm.getConfig();

            Bitmap resizedBitmap = bm.copy(mConfig, true);
            int heightPx = dpToPixels(context, dpHeight);
            int widthPx = (int) ((1.0 * resizedBitmap.getWidth() / resizedBitmap.getHeight()) * (heightPx * 1.0));
            return getResizedBitmap(resizedBitmap, widthPx, heightPx);
        } catch (Exception e) {
            return null;
        }
    }

    public static void saveImageToStorage(Bitmap image, String key, Context context) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, byteStream);

            FileOutputStream fileOut = context.openFileOutput(key, Context.MODE_PRIVATE);
            fileOut.write(byteStream.toByteArray());
            byteStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
