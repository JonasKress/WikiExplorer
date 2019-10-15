package net.nousefor.wikiexplorer.api;

import net.nousefor.wikiexplorer.model.Summary;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class WikipediaTest {

    @Test
    public void getSummary_withImage() {
        Wikipedia w = new Wikipedia();
        Summary s = w.getSummary("https://en.wikipedia.org", "Berlin");

        assertNotNull(s.text);
        assertNotNull(s.imageUrl);
    }

    @Test
    public void getSummary_withOutImage() {
        Wikipedia w = new Wikipedia();
        Summary s = w.getSummary("https://en.wikipedia.org", "Surname");

        assertNotNull(s.text);
        assertNull(s.imageUrl);
    }
}