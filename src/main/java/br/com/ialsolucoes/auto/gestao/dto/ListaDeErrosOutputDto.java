package br.com.ialsolucoes.auto.gestao.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ListaDeErrosOutputDto {

    private final List<ErroDeParametroOutputDto> erros = 
    							new ArrayList<ErroDeParametroOutputDto>();

    public void adicionaErroEmParametro(String parametro, String mensagem) {
    	erros.add(new ErroDeParametroOutputDto(parametro, mensagem));
    }

    public int getQuantidadeDeErros() {
        return erros.size();
    }

}
