package br.com.gestaocondial.autogestao.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import br.com.gestaocondial.autogestao.api.PessoaApi;
import br.com.gestaocondial.autogestao.api.model.MeioContato;
import br.com.gestaocondial.autogestao.api.model.Pessoa;
import br.com.gestaocondial.autogestao.config.EscopoDoCondominio;
import br.com.gestaocondial.autogestao.dto.PessoaDto;
import br.com.gestaocondial.autogestao.mapper.PessoaApiMapper;
import br.com.gestaocondial.autogestao.service.PessoaService;

import lombok.RequiredArgsConstructor;

/**
 * Moradores. Toda leitura passa pelo {@link EscopoDoCondominio}: ter {@code MORADOR_LER} não
 * significa ver o cadastro de todos os condomínios, só o do perfil ativo da sessão.
 */
@RestController
@RequiredArgsConstructor
public class PessoaController implements PessoaApi {

	private final PessoaService pessoaService;

	private final PessoaApiMapper pessoaApiMapper;

	private final EscopoDoCondominio escopoDoCondominio;

	@Override
	@PreAuthorize("hasAuthority('MORADOR_LER')")
	public ResponseEntity<List<Pessoa>> findAll(Long idCondominio, Long idUnidade, Boolean semUnidade, String busca) {
		Long escopo = escopoDoCondominio.restringir(idCondominio);
		return ResponseEntity
				.ok(pessoaApiMapper.toApiList(pessoaService.findPessoas(escopo, idUnidade, semUnidade, busca)));
	}

	@Override
	@PreAuthorize("hasAuthority('MORADOR_LER')")
	public ResponseEntity<Pessoa> findPessoaId(Long idPessoa) {
		PessoaDto pessoa = pessoaService.findPessoaId(idPessoa);
		escopoDoCondominio.exigirAcesso(pessoa.getCondominio() == null ? null : pessoa.getCondominio().getId());
		return ResponseEntity.ok(pessoaApiMapper.toApi(pessoa));
	}

	@Override
	@PreAuthorize("hasAuthority('MORADOR_LER')")
	public ResponseEntity<List<Pessoa>> findPessoasCondominio(Long idCondominio) {
		escopoDoCondominio.exigirAcesso(idCondominio);
		return ResponseEntity.ok(pessoaApiMapper.toApiList(pessoaService.findPessoasCondominioId(idCondominio)));
	}

	@Override
	@PreAuthorize("hasAuthority('MORADOR_LER')")
	public ResponseEntity<List<Pessoa>> findEnvioPorEmailPorCondominio(Long id) {
		escopoDoCondominio.exigirAcesso(id);
		return ResponseEntity.ok(pessoaApiMapper.toApiList(pessoaService.findPessoasEnvioEmail(id)));
	}

	@Override
	@PreAuthorize("hasAuthority('MORADOR_ESCREVER')")
	public ResponseEntity<Pessoa> savePessoa(Pessoa pessoa) {
		PessoaDto dto = pessoaApiMapper.toDto(pessoa);
		escopoDoCondominio.exigirAcesso(dto.getCondominio() == null ? null : dto.getCondominio().getId());
		return new ResponseEntity<>(pessoaApiMapper.toApi(pessoaService.createNewPessoa(dto)), HttpStatus.CREATED);
	}

	@Override
	@PreAuthorize("hasAuthority('MORADOR_ESCREVER')")
	public ResponseEntity<Pessoa> updatePessoa(Long idPessoa, Pessoa pessoa) {
		PessoaDto atual = pessoaService.findPessoaId(idPessoa);
		escopoDoCondominio.exigirAcesso(atual.getCondominio() == null ? null : atual.getCondominio().getId());
		return ResponseEntity
				.ok(pessoaApiMapper.toApi(pessoaService.updatePessoa(idPessoa, pessoaApiMapper.toDto(pessoa))));
	}

	@Override
	@PreAuthorize("hasAuthority('MORADOR_ESCREVER')")
	public ResponseEntity<Void> deletePessoa(Long idPessoa) {
		PessoaDto atual = pessoaService.findPessoaId(idPessoa);
		escopoDoCondominio.exigirAcesso(atual.getCondominio() == null ? null : atual.getCondominio().getId());
		pessoaService.deletePessoa(idPessoa);
		return ResponseEntity.noContent().build();
	}

	@Override
	@PreAuthorize("hasAuthority('MORADOR_ESCREVER')")
	public ResponseEntity<MeioContato> saveMeioContato(MeioContato meioContato) {
		var criado = pessoaService.createMeioContato(pessoaApiMapper.toDto(meioContato));
		return new ResponseEntity<>(pessoaApiMapper.toApi(criado), HttpStatus.CREATED);
	}

}
