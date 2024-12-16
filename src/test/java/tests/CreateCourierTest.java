package tests;

import api.CourierApi;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.CourierDataLombok;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.net.HttpURLConnection.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class CreateCourierTest {

    protected CourierApi courierApi;
    protected CourierDataLombok courierDataLombok;
    private int courierId = 0;

    @Before
    public void setUp() {
        courierApi = new CourierApi();
    }

    @After
    public void cleanUp() {
        if (courierId != 0) {
            courierApi.deleteCourier(courierId);
        }
    }

    @DisplayName("Check courier can be created")
    @Test
    public void courierCanBeCreatedTest() {
        courierDataLombok = new CourierDataLombok("TestUserrEhLc", "passwordsqmrg", "VladQVOto");

        ValidatableResponse response = courierApi.createCourierLombok(courierDataLombok);

        // Сохранить id курьера для дальнейшего использования
        courierId = response.extract().path("id");

        response.log().all()
                .assertThat()
                .statusCode(HTTP_CREATED)
                .body("ok", is(true));
    }

    @DisplayName("Check that duplicate courier cannot be created")
    @Test
    public void duplicateCourierCannotBeCreatedTest() {
        String login = "DuplicateUser";
        courierDataLombok = new CourierDataLombok(login, "password123", "Vlad");

        // Создаем первого курьера
        courierApi.createCourierLombok(courierDataLombok);

        //  создаем дублирующего курьера
        ValidatableResponse response = courierApi.createCourierLombok(courierDataLombok);

        response.log().all()
                .assertThat()
                .statusCode(HTTP_CONFLICT)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @DisplayName("Check that courier cannot be created without required fields")
    @Test
    public void courierCannotBeCreatedWithoutRequiredFieldsTest() {
        courierDataLombok = new CourierDataLombok(null, "password", "TestName");

        ValidatableResponse response = courierApi.createCourierLombok(courierDataLombok);

        response.log().all()
                .assertThat()
                .statusCode(HTTP_BAD_REQUEST)
                .body("message", is("Недостаточно данных для создания учетной записи"));
    }

    @DisplayName("Check that courier cannot be created without password")
    @Test
    public void courierCannotBeCreatedWithoutPasswordTest() {
        courierDataLombok = new CourierDataLombok("TestUser", null, "TestName");

        ValidatableResponse response = courierApi.createCourierLombok(courierDataLombok);

        response.log().all()
                .assertThat()
                .statusCode(HTTP_BAD_REQUEST)
                .body("message", is("Недостаточно данных для создания учетной записи"));
    }
}
