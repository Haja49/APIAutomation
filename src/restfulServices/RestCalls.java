package restfulServices;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpClientConnectionOperator;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.restassured.RestAssured;
import io.restassured.internal.util.IOUtils;
import io.restassured.path.json.JsonPath;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;

public class RestCalls {

	public void get() {
		Response response = RestAssured.get(
				"https://samples.openweathermap.org/data/2.5/weather?q=London,uk&appid=b6907d289e10d714a6e88b30761fae22");
		int statusCode = response.getStatusCode();
		Assert.assertEquals(200, statusCode);
		System.out.println(response.getTimeIn(TimeUnit.MILLISECONDS));
		System.out.println(response.asString());
	}

	public void getUsingBDD() {
		given().get(
				"https://samples.openweathermap.org/data/2.5/weather?q=London,uk&appid=b6907d289e10d714a6e88b30761fae22")
				.then().statusCode(200).log().all();
	}

	public void getSelfAPI() {
		RestAssured.baseURI = "http://localhost:3000";
		given().contentType("application/json").when().get("employees/1").then().log().all();
	}

	public void post() {
		JSONObject json = new JSONObject();
		json.put("id", "5");
		json.put("first_name", "Dummy");
		json.put("last_name", "D");
		json.put("email", "dummy@test.com");

		RestAssured.baseURI = "http://localhost:3000";
		given().header("Content-Type", "application/json").body(json.toString()).when().post("employees").then()
				.statusCode(201).and().log().all();
	}

	public void readJsonAndPost() throws IOException {
		FileInputStream fis = new FileInputStream(new File("./JSON Files/JsonRequest.json"));
		RestAssured.baseURI = "http://localhost:3000";
		given().header("Content-Type", "application/json").body(IOUtils.toByteArray(fis)).when().post("employees")
				.then().statusCode(201).and().log().all();
	}

	public void readClassJsonAndPost() throws IOException {
		JSONInputData data = new JSONInputData(7, "Dummy", "D", "haha@test.com");
		RestAssured.baseURI = "http://localhost:3000";
		given().header("Content-Type", "application/json").body(data).when().post("employees").then().statusCode(201)
				.and().log().all();
	}

	public void postAndReceive() throws IOException {
		JSONInputData data = new JSONInputData(12, "Dummy", "D", "haha@test.com");
		JSONOutputData oData = new JSONOutputData();
		Gson gson = new GsonBuilder().create();
		RestAssured.baseURI = "http://localhost:3000";
		Response response = given().header("Content-Type", "application/json").body(data).when().post("employees")
				.then().statusCode(201).and().log().all().and().extract().response();
		JsonPath path = new JsonPath(response.asString());
		path.get("id");  //path.getInt("id");

		oData = gson.fromJson(response.prettyPrint(), JSONOutputData.class);
		Assert.assertEquals(data.getId(), oData.getId());
	}

	public void delete() throws IOException {
		RestAssured.baseURI = "http://localhost:3000";
		given().header("Content-Type", "application/json").when().delete("employees/11").then().statusCode(200).and()
				.log().all();
	}

	public void postAsString() throws IOException {
		RestAssured.baseURI = "http://localhost:3000";
		given().header("Content-Type", "application/json")
				.body("{\r\n" + "    \"id\": 11,\r\n" + "    \"first_name\": \"Dummy\",\r\n"
						+ "    \"last_name\": \"D\",\r\n" + "    \"email\": \"haha@test.com\"\r\n" + "  }")
				.when().post("employees").then().statusCode(201).and().log().all().and().extract().response();
	}

	public void putAsString() throws IOException {
		RestAssured.baseURI = "http://localhost:3000";
		given().header("Content-Type", "application/json")
				.body("{\r\n" + "    \"id\": 11,\r\n" + "    \"first_name\": \"DummyUpdated\",\r\n"
						+ "    \"last_name\": \"D\",\r\n" + "    \"email\": \"haha@test.com\"\r\n" + "  }")
				.when().put("employees/11").then().statusCode(200).and().log().all().and().extract().response();
	}
	
	public void getUsingHTTPClient() throws ClientProtocolException, IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet getRequest = new HttpGet("http://localhost:3000/employees/11");
		getRequest.addHeader("Content-Type", "application/json");
		CloseableHttpResponse response = client.execute(getRequest);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		String respString = EntityUtils.toString(response.getEntity(),"UTF-8");
		System.out.println(respString);
	}
	
	public void postUsingHTTPClient() throws ClientProtocolException, IOException {
		String input = "{\r\n" + "    \"id\": 15,\r\n" + "    \"first_name\": \"DummyUpdated\",\r\n"
				+ "    \"last_name\": \"D\",\r\n" + "    \"email\": \"haha@test.com\"\r\n" + "  }";
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost postRequest = new HttpPost("http://localhost:3000/employees");
		postRequest.addHeader("Content-Type", "application/json");
		postRequest.setEntity(new StringEntity(input));
		CloseableHttpResponse response = client.execute(postRequest);
		Assert.assertEquals(201, response.getStatusLine().getStatusCode());
		String respString = EntityUtils.toString(response.getEntity(),"UTF-8");
		System.out.println(respString);
	}
	
	@Test
	public void readXMLAndPost() throws IOException {
		FileInputStream fis = new FileInputStream(new File("./Files/XMLRequest.xml"));
		RestAssured.baseURI = "http://currencyconverter.kowabunga.net";
		Response response = given().header("Content-Type", "text/xml").body(IOUtils.toByteArray(fis)).when().post("converter.asmx")
				.then().statusCode(200).and().log().all().and().extract().response();
		XmlPath path = new XmlPath(response.asString());
		System.out.println(path.getString("first_name"));
	}

}




















