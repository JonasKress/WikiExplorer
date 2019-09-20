package net.nosuefor.wikiexplorer.query;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Api {

    String endpoint = null;

    public Api(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getResult() {
        return getResult(null);
    }

    public String getResult(String query) {
        String urlString = null;

        try {
            urlString = endpoint;
            if (query != null)
                urlString += URLEncoder.encode(query);

            StringBuilder result = new StringBuilder();
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
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
