package net.nousefor.wikiexplorer.api;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class Wikidata {

    private static final String WIKIDATA_API_URL = "https://www.wikidata.org/w/api.php";

    private static final String API_ACTION_TOKEN = "action=query&format=json&meta=tokens";
    private static final String API_ACTION_SET_LABEL = "action=wbsetlabel&format=json&id={ID}&token={TOKEN}&language={LANG}&value={VALUE}";
    private static final String API_ACTION_GET_LABEL = "action=wbgetentities&format=json&ids={ID}&props=labels&languages={LANG}";
    private static final String API_ACTION_SET_DESCRIPTION = "action=wbsetdescription&format=json&id={ID}&token={TOKEN}&language={LANG}&value={VALUE}";
    private static final String API_ACTION_GET_DESCRIPTION = "action=wbgetentities&format=json&ids={ID}&props=descriptions&languages={LANG}";

    private Api api;

    public Wikidata() {
        api = new Api(WIKIDATA_API_URL);
    }

    String getToken() {
        try {
            JSONObject json = new JSONObject(api.post(API_ACTION_TOKEN));
            return json.getJSONObject("query").getJSONObject("tokens").getString("csrftoken");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean setLabel(String id, String language, String value) {
        try {
            JSONObject json = new JSONObject(api.post(API_ACTION_SET_LABEL
                    .replace("{ID}", id)
                    .replace("{VALUE}", api.encode(value))
                    .replace("{LANG}", language)
                    .replace("{TOKEN}", api.encode(getToken()))
            ));
            return json.getInt("success") == 1;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getLabel(String id, String language) {
        try {
            JSONObject json = new JSONObject(api.post(API_ACTION_GET_LABEL
                    .replace("{ID}", id)
                    .replace("{LANG}", "en")
            ));
            return json.getJSONObject("entities")
                    .getJSONObject(id)
                    .getJSONObject("labels")
                    .getJSONObject(language)
                    .getString("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    boolean setDescription(String id, String language, String value) {
        try {
            JSONObject json = new JSONObject(api.post(API_ACTION_SET_DESCRIPTION
                    .replace("{ID}", id)
                    .replace("{VALUE}", api.encode(value))
                    .replace("{LANG}", language)
                    .replace("{TOKEN}", api.encode(getToken()))
            ));
            return json.getInt("success") == 1;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    String getDescription(String id, String language) {
        try {
            JSONObject json = new JSONObject(api.post(API_ACTION_GET_DESCRIPTION
                    .replace("{ID}", id)
                    .replace("{LANG}", "en")
            ));
            return json.getJSONObject("entities")
                    .getJSONObject(id)
                    .getJSONObject("descriptions")
                    .getJSONObject(language)
                    .getString("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
