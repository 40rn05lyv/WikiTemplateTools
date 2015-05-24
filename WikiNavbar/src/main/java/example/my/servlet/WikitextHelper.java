package example.my.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikitextHelper {

    static Pattern p1 = Pattern.compile("(\\[\\[.*?\\]\\])");

    public static List<String> getLinksFromWikiText(String wikitext) {
        List<String> result = new ArrayList<String>();
        Matcher m = p1.matcher(wikitext);
        while (m.find()) {
            String wikiLink = m.group();
            int sharpIndex = wikiLink.indexOf('#');
            int pipeIndex = wikiLink.indexOf('|');
            int index = pipeIndex;
            if (sharpIndex != -1 && sharpIndex < pipeIndex) {
                index = sharpIndex;
            }
            if (index != -1) {
                String link = wikiLink.substring(2, index).trim();
                System.out.println("Found link: " + link);
                result.add(link);
            } else {
                String link = wikiLink.substring(2, wikiLink.length() - 2).trim();
                System.out.println("Found clean link: " + link);
                result.add(link);
            }
        }
        return result;
    }
}
