package com.imaginamos.farmatodo.backend.util;


import com.imaginamos.farmatodo.model.customer.Customer;
import com.imaginamos.farmatodo.model.util.URLConnections;
import org.json.simple.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Logger;

@SuppressWarnings("ALL")
public class SupportMethods {
  private static final Logger log = Logger.getLogger(Customer.class.getName());

/*  public int sendEmail(String recipient, String subject, String contentType, String content) throws IOException {
    HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.POST, URLConnections.URL_SEND_EMAIL);
    JSONObject email = new JSONObject();
    email.put("content_type", contentType);
    email.put("content", content);
    email.put("recipient", recipient);
    email.put("subject", subject);
    String write = email.toJSONString();
    log.warning(write);
    DataOutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());
    outputStream.writeBytes(email.toJSONString());
    outputStream.flush();
    outputStream.close();
    int responseCode = httpURLConnection.getResponseCode();
    return responseCode;
  }*/

/*
  public int sendSms(String phone, String message) throws IOException {
    JSONObject sms = new JSONObject();
    sms.put("phone", phone);
    sms.put("message", message);
    log.warning(sms.toString());
    HttpURLConnection httpURLConnection = URLConnections.getConnection(URLConnections.POST, URLConnections.URL_SEND_SMS);


    DataOutputStream outputStream = new DataOutputStream(httpURLConnection.getOutputStream());
    outputStream.writeBytes(sms.toJSONString());
    outputStream.flush();
    outputStream.close();
    int codeSMS = httpURLConnection.getResponseCode();
    return codeSMS;
  }
*/

}
