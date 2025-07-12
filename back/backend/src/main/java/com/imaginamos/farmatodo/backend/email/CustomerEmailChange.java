package com.imaginamos.farmatodo.backend.email;


import com.google.api.server.spi.response.ConflictException;
import com.imaginamos.farmatodo.backend.customer.Customers;
import com.imaginamos.farmatodo.backend.user.Authenticate;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class CustomerEmailChange extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CustomerEmailChange.class.getName());

    private Authenticate authenticate;
    private Customers customers;
    public CustomerEmailChange() {
        customers = new Customers();
        authenticate = new Authenticate();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        String paramPrueba = req.getParameter("prueba");
        String token = req.getParameter("token");
        String tokenIdWebSafe = req.getParameter("tokenIdWebSafe");
        String idCustomerWebSafe = req.getParameter("idCustomerWebSafe");
        String newEmail = req.getParameter("newEmail");

        try {
            if (token == null || tokenIdWebSafe == null || idCustomerWebSafe == null  ||
                    token.isEmpty() || tokenIdWebSafe.isEmpty() || idCustomerWebSafe.isEmpty())
                throw new ConflictException(Constants.INVALID_TOKEN);
            if (!authenticate.isValidToken(token, tokenIdWebSafe))
                throw new ConflictException(Constants.INVALID_TOKEN);

            if (newEmail == null || newEmail.isEmpty()){
                throw new ConflictException(Constants.MESSAGE_MAIL_NOT_VALID);
            }

//            Answer answer = customers.changeEmailCustomerClick(idCustomerWebSafe,newEmail);
//
//            if (answer.isConfirmation()){
//            }

        }catch (Exception e){
            LOG.severe("Error click change email" + e.getMessage());
        }

        String redirectUrl = "https://www.farmatodo.com.co";
        resp.sendRedirect(redirectUrl);
        super.doGet(req, resp);
    }
}
