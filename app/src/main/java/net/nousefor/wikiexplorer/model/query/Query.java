package net.nousefor.wikiexplorer.model.query;

import net.nousefor.wikiexplorer.model.action.Action;

public class Query implements Cloneable {
    public String name = null;
    public String sparql = null;
    public Action[] actions = null;

    public Query clone() {
        try {
            return (Query) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return new Query();
    }
}
