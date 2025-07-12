package com.imaginamos.farmatodo.backend.util;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

public class TraceUtil {
    private static final Logger log = Logger.getLogger(TraceUtil.class.getName());

    public static String getXCloudTraceId(HttpServletRequest request){
        //log.info("method getXCloudTraceId x-cloud-trace-context ");
        return request.getHeader("x-cloud-trace-context").split("/")[0];
    }
}
