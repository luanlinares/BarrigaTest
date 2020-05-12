package br.sp.lmclinares.rest.utils;

import static io.restassured.RestAssured.get;

public class BarrigaUtils {
	public static Integer getIdContaPeloNome(String nome) {
		return get("/contas?nome="+nome).then().extract().path("id[0]");
	}
	
	public static Integer getIdMovimentacaoPelaDescricao(String desc) {
		return get("/transacoes?descricao="+desc).then().extract().path("id[0]");
	}
}
