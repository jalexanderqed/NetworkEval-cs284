package ninja.jalexander.networkeval;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by jalex on 2/27/2018.
 */

public class DataPoster {
    private static final String urlStr = "http://13.57.60.32:8080/data";

    public static int post(String data) {
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL");
        }

        int responseCode = -1;

        try{
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            BufferedWriter writer = new BufferedWriter (new OutputStreamWriter(out, "UTF-8"));

            writer.write(data);
            writer.flush();
            responseCode = connection.getResponseCode();

            writer.close();
            out.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return responseCode;
    }
}
