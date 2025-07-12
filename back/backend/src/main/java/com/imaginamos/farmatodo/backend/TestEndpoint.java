package com.imaginamos.farmatodo.backend;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;


public class TestEndpoint extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TestEndpoint.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String paramPrueba = req.getParameter("prueba");
        LOGGER.info("parameter prueba -> "  + paramPrueba);

        String redirectUrl = "https://www.farmatodo.com.co";
        resp.sendRedirect(redirectUrl);
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req,resp);
    }
}
