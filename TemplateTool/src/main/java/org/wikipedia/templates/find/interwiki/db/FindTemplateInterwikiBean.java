package org.wikipedia.templates.find.interwiki.db;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.wikipedia.api.Constants;

public class FindTemplateInterwikiBean {

    private String templateName;
    private String templateLang;
    private Set<String> searchLangs;
    private boolean freeze;
    private boolean templateExists;
    private static final String[] supportedLangs = Constants.SUPPORTED_LANGS;
    private TemplateInterwikiFinderResult result;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateLang() {
        return templateLang;
    }

    public void setTemplateLang(String templateLang) {
        this.templateLang = templateLang;
    }

    public boolean isFreeze() {
        return freeze;
    }

    public void setFreeze(boolean freeze) {
        this.freeze = freeze;
    }

    public boolean isTemplateExists() {
        return templateExists;
    }

    public void setTemplateExists(boolean templateExists) {
        this.templateExists = templateExists;
    }

    public Set<String> getSearchLangs() {
        return searchLangs;
    }

    public String getSearchLangsForDisplay() {
        return StringUtils.join(searchLangs, ", ");
    }

    public void setSearchLangs(Set<String> searchLangs) {
        this.searchLangs = searchLangs;
    }

    public TemplateInterwikiFinderResult getResult() {
        return result;
    }

    public void setResult(TemplateInterwikiFinderResult result) {
        this.result = result;
    }

    public String[] getSupportedLangs() {
        return supportedLangs;
    }

}
