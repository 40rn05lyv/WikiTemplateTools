package example.find.template.interwiki;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import example.api.QueryHelper;

public class ListTemplatesWithoutInterwiki extends HttpServlet {

    private static final long serialVersionUID = -8441332046266659846L;
    
    @Override
    public void init() throws ServletException {
        super.init();
        QueryHelper.go();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String parentTemplate = req.getParameter("parentTemplate");
        List<String> templates = QueryHelper.getLinksHereTemplatesWithLangLinks(parentTemplate);
        req.setAttribute("searchLang", "uk");
        req.setAttribute("templates", templates);
        req.getRequestDispatcher("/listwithoutinterwiki.jsp").forward(req, resp);
    }
    
    @Override
    public void destroy() {
        super.destroy();
        QueryHelper.stop();
    }

}
