package br.com.ialsolucoes.auto.gestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.ialsolucoes.auto.gestao.domain.MeioContato;
import br.com.ialsolucoes.auto.gestao.domain.Pessoa;
import br.com.ialsolucoes.auto.gestao.dto.MeioContatoDto;
import br.com.ialsolucoes.auto.gestao.dto.PessoaDto;

@Mapper(componentModel = "spring")
public interface PessoaMapper {

	PessoaDto pessoaDomainToDto (Pessoa pessoa);

	Pessoa pessoaDtoToDomain(PessoaDto pessoaDto);
	
	List<PessoaDto> listPessoaDomainToDto (List<Pessoa> pessoaDomain);
	
	MeioContato meioContatoDtoToDomain (MeioContatoDto meioContato);
	
	MeioContatoDto meioContatoDomainToDto (MeioContato meioContato);

}
