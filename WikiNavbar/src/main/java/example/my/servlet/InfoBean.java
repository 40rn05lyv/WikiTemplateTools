package example.my.servlet;

public class InfoBean {

    int pageId;
    String article;
    int existing;
    int nonExisting;
    double percentage;
    String context;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public InfoBean(int pageId, String article, int existing, int nonExisting, double percentage, String context) {
        super();
        this.pageId = pageId;
        this.article = article;
        this.existing = existing;
        this.nonExisting = nonExisting;
        this.percentage = percentage;
        this.context = context;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public String getArticle() {
        return article;
    }

    public void setArticle(String article) {
        this.article = article;
    }

    public int getExisting() {
        return existing;
    }

    public void setExisting(int existing) {
        this.existing = existing;
    }

    public int getNonExisting() {
        return nonExisting;
    }

    public void setNonExisting(int nonExisting) {
        this.nonExisting = nonExisting;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

}
