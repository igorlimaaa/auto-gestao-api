package br.com.gestaocondial.autogestao.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErroDeParametroOutputDto {

    private String parametro;
    private String mensagem;

}
