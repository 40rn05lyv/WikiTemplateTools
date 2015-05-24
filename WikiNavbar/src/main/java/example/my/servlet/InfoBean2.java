package example.my.servlet;

import java.util.List;
import java.util.Map;

public class InfoBean2 {

    String article;
    Map<String, String> existing;
    List<String> nonExisting;
    double percentage;
    String context;

    public InfoBean2(String article, Map<String, String> existing, List<String> nonExisting, double percentage, String context) {
        super();
        this.article = article;
        this.existing = existing;
        this.nonExisting = nonExisting;
        this.percentage = percentage;
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Map<String, String> getExisting() {
        return existing;
    }

    public void setExisting(Map<String, String> existing) {
        this.existing = existing;
    }

    public List<String> getNonExisting() {
        return nonExisting;
    }

    public void setNonExisting(List<String> nonExisting) {
        this.nonExisting = nonExisting;
    }

}
