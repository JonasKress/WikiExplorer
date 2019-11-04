package net.nousefor.wikiexplorer.api;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

class Api {

    private String endpoint;

    Api(String endpoint) {
        this.endpoint = endpoint;
    }

    String get() {
        return get(null);
    }

    String get(String query) {
        String urlString = endpoint;

        if (query != null)
            urlString += query;

        return getString(createConnection(urlString));

    }

    String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    String post(String urlParameters) {
        try {
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            HttpURLConnection c = createConnection(endpoint);
            c.setDoOutput(true);
            c.setRequestMethod("POST");

            DataOutputStream wr = new DataOutputStream(c.getOutputStream());
            wr.write(postData);

            return getString(c);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private HttpURLConnection createConnection(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                    "WikiExplorer/0.1 (Android) wikiexplorer@xp.nousefor.net");

            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getString(HttpURLConnection connection) {

        try {
            InputStream inputStream = connection.getInputStream();
            String line;
            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder result = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
