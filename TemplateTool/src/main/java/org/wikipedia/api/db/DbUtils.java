package org.wikipedia.api.db;

public class DbUtils {

    public static String toDBView(String templateName) {
        return templateName.replaceAll(" ", "_");
    }
    
}
