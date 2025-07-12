package com.imaginamos.farmatodo.backend;


import com.googlecode.objectify.ObjectifyService;

import javax.servlet.*;
import java.util.logging.Logger;

public class CustomBackendFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(CustomBackendFilter.class.getName());
    private FilterConfig filterConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException{
//        LOG.info("CustomBackendFilter.doFilter(...) -> Ok");
        try{
            ObjectifyService.begin();
            filterChain.doFilter(request, response);
        }catch (Exception e){
            final String message = "Error al inicializar ObjectifyService... Mensaje: Revise com.imaginamos.farmatodo.backend.CustomBackendFilter";
            LOG.severe(message);
            throw new ServletException(message);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    public void destroy() {}
}
