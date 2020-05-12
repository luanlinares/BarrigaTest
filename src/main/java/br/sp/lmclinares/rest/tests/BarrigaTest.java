package br.sp.lmclinares.rest.tests;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.runners.MethodSorters.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

import br.sp.lmclinares.rest.core.BaseTest;
import br.sp.lmclinares.rest.utils.DateUtils;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;

@FixMethodOrder(NAME_ASCENDING)
public class BarrigaTest extends BaseTest {
	
	private static String CONTA_NAME = "Conta " + System.nanoTime();
	private static Integer CONTA_ID;
	private static Integer MOV_ID;
	
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
	}
	
	//Cadastro de Movimenta��o
	private Movimentacao getMovimentacaoValida() {
		Movimentacao mov = new Movimentacao();
		mov.setConta_id(CONTA_ID);
//		mov.setUsuario_id(usuario_id);
		mov.setDescricao("Descri��o da Movimenta��o");
		mov.setEnvolvido("Envolvido na Movimenta��o");
		mov.setTipo("REC");
		mov.setData_transacao(DateUtils.getDataDiferencaDias(-1));
		mov.setData_pagamento(DateUtils.getDataDiferencaDias(5));
		mov.setValor(100f);
		mov.setStatus(true);
		return mov;
	}
	
	@Test
	public void t01_deveIncluirContaComSucesso() {
		CONTA_ID = given()	
			.body("{\"nome\": \""+CONTA_NAME+"\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
			.extract().path("id");
	}
	
	@Test
	public void t02_deveAlterarContaComSucesso() {
		given()	
			.body("{\"nome\": \""+CONTA_NAME+" Alterada\"}")
			.pathParam("id", CONTA_ID)
		.when()
			.put("/contas/{id}")
		.then()
			.statusCode(200)
			.body("nome", is(CONTA_NAME+ " Alterada"));
	}
	
	@Test
	public void t03_naoDeveIncluirContaComMesmoNome() {
		given()	
			.body("{\"nome\": \""+CONTA_NAME+" Alterada\"}")
		.when()
			.post("/contas")
		.then()
			.statusCode(400)
			.body("error", is("J� existe uma conta com esse nome!"));
	}
	
	@Test
	public void t04_deveInserirMovimentacaoSucesso  () {
		Movimentacao mov = getMovimentacaoValida();
		MOV_ID = given()	
			.body(mov)
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201)
			.extract().path("id");
	}
	
	@Test
	public void t05_deveValidarCamposObrigatoriosMovimentacao  () {
		given()	
			.body("{}")
		.when()
			.post("/transacoes")
		.then()
			.statusCode(400)
			.body("$", hasSize(8))
			.body("msg", hasItems(
					"Data da Movimenta��o � obrigat�rio",
					"Data do pagamento � obrigat�rio",
					"Descri��o � obrigat�rio",
					"Interessado � obrigat�rio",
					"Valor � obrigat�rio",
					"Valor deve ser um n�mero",
					"Conta � obrigat�rio",
					"Situa��o � obrigat�rio"));
	}
	
	@Test
	public void t06_naoDeveInserirMovimentacaoFutura  () {
		Movimentacao mov = getMovimentacaoValida();
		mov.setData_transacao(DateUtils.getDataDiferencaDias(2));
		
		given()	
			.body(mov)
		.when()
			.post("/transacoes")
		.then()
			.statusCode(400)
			.body("$", hasSize(1))
			.body("msg", hasItem("Data da Movimenta��o deve ser menor ou igual � data atual"));
	}
	

	
	@Test
	public void t07_naoDeveEcxluirContaComMovimentacao () {
		given()	
			.pathParam("id", CONTA_ID)
		.when()
			.delete("/contas/{id}")
		.then()
			.statusCode(500)
			.body("constraint", is("transacoes_conta_id_foreign"));
	}
	
	@Test
	public void t08_deveCalcularSaldoContas () {
		given()	
		.when()
			.get("/saldo")
		.then()
			.statusCode(200)
			.body("find{it.conta_id =="+CONTA_ID+"}.saldo", is("100.00"));
	}
	
	@Test
	public void t09_deveRemoverMovimentacao () {
		given()	
			.pathParam("id", MOV_ID)
		.when()
			.delete("/transacoes/{id}")
		.then()
			.statusCode(204);
	}
	
	@Test
	public void t10_naoDeveAcessarAPISemToken() {
		FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
		req.removeHeader("Authorization");
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401);
	}


}