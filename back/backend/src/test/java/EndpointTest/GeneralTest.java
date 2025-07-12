package EndpointTest;

import com.imaginamos.farmatodo.backend.order.create_order.domain.OrderMethods;
import com.imaginamos.farmatodo.backend.util.FTDUtil;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class GeneralTest {

    @Test
    void testExample() throws Exception {

        /*Key<Customer> customerKey = Key.create("ahZzfnN0dW5uaW5nLWJhc2UtMTY0NDAyci4LEgRVc2VyIiQ0MjBjZWQzNS1mNThhLTQzZTgtYWQzNS05NDJmYzIxZjBmM2MM");
        System.out.println(customerKey.getString());*/

        String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

        String dateInString = "2018-05-28T10:00:00";
        LocalDateTime ldt = LocalDateTime.parse(dateInString, DateTimeFormatter.ofPattern(DATE_FORMAT));

        ZoneId singaporeZoneId = ZoneId.of("Asia/Singapore");
        System.out.println("TimeZone : " + singaporeZoneId);

        //LocalDateTime + ZoneId = ZonedDateTime
        ZonedDateTime asiaZonedDateTime = ldt.atZone(singaporeZoneId);
        System.out.println("Date (Singapore) : " + asiaZonedDateTime);

        ZoneId newYokZoneId = ZoneId.of("America/New_York");
        System.out.println("TimeZone : " + newYokZoneId);

        ZonedDateTime nyDateTime = asiaZonedDateTime.withZoneSameInstant(newYokZoneId);
        System.out.println("Date (New York) : " + nyDateTime);

        DateTimeFormatter format = DateTimeFormatter.ofPattern(DATE_FORMAT);
        System.out.println("\n---DateTimeFormatter---");
        System.out.println("Date (Singapore) : " + format.format(asiaZonedDateTime));
        System.out.println("Date (New York) : " + format.format(nyDateTime));





        //FRONT
        String dateFromJsonFront = "2018-05-28T03:00:00";

        //BACKEND
        Date date2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateFromJsonFront);
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        //SOLUTION
        /*Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.HOUR, 5);
        date = c.getTime();*/
        String newDate = format2.format(date2) + "-0500";

        //CORE
        format2.setTimeZone(TimeZone.getTimeZone("GMT-10"));
        System.out.println(newDate);
    }


    @Test
    void testStrings() throws Exception {
    String cutWorldRes =
        FTDUtil.replaceStringVar(
            "<font color='blue'>Hola {user_name} </font> Estas son las mejores ofertas", "user_name", "Pepito");
        System.out.println(cutWorldRes);
    }

    /*@Test
    void testBanners() throws IOException {
        //BannersDTFRes response  = ApiBackend30Service.get().getBannersCMS(null,null,null);

        BannersDTFRes response  = CMSService.get().getBannersCMS("rdz.cristhian@gmail.com",null,null ,null, false );

        Assert.isTrue(response != null);
        System.out.println(response.getData());
    }*7

    @Test
    void testAlgolia(){
        HomeConfigAlgolia homeConfigAlgolia = APIAlgolia.getHomeV2Config();
        Assert.notNull(homeConfigAlgolia);
        System.out.println("prueba test");
    }

    @Test
    void testDates(){
        String fecha1 = "20200430";


        if (fecha1.contains("/")) {
            fecha1 = fecha1.replace("/","");
        }
        String fechaFormateada = fecha1;
        System.out.println("Fecha -> " + fechaFormateada);
    }

    /*@Test
    void testSendEmailBraze() {
        SendMailReq sendMailReq = new SendMailReq();
        sendMailReq.setTo("cristhian.rodriguez@farmatodo.com");
        sendMailReq.setSubject("email test via backend3");
        sendMailReq.setText("<br> email enviado via backend3 !!</br>");
        ApiGatewayService.get().sendEmailBraze(sendMailReq);
    }*/

    @Test
    void testCalendar(){
        OrderMethods orderMethods = new OrderMethods();
        Date fecha = orderMethods.getNextDateToSchedule(18);

        System.out.println(fecha.toString());
    }



}
