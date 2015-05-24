package example.my.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyServlet extends HttpServlet {

    private static final long serialVersionUID = -9122125709896866661L;

    Connection con;

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.out);
        }
        try {
            con = DriverManager.getConnection("jdbc:hsqldb:mydatabase", "SA", "");
            con.createStatement().executeUpdate("create table contacts (name varchar(45),email varchar(45),phone varchar(45))");
        } catch (SQLException e) {
            e.printStackTrace(System.out);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String continueParam = req.getParameter("continue");
        MyServletProcessor processor = new MyServletProcessor();
        processor.process(5, continueParam);
        req.setAttribute("continueParam", processor.getContinue());
        req.setAttribute("list", processor.getList());
        req.getRequestDispatcher("/navbar.jsp").forward(req, resp);
    }


}
