package org.wikipedia.templates.find.interwiki.db;

import java.util.List;

public class FindTemplateInterwikiResult {

    public static enum FindTemplateInterwikiResultEnum {
        TEMPLATE_DONT_EXIST, NO_TRANSCLUSIONS, SUCCESS, HAS_INTERWIKI
    }

    private FindTemplateInterwikiResultEnum result;
    private List<InterwikiCandidate> candidates;

    public FindTemplateInterwikiResultEnum getResult() {
        return result;
    }

    public void setResult(FindTemplateInterwikiResultEnum result) {
        this.result = result;
    }

    public List<InterwikiCandidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<InterwikiCandidate> candidates) {
        this.candidates = candidates;
    }

    public boolean isSuccess() {
        return result == FindTemplateInterwikiResultEnum.SUCCESS;
    }

    public boolean isTemplateDontExist() {
        return result == FindTemplateInterwikiResultEnum.TEMPLATE_DONT_EXIST;
    }

    public boolean hasNoTransclusions() {
        return result == FindTemplateInterwikiResultEnum.NO_TRANSCLUSIONS;
    }

    public boolean hasInterwiki() {
        return result == FindTemplateInterwikiResultEnum.HAS_INTERWIKI;
    }

}
