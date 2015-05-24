package example.my.servlet;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class GeneralHelper {
    
    public static Pages filterByLangLink(Pages pages, String lang) {
        Pages result = new Pages();
        for (Page page : pages.getList()) {
            if (!page.getLangs().contains(lang)) {
                result.add(page);
            }
        }
        return result;
    }

    public static Pages generateStats(Pages pages) {
        for (Page page: pages.getList()) {
            //page.get
        }
        return null;
    }
    
    public static String replaceWikiLinks(String text, String linkSource, String linkTarget) {
        return StringUtils.replace(text, linkSource, linkTarget);
    }
    
    public static String replaceWikiLink(String text, String linkSource, String linkTarget) {
        return StringUtils.replace(text, "[[" + linkSource + "]]", "[[" + linkTarget + "]]");
    }

    public static String replaceWikiLinks(String string, Map<String, String> map) {
        return null;
    }

}
