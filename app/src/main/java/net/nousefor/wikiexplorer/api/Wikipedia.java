package net.nousefor.wikiexplorer.api;

import net.nousefor.wikiexplorer.model.Summary;

import org.json.JSONObject;

public class Wikipedia {

    private static final String WIKIPEDIA_SUMARRY_REST_URL = "{URL}/api/rest_v1/page/summary/{TITLE}";


    private Api api;

    public Wikipedia() {

    }

    public Summary getSummary(String url, String title) {
        Summary summary = new Summary();
        try {
            api = new Api(WIKIPEDIA_SUMARRY_REST_URL
                    .replace("{URL}", url)
                    .replace("{TITLE}", title)
            );

            JSONObject json = new JSONObject(api.get());
            summary.text = json.getString("extract");
            summary.textHtml = json.getString("extract_html");

            if (json.has("originalimage"))
                summary.imageUrl = api.decode(json.getJSONObject("originalimage").getString("source"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return summary;
    }

}
