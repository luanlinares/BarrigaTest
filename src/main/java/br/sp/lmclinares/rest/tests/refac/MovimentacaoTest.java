package br.sp.lmclinares.rest.tests.refac;

import static io.restassured.RestAssured.*;

import static org.hamcrest.Matchers.*;
import org.junit.Test;

import br.sp.lmclinares.rest.core.BaseTest;
import br.sp.lmclinares.rest.tests.Movimentacao;
import br.sp.lmclinares.rest.utils.BarrigaUtils;
import br.sp.lmclinares.rest.utils.DateUtils;

public class MovimentacaoTest extends BaseTest {
	
	
	
	//Cadastro de Movimentação
		private Movimentacao getMovimentacaoValida() {
			Movimentacao mov = new Movimentacao();
			mov.setConta_id(BarrigaUtils.getIdContaPeloNome("Conta para movimentacoes"));
//			mov.setUsuario_id(usuario_id);
			mov.setDescricao("Descrição da Movimentação");
			mov.setEnvolvido("Envolvido na Movimentação");
			mov.setTipo("REC");
			mov.setData_transacao(DateUtils.getDataDiferencaDias(-1));
			mov.setData_pagamento(DateUtils.getDataDiferencaDias(5));
			mov.setValor(100f);
			mov.setStatus(true);
			return mov;
		}
	
	@Test
	public void deveInserirMovimentacaoSucesso  () {
		Movimentacao mov = getMovimentacaoValida();
		given()	
			.body(mov)
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201)
			.body("valor", is("100.00"));
	}
	
	@Test
	public void deveValidarCamposObrigatoriosMovimentacao  () {
		given()	
			.body("{}")
		.when()
			.post("/transacoes")
		.then()
			.statusCode(400)
			.body("$", hasSize(8))
			.body("msg", hasItems(
					"Data da Movimentação é obrigatório",
					"Data do pagamento é obrigatório",
					"Descrição é obrigatório",
					"Interessado é obrigatório",
					"Valor é obrigatório",
					"Valor deve ser um número",
					"Conta é obrigatório",
					"Situação é obrigatório"));
	}
	
	@Test
	public void naoDeveInserirMovimentacaoFutura  () {
		Movimentacao mov = getMovimentacaoValida();
		mov.setData_transacao(DateUtils.getDataDiferencaDias(2));
		
		given()	
			.body(mov)
		.when()
			.post("/transacoes")
		.then()
			.statusCode(400)
			.body("$", hasSize(1))
			.body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"));
	}
	
	@Test
	public void naoDeveEcxluirContaComMovimentacao () {
		given()	
			.pathParam("id", BarrigaUtils.getIdContaPeloNome("Conta com movimentacao"))
		.when()
			.delete("/contas/{id}")
		.then()
			.statusCode(500)
			.body("constraint", is("transacoes_conta_id_foreign"));
	}
	
	@Test
	public void deveRemoverMovimentacao () {
		given()	
			.pathParam("id", BarrigaUtils.getIdMovimentacaoPelaDescricao("Movimentacao para exclusao"))
		.when()
			.delete("/transacoes/{id}")
		.then()
			.statusCode(204);
	}
}


