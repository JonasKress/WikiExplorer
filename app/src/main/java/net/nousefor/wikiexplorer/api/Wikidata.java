package net.nousefor.wikiexplorer.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Wikidata {

    private static final String WIKIDATA_API_URL = "https://www.wikidata.org/w/api.php";

    private static final String API_ACTION_TOKEN = "action=query&format=json&meta=tokens";

    private static final String API_ACTION_GET_LABEL = "action=wbgetentities&format=json&ids={ID}&props=labels&languages={LANG}";
    private static final String API_ACTION_SET_LABEL = "action=wbsetlabel&format=json&id={ID}&token={TOKEN}&language={LANG}&value={VALUE}";

    private static final String API_ACTION_GET_DESCRIPTION = "action=wbgetentities&format=json&ids={ID}&props=descriptions&languages={LANG}";
    private static final String API_ACTION_SET_DESCRIPTION = "action=wbsetdescription&format=json&id={ID}&token={TOKEN}&language={LANG}&value={VALUE}";

    private static final String API_ACTION_GET_PROPERTY = "action=wbgetclaims&format=json&entity={ID}&property={PROPERTYID}";
    private static final String API_ACTION_SET_PROPERTY = "action=wbcreateclaim&format=json&entity={ID}&snaktype=value&property={PROPERTYID}&value={VALUE}&token={TOKEN}";

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
            return null;
        }
    }

    public boolean setLabel(String id, String language, String value) throws Exception {
        try {
            JSONObject json = new JSONObject(api.post(API_ACTION_SET_LABEL
                    .replace("{ID}", id)
                    .replace("{VALUE}", api.encode(value))
                    .replace("{LANG}", language)
                    .replace("{TOKEN}", api.encode(getToken()))
            ));

            if (json.has("error"))
                throw new Exception(json.getJSONObject("error").getString("info"));

            return json.getInt("success") == 1;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean setDescription(String id, String language, String value) throws Exception {
        try {
            JSONObject json = new JSONObject(api.post(API_ACTION_SET_DESCRIPTION
                    .replace("{ID}", id)
                    .replace("{VALUE}", api.encode(value))
                    .replace("{LANG}", language)
                    .replace("{TOKEN}", api.encode(getToken()))
            ));

            if (json.has("error"))
                throw new Exception(json.getJSONObject("error").getString("info"));

            return json.getInt("success") == 1;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getDescription(String id, String language) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getProperty(String id, String PropertyId) {
        try {
            JSONObject json = new JSONObject(api.post(API_ACTION_GET_PROPERTY
                    .replace("{ID}", id)
                    .replace("{PROPERTYID}", PropertyId)
            ));

            JSONArray array = json.getJSONObject("claims")
                    .getJSONArray(PropertyId);

            return array
                    .getJSONObject(array.length() - 1)
                    .getJSONObject("mainsnak")
                    .getJSONObject("datavalue")
                    .getString("value");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean setProperty(String id, String PropertyId, String value) throws Exception {
        if (value.matches("[A-Z][0-9]+"))
            value = "{\"id\":\"" + value + "\"}";
        else
            value = "\"" + value + "\"";

        try {
            JSONObject json = new JSONObject(api.post(API_ACTION_SET_PROPERTY
                    .replace("{ID}", id)
                    .replace("{PROPERTYID}", PropertyId)
                    .replace("{VALUE}", api.encode(value))
                    .replace("{TOKEN}", api.encode(getToken()))
            ));

            if (json.has("error"))
                throw new Exception(json.getJSONObject("error").getString("info"));

            return json.getInt("success") == 1;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

}
