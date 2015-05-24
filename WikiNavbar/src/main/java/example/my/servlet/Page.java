package example.my.servlet;

import java.util.ArrayList;
import java.util.List;

public class Page {
    private int pageId;
    private String pageTitle;
    private List<String> langs;
    private List<String> links;
    private List<String> trueLinks;

    private String targetLang;
    private List<String> existingLinksInTarget;
    private List<String> unexistingLinksInTarget;

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public void addLang(String lang) {
        if (langs == null) {
            langs = new ArrayList<String>();
        }
        langs.add(lang);
    }

    public List<String> getLangs() {
        return langs;
    }

    public void addLink(String link) {
        if (links == null) {
            links = new ArrayList<String>();
        }
        links.add(link);
    }

    public List<String> getLinks() {
        return links;
    }

    public List<String> getTrueLinks() {
        return trueLinks;
    }

    public void setTrueLinks(List<String> trueLinks) {
        this.trueLinks = trueLinks;
    }

}
