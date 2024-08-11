package tests;

import org.testng.annotations.Test;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import junit.framework.Assert;
import pojo.LoginRequest;
import pojo.LoginResponse;
import pojo.OrderDetail;
import pojo.OrderDetails;

import static io.restassured.RestAssured.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ECommAPITest {
	private String token;
	private String userID;
	private String productID;

	@Test(priority = 1)
	public void getToken() {
		RequestSpecification req = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/")
				.setContentType(ContentType.JSON).build();

		LoginRequest loginRequest = new LoginRequest();
		loginRequest.setUserEmail("renukaee023@gmail.com");
		loginRequest.setUserPassword("Renuka@24");

		RequestSpecification reqLogin = given().log().all().spec(req).body(loginRequest);
		LoginResponse loginResponse = reqLogin.post("api/ecom/auth/login").then().assertThat().statusCode(200).log()
				.all().extract().response().as(LoginResponse.class);

		token = loginResponse.getToken();
		userID = loginResponse.getUserId();

	}

	@Test(priority = 1, dependsOnMethods = "getToken")
	public void addProduct() {
		RequestSpecification addProductBaseURI = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/")
				.addHeader("Authorization", token).build();
		RequestSpecification reqAddProduct = given().log().all().spec(addProductBaseURI)
				.param("productName", "Face Wash").param("productAddedBy", userID)
				.param("productCategory", "Saliclic Acid").param("productSubCategory", "Skin Care")
				.param("productPrice", "300").param("productDescription", "SkinCo").param("productFor", "Women")
				.multiPart("productImage", new File("src//test//resources//FaceWash.jpg"));

		String responseAddProduct = reqAddProduct.post("api/ecom/product/add-product").then().log().all().assertThat()
				.statusCode(201).extract().response().asString();
		JsonPath js = new JsonPath(responseAddProduct);

		productID = js.get("productId");
		System.out.println("ProductID : " + productID);

	}
	
	
	@Test(priority = 4,dependsOnMethods = "addProduct")
	public void createOrder() {
		RequestSpecification createOrderBaseURI = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/")
				.addHeader("Authorization",token).
				setContentType(ContentType.JSON).build();
		
		OrderDetail orderDetail=new OrderDetail();
		orderDetail.setCountry("India");
		orderDetail.setProductOrderedId(productID);
		List<OrderDetail> orderDetailList= new ArrayList<OrderDetail>();
		orderDetailList.add(orderDetail);
		OrderDetails orderDetails = new OrderDetails();
		orderDetails.setOrders(orderDetailList);
		System.out.println(orderDetails);
		RequestSpecification reqCreateOrder = given().log().all().spec(createOrderBaseURI).body(orderDetails);
		String responseCreateProduct = reqCreateOrder.post("api/ecom/order/create-order").then().log().all().assertThat()
				.statusCode(201).extract().response().asString();
		System.out.println(responseCreateProduct);
	}
	
	@SuppressWarnings("deprecation")
	@Test(priority = 5,dependsOnMethods = "addProduct")
	public void deleteProduct() {
		RequestSpecification deleteProductBaseURI = new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com/")
				.addHeader("Authorization",token).
				setContentType(ContentType.JSON).build();
		RequestSpecification deleteProductReq=given().log().all().spec(deleteProductBaseURI).
				pathParam("productId",productID);
		String deleteProductResponse  = deleteProductReq.delete("api/ecom/product/delete-product/{productId}").then().assertThat().statusCode(200).log()
				.all().extract().response().asString();
		JsonPath js = new JsonPath(deleteProductResponse);
		Assert.assertEquals("Product Deleted Successfully",js.get("message"));
		
	}

		

}
