package com.imaginamos.farmatodo.backend.coupon

import com.google.api.client.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.googlecode.objectify.Key
import com.googlecode.objectify.Ref
import com.imaginamos.farmatodo.backend.OfyService
import com.imaginamos.farmatodo.model.coupon.Coupon
import com.imaginamos.farmatodo.model.coupon.CustomerCoupon
import com.imaginamos.farmatodo.model.coupon.CustomerCouponStatusEnum
import com.imaginamos.farmatodo.model.order.DeliveryOrder
import com.imaginamos.farmatodo.model.order.DeliveryOrderItem
import com.imaginamos.farmatodo.model.user.User
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.logging.Logger
import javax.imageio.ImageIO

class CouponService {
    private val logger = Logger.getLogger(CouponService::class.java.name)

    private val QR_SIZE = 512

    fun setStatusCouponsForCustomer(userKey: Key<User>, couponList: MutableList<Coupon>): MutableList<Coupon> {

        // shoppingCart
        val deliveryOrder: DeliveryOrder? = OfyService.ofy().load().type(DeliveryOrder::class.java)
                .filter("currentStatus", 1)
                .ancestor(Ref.create(userKey))
                .first()
                .now()

        couponList.forEach { coupon: Coupon ->
            val queryCustomer = OfyService.ofy().load().type(CustomerCoupon::class.java)
                    .ancestor(coupon)
                    .filter("customerKey", userKey)

            coupon.status = CustomerCouponStatusEnum.AVAILABLE

            if (queryCustomer.count() > 0){
                coupon.status = CustomerCouponStatusEnum.USED
            }

            if (deliveryOrder != null){
                val deliveryOrderItemCoupon: DeliveryOrderItem? = OfyService.ofy().load().type(DeliveryOrderItem::class.java)
                        .ancestor(deliveryOrder).list().firstOrNull { deliveryOrderItem -> deliveryOrderItem.coupon == true }

                if (coupon.itemId.name == deliveryOrderItemCoupon?.idItem?.name){
                    coupon.status = CustomerCouponStatusEnum.IN_SHOPPING_CART
                }

            }

            val qrCodeGenerated = generateQRCode(coupon.name)

            if (qrCodeGenerated.isNotEmpty()){
                logger.info("Setenando el cupon: >> $qrCodeGenerated")
                coupon.qrCode = qrCodeGenerated
            }

//            val qrCodeGenerated = generateQRCode(coupon.name)
//
//            if (qrCodeGenerated.isNotEmpty()){
//            coupon.qrCode = "."
//            }

        }

        return couponList
    }

    private fun generateQRCode(text: String): String {

        try {
            logger.info("Generate QRCODE for string >> $text")

            val encodeHints = HashMap<EncodeHintType, Any>()
            encodeHints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
            encodeHints[EncodeHintType.MARGIN] = 0

            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix: BitMatrix = multiFormatWriter.encode(text,BarcodeFormat.QR_CODE,QR_SIZE,QR_SIZE, encodeHints)

            val bufferedImage: BufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix)

            return imageToBase64(bufferedImage)
        }catch (e: Exception){
            e.printStackTrace()
        }

        return ""
    }

    private fun imageToBase64(bufferedImage: BufferedImage): String {
        val out = ByteArrayOutputStream()
        ImageIO.write(bufferedImage,"png", out)
        return Base64.encodeBase64String(out.toByteArray())
    }
}
