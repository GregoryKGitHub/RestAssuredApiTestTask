package org.example;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.*;

public class RestApiTest {
    static String token = "token=";
    static String bookingIdRef = "https://restful-booker.herokuapp.com/booking/";
    static int bookingId;
    static Response responseCreateBooking;

    @BeforeClass
    public static void authBooking()   {
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", "admin");
        requestBody.put("password", "password123");

        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");

        request.body(requestBody.toString());
        Response response = request.post("https://restful-booker.herokuapp.com/auth");
        token += response.path("token");

        JSONObject requestBodyCreateBooking = new JSONObject();
        JSONObject bookingDates = new JSONObject();
        bookingDates.put("checkin", "2022-10-10");
        bookingDates.put("checkout", "2022-10-11");
        requestBodyCreateBooking.put("firstname", "John");
        requestBodyCreateBooking.put("lastname", "Dou");
        requestBodyCreateBooking.put("totalprice", 1000);
        requestBodyCreateBooking.put("depositpaid", true);
        requestBodyCreateBooking.put("bookingdates", bookingDates);
        requestBodyCreateBooking.put("additionalneeds", "breakfast");

        RequestSpecification requestCreateBooking = RestAssured.given();
        requestCreateBooking.header("Content-Type", "application/json");
        requestCreateBooking.header("Accept", "application/json");

        requestCreateBooking.body(requestBodyCreateBooking.toString());
        responseCreateBooking = requestCreateBooking.post("https://restful-booker.herokuapp.com/booking");
        bookingId = responseCreateBooking.path("bookingid");
        bookingIdRef += bookingId;
    }

    @Test
    public void createBookingTest() {
        int totalPrice = responseCreateBooking.path("booking.totalprice");

        Assert.assertEquals("John", responseCreateBooking.path("booking.firstname"));
        Assert.assertEquals("Dou", responseCreateBooking.path("booking.lastname"));
        Assert.assertEquals(1000, totalPrice);
        Assert.assertEquals(true, responseCreateBooking.path("booking.depositpaid"));
        Assert.assertEquals("2022-10-10", responseCreateBooking.path("booking.bookingdates.checkin"));
        Assert.assertEquals("2022-10-11", responseCreateBooking.path("booking.bookingdates.checkout"));
        Assert.assertEquals("breakfast", responseCreateBooking.path("booking.additionalneeds"));
    }

    @Test
    public void updateBookingTest() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("firstname", "Jack");

        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.header("Accept", "application/json");
        request.header("Cookie", token);

        request.body(requestBody.toString());
        Response response = request.urlEncodingEnabled(false).patch(bookingIdRef);

        Assert.assertEquals("Jack", response.path("firstname"));
    }

    @Test
    public void bookingIdExistTest()    {
        get("https://restful-booker.herokuapp.com/booking").then()
                .assertThat().body("bookingid", hasItem(bookingId));
    }

    @AfterClass
    public static void deleteBooking() {
        RequestSpecification request = RestAssured.given();
        request.header("Content-Type", "application/json");
        request.header("Cookie", token);

        Response response = request.urlEncodingEnabled(false).delete(bookingIdRef);
    }
}
