package com.imaginamos.farmatodo.networking.util;


import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.networking.cache.SecretsCache;

import javax.servlet.http.HttpServlet;
import java.util.logging.Logger;

public class AppStartupListener extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AppStartupListener.class.getName());


    @Override
        public void init() {
            LOG.info("AppStartupListener.init() -> Ok");
            SecretsCache.getInstance(Constants.GCP_PROJECT_ID);
        }
}