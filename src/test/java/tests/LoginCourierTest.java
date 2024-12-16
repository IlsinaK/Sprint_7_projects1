package tests;

import api.CourierApi;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.CourierDataLombok;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.net.HttpURLConnection.*;
import static org.hamcrest.CoreMatchers.is;

public class LoginCourierTest {

    private CourierApi courierApi;
    private CourierDataLombok courierDataLombok;
    private int courierId; // Хранение ID курьера для последующего удаления

    @Before
    public void setUp() {
        courierApi = new CourierApi();

        String loginParam = "Vlad" + RandomStringUtils.randomAlphabetic(4);
        courierDataLombok = new CourierDataLombok(loginParam, "passwordVlad1234534", "Vlad");

        ValidatableResponse response = courierApi.createCourierLombok(courierDataLombok);

        response.log().all()
                .assertThat()
                .statusCode(HTTP_CREATED)
                .body("ok", is(true));

    }




    @After
    public void cleanUp() {
        if (courierId != 0) {
            courierApi.deleteCourier(courierId); // Удаляем курьера после теста
        }
    }

    @DisplayName("Check courier can login")
    @Test
    public void courierCanLoginTest() {
        ValidatableResponse response = courierApi.loginCourier(new CourierDataLombok(courierDataLombok.getLogin(), courierDataLombok.getPassword()));

        response.log().all()
                .assertThat()
                .statusCode(HTTP_OK)
                .body("id", is(courierId)); // Используем сохраненный ID
    }

    @DisplayName("Check that login fails with incorrect credentials")
    @Test
    public void loginFailsWithIncorrectCredentialsTest() {
        CourierDataLombok incorrectCourier = new CourierDataLombok("NonExistentUser", "wrongPassword");
        ValidatableResponse response = courierApi.loginCourier(incorrectCourier);

        response.log().all()
                .assertThat()
                .statusCode(HTTP_NOT_FOUND)
                .body("message", is("Недостаточно данных для входа"));
    }

    @DisplayName("Check that login fails with missing fields")
    @Test
    public void loginFailsWithMissingFieldsTest() {
        // Отсутствует логин
        ValidatableResponse responseMissingLogin = courierApi.loginCourier(new CourierDataLombok(null, "password"));
        responseMissingLogin.log().all()
                .assertThat()
                .statusCode(HTTP_GATEWAY_TIMEOUT)
                .body("message", is("Недостаточно данных для входа"));

        // Отсутствует пароль
        ValidatableResponse responseMissingPassword = courierApi.loginCourier(new CourierDataLombok("TestUser", null));
        responseMissingPassword.log().all()
                .assertThat()
                .statusCode(HTTP_BAD_REQUEST)
                .body("message", is("Недостаточно данных для входа"));
    }

    @DisplayName("Check that login fails for non-existent user")
    @Test
    public void loginFailsForNonExistentUserTest() {
        CourierDataLombok nonExistentCourier = new CourierDataLombok("NonExistentUser", "password");
        ValidatableResponse response = courierApi.loginCourier(nonExistentCourier);

        response.log().all()
                .assertThat()
                .statusCode(HTTP_UNAUTHORIZED)
                .body("message", is("Недостаточно данных для входа"));
    }
}

