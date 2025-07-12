package com.farmatodo.backend.util;

import com.google.api.server.spi.response.BadRequestException;
import com.imaginamos.farmatodo.model.util.Constants;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

public class CoreExceptions extends DefaultResponseErrorHandler {
  @Override
  public void handleError(ClientHttpResponse response) throws IOException {
    try {
      throw new BadRequestException(Constants.DEFAULT_MESSAGE);
    } catch (BadRequestException e) {
      e.printStackTrace();
    }
  }
}
