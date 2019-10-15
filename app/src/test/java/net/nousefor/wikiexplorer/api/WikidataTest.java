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


    @Test
    public void getToken() {
        Wikidata w = new Wikidata();
        String result = w.getToken();

        assertNotNull(result);
        assertTrue(result.length() > 5);
    }

    @Test
    public void setLabel() {
        Wikidata w = new Wikidata();
        boolean result = w.setLabel(ITEM_ID, LANGUAGE, LABEL);
        assertTrue(result);
    }

    @Test
    public void getLabel() {
        Wikidata w = new Wikidata();
        w.setLabel(ITEM_ID, LANGUAGE, LABEL);
        String result = w.getLabel(ITEM_ID, LANGUAGE);
        assertEquals(LABEL, result);
    }


    @Test
    public void setDescription() {
        Wikidata w = new Wikidata();
        boolean result = w.setDescription(ITEM_ID, LANGUAGE, DESCRIPTION);
        assertTrue(result);
    }

    @Test
    public void getDescription() {
        Wikidata w = new Wikidata();
        w.setDescription(ITEM_ID, LANGUAGE, DESCRIPTION);
        String result = w.getDescription(ITEM_ID, LANGUAGE);
        assertEquals(DESCRIPTION, result);
    }

    @Test
    public void getImage() {
        Wikidata w = new Wikidata();
        w.setImage(ITEM_ID, IMAGE_NAME);
        String result = w.getImage(ITEM_ID);
        assertEquals(IMAGE_NAME, result);
    }

    @Test
    public void setImage() {
        Wikidata w = new Wikidata();
        boolean result = w.setImage(ITEM_ID, IMAGE_NAME);
        assertTrue(result);
    }
}