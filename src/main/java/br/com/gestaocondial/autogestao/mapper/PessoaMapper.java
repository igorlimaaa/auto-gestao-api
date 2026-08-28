package br.com.gestaocondial.autogestao.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import br.com.gestaocondial.autogestao.domain.MeioContato;
import br.com.gestaocondial.autogestao.domain.Pessoa;
import br.com.gestaocondial.autogestao.dto.MeioContatoDto;
import br.com.gestaocondial.autogestao.dto.PessoaDto;

@Mapper(componentModel = "spring")
public interface PessoaMapper {

	PessoaDto pessoaDomainToDto (Pessoa pessoa);

	Pessoa pessoaDtoToDomain(PessoaDto pessoaDto);
	
	List<PessoaDto> listPessoaDomainToDto (List<Pessoa> pessoaDomain);
	
	MeioContato meioContatoDtoToDomain (MeioContatoDto meioContato);
	
	MeioContatoDto meioContatoDomainToDto (MeioContato meioContato);

}
