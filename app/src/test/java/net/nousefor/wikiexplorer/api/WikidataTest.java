package net.nousefor.wikiexplorer.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WikidataTest {
    final static String ITEM_ID = "Q4115189";//sandbox item
    final static String LANGUAGE = "en";
    final static String LABEL = "[LABEL]";
    final static String DESCRIPTION = "[DESCRIPTION]";


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
}