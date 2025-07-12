package com.farmatodo.backend.qr;

import com.farmatodo.backend.user.Authenticate;
import com.farmatodo.backend.util.CoreConnection;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.imaginamos.farmatodo.model.monitor.QRCode;
import com.imaginamos.farmatodo.model.util.Constants;
import com.imaginamos.farmatodo.model.util.URLConnections;
import org.json.simple.JSONObject;


import javax.inject.Named;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @Api - Google indicator that the present class ins an Api.
 * Every further indication of @ApiMethod includes this as a Google API.
 */
@Api(name = "qrCodeMonitorEndpoint",
    version = "v1",
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID_DEVELOPER, com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE},
    namespace = @ApiNamespace(ownerDomain = Constants.OWNER_DOMAIN, ownerName = Constants.OWNER_NAME, packagePath = ""),
    description = "Get QR Code for monitor.")
public class QRCodeMonitorEndpoint {
  private static final Logger log = Logger.getLogger(QRCodeMonitorEndpoint.class.getName());
  private Authenticate authenticate;

  public QRCodeMonitorEndpoint() {
    authenticate = new Authenticate();
  }

  /**
   * Get QR Code by order store
   * @param store
   * @return QRCode
   * @throws ConflictException
   * @throws BadRequestException
   * @throws IOException
   * @throws InternalServerErrorException
   * @throws WriterException
   */
  @ApiMethod(name = "getQRCode", path = "/qrCodeMonitorEndpoint/getQRCode", httpMethod = ApiMethod.HttpMethod.GET)
  public QRCode getQRCode(@Named("store") final String store)
          throws ConflictException, BadRequestException, IOException, InternalServerErrorException, WriterException {

    if (store == null || store.isEmpty())
      throw new ConflictException(Constants.ERROR_STORE_ID);

    QRCode qrCode = new QRCode();

    String url = URLConnections.URL_GET_ORDER_QR_CODE + "/" + store;
    JSONObject jsonObject = CoreConnection.getRequest(url, JSONObject.class);

    if (jsonObject == null || jsonObject.get("orderId") == null) {
      log.info("No hay pedidos en cola para la tienda " + store);
      qrCode.setStatus(Constants.CODE_NOT_FOUND_ORDER_QR);
      return qrCode;
    }

    qrCode.setStatus(Constants.CODE_SUCCESS);
    jsonObject.put("storeId", store);
    jsonObject.put("random", UUID.randomUUID().toString());

    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode(jsonObject.toJSONString(),
            BarcodeFormat.QR_CODE, Constants.QR_CODE_WIDTH, Constants.QR_CODE_HEIGHT);

    ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
    byte[] code = Base64.getEncoder().encode((pngOutputStream.toByteArray()));

    qrCode.setCode(new String(code));
    return qrCode;
  }

  @ApiMethod(name = "readQRCode", path = "/qrCodeMonitorEndpoint/readQRCode", httpMethod = ApiMethod.HttpMethod.GET)
  public JSONObject readQRCode(@Named("store") final String store)
          throws ConflictException, BadRequestException, IOException, InternalServerErrorException, WriterException {
    String url = URLConnections.URL_PUSH_NOTIFICATION_MONITOR + store;
    return CoreConnection.getRequest(url, JSONObject.class);
  }
}

