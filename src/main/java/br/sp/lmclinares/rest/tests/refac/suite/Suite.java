package br.sp.lmclinares.rest.tests.refac.suite;

import static io.restassured.RestAssured.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import br.sp.lmclinares.rest.core.BaseTest;
import br.sp.lmclinares.rest.tests.refac.AuthTest;
import br.sp.lmclinares.rest.tests.refac.ContasTest;
import br.sp.lmclinares.rest.tests.refac.MovimentacaoTest;
import br.sp.lmclinares.rest.tests.refac.SaldoTest;
import io.restassured.RestAssured;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses({
	ContasTest.class,
	MovimentacaoTest.class,
	SaldoTest.class,
	AuthTest.class
})
public class Suite extends BaseTest {
	@BeforeClass
	public static void login() {
		Map<String, String> login = new HashMap<>();
		login.put("email", "test@rest.com.br");
		login.put("senha", "123456");
		
		String TOKEN = given()	
			.body(login)
		.when()
			.post("/signin")
		.then()
			.statusCode(200)
			.extract().path("token");
		
		RestAssured.requestSpecification.header("Authorization","JWT " + TOKEN);
		RestAssured.get("/reset").then().statusCode(200);
	}
}
