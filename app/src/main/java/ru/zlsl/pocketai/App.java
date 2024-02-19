package ru.zlsl.pocketai;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.FileUtils;
import android.util.Log;

import androidx.core.os.LocaleListCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import ru.zlsl.pocketai.types.Character;


public class App extends Application {
    private static App instance;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static SharedPreferences sp;

    public enum message_type {
        BACKGROUND,
        USER,
        CHARACTER
    }

    public static List<Character> Characters = new ArrayList<>();

    public static void NewCharacter(String name) {
        if (!Characters.contains(name)) {
            Character c = new Character();
            c.name = name;
            Characters.add(c);
        }
        SaveCharacters();
    }

    private static void SaveCharacters() {
        try {
            FileWriter out = new FileWriter(new File(App.getContext().getFilesDir(), "characters.db"));
            for (Character c : Characters) {
                out.write(c.name + "\n");
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void LoadCharacters() {
        Characters.clear();
        if (!new File(App.getContext().getFilesDir(), "characters.db").exists()) {
            return;
        }
        try {
            FileInputStream fis = App.getContext().openFileInput("characters.db");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isEmpty()) {
                    NewCharacter(line);
                }
            }

            bufferedReader.close();
            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ModelParamerers {
        public String API_SERVER = "http://10.32.0.6:5000/";
        public String API_KEY = "";

        public int max_tokens = 150;
        public float temperature = 0.7F;
        public float repetition_penalty = 1.15F;
        public float frequency_penalty = 0.05F;

        public String UserName = "Я";
        public boolean FixedCharacters = true;
        public boolean AllowOwn = false;
        public boolean UseCharToken = true;


        public void LoadState() {
            String lng = Locale.getDefault().getLanguage();
            if (lng.equals("ru")) {
                UserName = sp.getString("UserName", "Я");
            } else {
                UserName = sp.getString("UserName", "Me");
            }

            API_SERVER = sp.getString("API_SERVER", "http://127.0.0.1:5000");
            API_KEY = sp.getString("API_KEY", "");


            FixedCharacters = sp.getBoolean("FixedCharacters", true);
            AllowOwn = sp.getBoolean("AllowOwn", false);
            UseCharToken = sp.getBoolean("UseCharToken", true);

            max_tokens = sp.getInt("max_tokens", 150);
            temperature = sp.getFloat("temperature", 0.7F);
            repetition_penalty = sp.getFloat("repetition_penalty", 1.15F);
            frequency_penalty = sp.getFloat("frequency_penalty", 0.05F);

            SaveState();
        }

        public void SaveState() {
            SharedPreferences.Editor e = sp.edit();

            e.putString("API_SERVER", API_SERVER);
            e.putString("API_KEY", API_KEY);

            e.putString("UserName", UserName);

            e.putBoolean("FixedCharacters", FixedCharacters);
            e.putBoolean("AllowOwn", AllowOwn);
            e.putBoolean("UseCharToken", UseCharToken);

            e.putInt("max_tokens", max_tokens);
            e.putFloat("temperature", temperature);
            e.putFloat("repetition_penalty", repetition_penalty);
            e.putFloat("frequency_penalty", frequency_penalty);

            e.apply();
        }
    }

    public static ModelParamerers modelParameters;

    public static String getAPIServer() {
        return modelParameters.API_SERVER;
    }

    public static final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(100000, TimeUnit.MILLISECONDS)
            .readTimeout(100000, TimeUnit.MILLISECONDS)
            .writeTimeout(100000, TimeUnit.MILLISECONDS)
            .followRedirects(true)
            .build();

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();

        sp = getSharedPreferences("Settings", MODE_PRIVATE);

        modelParameters = new ModelParamerers();
        modelParameters.LoadState();

        LoadCharacters();
    }

    public static Context getContext() {
        return instance;
    }

    public static Request getRequestBuilder(String url) {
        return new Request.Builder()
                .url(url)
                .build();
    }

    public static Request getJSONRequestBuilder(String url, String json) {
        RequestBody body = RequestBody.create(json, JSON);

        return new Request.Builder()
                .url(url)
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("content-type", "application/json")
                .post(body)
                .build();
    }

    public static String Beautify(String tmp) {
        tmp = tmp.trim().replaceAll("<[^>]*>", ""); //remove tags
        if (tmp.startsWith("-") || tmp.startsWith("–") || tmp.startsWith("—")) {
            tmp = tmp.substring(1);
        }

        int last1 = tmp.lastIndexOf(".");
        int last2 = tmp.lastIndexOf("!");
        int last3 = tmp.lastIndexOf("?");

        int pos = Math.max(last1, last2);
        pos = Math.max(pos, last3);

        if (pos == -1) {
            tmp = tmp.trim() + ".";
        }

        return tmp.substring(0, pos + 1).trim();
    }

    public static String GetDialogInfo(String filename) {
        String title = "?";

        try {
            FileInputStream fis = App.instance.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("*")) {
                    title = line.replace("*", "");
                    break;
                }
            }

            bufferedReader.close();
            isr.close();
            fis.close();
        } catch (IOException e) {
            Log.e("GDI", "err");
            e.printStackTrace();
        }
        return title;
    }

    public static String GetAPIObject(String prompt) {
        JSONObject j = new JSONObject();
        try {
            j.put("prompt", prompt);
            j.put("max_tokens", modelParameters.max_tokens);
            j.put("temperature", modelParameters.temperature);
            j.put("repetition_penalty", modelParameters.repetition_penalty);
            j.put("frequency_penalty", modelParameters.frequency_penalty);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return j.toString();
    }

    public static Color GetColor(String text, Color background) {
        int backgroundColor = background.toArgb();

        // Generate the color based on the text
        int textColor = generateColor(text, backgroundColor);

        // Print the RGB values of the generated color
        int red = Color.red(textColor);
        int green = Color.green(textColor);
        int blue = Color.blue(textColor);
        return Color.valueOf(red, green, blue);
    }

    public static int generateColor(String text, int backgroundColor) {
        // Calculate the hash code of the text
        int hashCode = text.hashCode();

        // Create a random object with the hash code as the seed
        Random random = new Random(hashCode);

        // Generate random RGB values for the text color
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // Ensure contrast with the background color
        while (getContrastRatio(Color.rgb(red, green, blue), backgroundColor) < 4.5) {
            red = random.nextInt(256);
            green = random.nextInt(256);
            blue = random.nextInt(256);
        }

        // Return the generated color
        return Color.rgb(red, green, blue);
    }

    public static double getContrastRatio(int color1, int color2) {
        double luminance1 = getLuminance(color1);
        double luminance2 = getLuminance(color2);

        if (luminance1 > luminance2) {
            return (luminance1 + 0.05) / (luminance2 + 0.05);
        } else {
            return (luminance2 + 0.05) / (luminance1 + 0.05);
        }
    }

    public static double getLuminance(int color) {
        double red = Color.red(color) / 255.0;
        double green = Color.green(color) / 255.0;
        double blue = Color.blue(color) / 255.0;

        red = red <= 0.03928 ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);
        green = green <= 0.03928 ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);
        blue = blue <= 0.03928 ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);

        return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
    }

    public static boolean IsFemale(String name) {
        String fends = "аеья";
        String c = name.substring(name.length() - 1);
        return fends.contains(c);
    }

    public static void saveFileFromUri(Uri uri, String filename) {
        try {
            File file = new File(getContext().getFilesDir(), filename);
            if (!file.createNewFile()) {
                Log.e("sffu", "Error creating file");
            }
            OutputStream outputStream = new FileOutputStream(file);
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            FileUtils.copy(inputStream, outputStream);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}