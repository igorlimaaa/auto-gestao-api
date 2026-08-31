package br.com.gestaocondial.autogestao.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.domain.Unidade;
import br.com.gestaocondial.autogestao.dto.UnidadeDto;
import br.com.gestaocondial.autogestao.exception.CondominioNaoEncontradoException;
import br.com.gestaocondial.autogestao.exception.RecursoEmUsoException;
import br.com.gestaocondial.autogestao.exception.UnidadeJaExisteException;
import br.com.gestaocondial.autogestao.exception.UnidadeNaoEncontradaException;
import br.com.gestaocondial.autogestao.mapper.UnidadeMapper;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.PessoaRepository;
import br.com.gestaocondial.autogestao.repository.UnidadeRepository;
import br.com.gestaocondial.autogestao.repository.specification.UnidadeSpecifications;
import br.com.gestaocondial.autogestao.service.UnidadeService;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class UnidadeImpl implements UnidadeService {

	private final UnidadeRepository unidadeRepository;

	private final CondominioRepository condominioRepository;

	private final PessoaRepository pessoaRepository;

	private final UnidadeMapper mapper;

	@Override
	@Transactional(readOnly = true)
	public List<UnidadeDto> listUnidades(Long idCondominio, Boolean ativa, String busca) {
		Specification<Unidade> filtro = Specification.allOf(
				UnidadeSpecifications.doCondominio(idCondominio),
				UnidadeSpecifications.comAtiva(ativa),
				UnidadeSpecifications.comBusca(busca));

		return unidadeRepository.findAll(filtro).stream().map(this::comContagemDeMoradores).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public UnidadeDto findUnidade(Long idUnidade) {
		return comContagemDeMoradores(porId(idUnidade));
	}

	@Override
	@Transactional
	public UnidadeDto createUnidade(UnidadeDto unidadeDto) {
		Condominio condominio = condominioDe(unidadeDto);
		String bloco = blocoNormalizado(unidadeDto.getBloco());
		exigirIdentificacaoInedita(condominio.getId(), bloco, unidadeDto.getNumero().trim(), null);

		Unidade unidade = new Unidade();
		unidade.setCondominio(condominio);
		aplicar(unidade, unidadeDto, bloco);
		unidade.setAtiva(unidadeDto.getAtiva() == null || unidadeDto.getAtiva());

		return comContagemDeMoradores(unidadeRepository.save(unidade));
	}

	@Override
	@Transactional
	public UnidadeDto updateUnidade(Long idUnidade, UnidadeDto unidadeDto) {
		Unidade unidade = porId(idUnidade);
		Condominio condominio = condominioDe(unidadeDto);
		String bloco = blocoNormalizado(unidadeDto.getBloco());
		exigirIdentificacaoInedita(condominio.getId(), bloco, unidadeDto.getNumero().trim(), idUnidade);

		unidade.setCondominio(condominio);
		aplicar(unidade, unidadeDto, bloco);
		if (unidadeDto.getAtiva() != null) {
			unidade.setAtiva(unidadeDto.getAtiva());
		}

		return comContagemDeMoradores(unidadeRepository.save(unidade));
	}

	@Override
	@Transactional
	public void deleteUnidade(Long idUnidade) {
		Unidade unidade = porId(idUnidade);

		long moradores = pessoaRepository.countByUnidadeId(idUnidade);
		if (moradores > 0) {
			throw new RecursoEmUsoException("A unidade " + identificacao(unidade) + " tem " + moradores
					+ (moradores == 1 ? " morador vinculado" : " moradores vinculados")
					+ ". Desvincule antes de excluí-la.");
		}

		unidadeRepository.delete(unidade);
	}

	private void aplicar(Unidade unidade, UnidadeDto dto, String bloco) {
		unidade.setBloco(bloco);
		unidade.setNumero(dto.getNumero().trim());
		unidade.setAndar(dto.getAndar());
		unidade.setFracaoIdeal(dto.getFracaoIdeal());
		unidade.setObservacao(textoOuNulo(dto.getObservacao()));
	}

	private Condominio condominioDe(UnidadeDto dto) {
		Long idCondominio = dto.getCondominio() == null ? null : dto.getCondominio().getId();
		if (idCondominio == null) {
			throw new IllegalArgumentException("Informe o condomínio da unidade.");
		}
		return condominioRepository.findById(idCondominio)
				.orElseThrow(() -> new CondominioNaoEncontradoException(
						"Condomínio " + idCondominio + " não encontrado."));
	}

	private void exigirIdentificacaoInedita(Long idCondominio, String bloco, String numero, Long idIgnorado) {
		Optional<Unidade> existente = unidadeRepository.findByCondominioIdAndBlocoAndNumero(idCondominio, bloco,
				numero);

		if (existente.isPresent() && !existente.get().getId().equals(idIgnorado)) {
			throw new UnidadeJaExisteException("Este condomínio já tem a unidade "
					+ (bloco.isEmpty() ? numero : bloco + " " + numero) + ".");
		}
	}

	/** Sem bloco é string vazia, não nulo: é o que mantém o UNIQUE simples. Ver migration V3. */
	private static String blocoNormalizado(String bloco) {
		return bloco == null ? "" : bloco.trim();
	}

	private Unidade porId(Long idUnidade) {
		return unidadeRepository.findById(idUnidade)
				.orElseThrow(() -> new UnidadeNaoEncontradaException("Unidade " + idUnidade + " não encontrada."));
	}

	private UnidadeDto comContagemDeMoradores(Unidade unidade) {
		UnidadeDto dto = mapper.unidadeDomainToDto(unidade);
		dto.setQuantidadeDeMoradores((int) pessoaRepository.countByUnidadeId(unidade.getId()));
		return dto;
	}

	private static String identificacao(Unidade unidade) {
		String bloco = unidade.getBloco();
		return bloco == null || bloco.isEmpty() ? unidade.getNumero() : bloco + " " + unidade.getNumero();
	}

	private static String textoOuNulo(String valor) {
		return valor == null || valor.isBlank() ? null : valor.trim();
	}

}
