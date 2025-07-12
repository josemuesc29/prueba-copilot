package com.imaginamos.farmatodo.backend.start;

/**
 * Created by Admin on 20/06/2017.
 */

import com.google.api.server.spi.config.*;
import com.imaginamos.farmatodo.model.util.Answer;
import com.imaginamos.farmatodo.model.util.Constants;
import java.util.logging.Logger;


/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "startEndpoint",
        version = "v1",
        apiKeyRequired = AnnotationBoolean.TRUE,
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
        description = "Stores configuration for apps for all pages.")

public class StartEndpoint {

  private static final Logger LOGGER = Logger.getLogger(StartEndpoint.class.getName());

  public StartEndpoint() {
  }
    @ApiMethod(name = "warmUp", path = "/start", httpMethod = ApiMethod.HttpMethod.GET)
    public Answer warmup() {
        Answer answer = new Answer();
        return answer;
    }
}
