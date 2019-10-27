package net.nousefor.wikiexplorer.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WikidataTest {
    private final static String ITEM_ID = "Q4115189";//sandbox item
    private final static String LANGUAGE = "en";
    private final static String LABEL = "[LABEL]";
    private final static String DESCRIPTION = "[DESCRIPTION]";
    private final static String IMAGE_NAME = "Marienkirche B-Mitte 03-2014.jpg";
    private final static String PROPERTY_ID = "P31";

    @Test
    public void getToken() {
        Wikidata w = new Wikidata();
        String result = w.getToken();

        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void setLabel() throws Exception {
        Wikidata w = new Wikidata();
        boolean result = w.setLabel(ITEM_ID, LANGUAGE, LABEL);
        assertTrue(result);
    }

    @Test
    public void getLabel() throws Exception {
        Wikidata w = new Wikidata();
        w.setLabel(ITEM_ID, LANGUAGE, LABEL);
        String result = w.getLabel(ITEM_ID, LANGUAGE);
        assertEquals(LABEL, result);
    }


    @Test
    public void setDescription() throws Exception {
        Wikidata w = new Wikidata();
        boolean result = w.setDescription(ITEM_ID, LANGUAGE, DESCRIPTION);
        assertTrue(result);
    }

    @Test
    public void getDescription() throws Exception {
        Wikidata w = new Wikidata();
        w.setDescription(ITEM_ID, LANGUAGE, DESCRIPTION);

        String result = w.getDescription(ITEM_ID, LANGUAGE);
        assertEquals(DESCRIPTION, result);
    }

    @Test
    public void getProperty() throws Exception {
        Wikidata w = new Wikidata();
        w.setProperty(ITEM_ID, PROPERTY_ID, ITEM_ID);
        String result = w.getProperty(ITEM_ID, PROPERTY_ID);

        assertTrue(result.contains(ITEM_ID));
    }

    @Test
    public void setProperty() throws Exception {
        Wikidata w = new Wikidata();
        boolean result = w.setProperty(ITEM_ID, PROPERTY_ID, ITEM_ID);

        assertTrue(result);
    }
}