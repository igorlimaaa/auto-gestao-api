package br.com.gestaocondial.autogestao.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gestaocondial.autogestao.domain.CobrancaOrdinaria;
import br.com.gestaocondial.autogestao.dto.CobrancaOrdinariaDto;
import br.com.gestaocondial.autogestao.enumeration.SituacaoCobrancaEnum;
import br.com.gestaocondial.autogestao.exception.CobrancaOrdinariaNaoEncontradaException;
import br.com.gestaocondial.autogestao.mapper.CobrancaOrdinariaMapper;
import br.com.gestaocondial.autogestao.repository.CobrancaOrdinariaRepository;
import br.com.gestaocondial.autogestao.repository.specification.CobrancaOrdinariaSpecifications;
import br.com.gestaocondial.autogestao.service.CobrancaOrdinariaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CobrancaOrdinariaImpl implements CobrancaOrdinariaService {

	private final CobrancaOrdinariaRepository cobrancaRepository;

	private final CobrancaOrdinariaMapper mapper;

	@Override
	@Transactional(readOnly = true)
	public List<CobrancaOrdinariaDto> listCobrancas(Long idCondominio, Long idUnidade, LocalDate competencia) {
		Specification<CobrancaOrdinaria> filtro = Specification.allOf(
				CobrancaOrdinariaSpecifications.doCondominio(idCondominio),
				CobrancaOrdinariaSpecifications.doUnidade(idUnidade),
				CobrancaOrdinariaSpecifications.daCompetencia(competencia));

		return cobrancaRepository.findAll(filtro).stream().map(this::comSituacao).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public CobrancaOrdinariaDto findCobranca(Long idCobrancaOrdinaria) {
		return comSituacao(porId(idCobrancaOrdinaria));
	}

	@Override
	@Transactional
	public CobrancaOrdinariaDto atualizarDadosDePagamento(Long idCobrancaOrdinaria, String pixCopiaCola,
			String linhaDigitavel) {
		CobrancaOrdinaria cobranca = porId(idCobrancaOrdinaria);
		cobranca.setPixCopiaCola(textoOuNulo(pixCopiaCola));
		cobranca.setLinhaDigitavel(textoOuNulo(linhaDigitavel));
		return comSituacao(cobrancaRepository.save(cobranca));
	}

	@Override
	@Transactional
	public CobrancaOrdinariaDto registrarPagamento(Long idCobrancaOrdinaria, LocalDate dataPagamento) {
		CobrancaOrdinaria cobranca = porId(idCobrancaOrdinaria);
		cobranca.setDataPagamento(dataPagamento != null ? dataPagamento : LocalDate.now());
		return comSituacao(cobrancaRepository.save(cobranca));
	}

	private CobrancaOrdinaria porId(Long idCobrancaOrdinaria) {
		return cobrancaRepository.findById(idCobrancaOrdinaria)
				.orElseThrow(() -> new CobrancaOrdinariaNaoEncontradaException(
						"Cobrança " + idCobrancaOrdinaria + " não encontrada."));
	}

	private CobrancaOrdinariaDto comSituacao(CobrancaOrdinaria cobranca) {
		CobrancaOrdinariaDto dto = mapper.cobrancaOrdinariaDomainToDto(cobranca);
		dto.setSituacao(calcularSituacao(cobranca));
		return dto;
	}

	private static SituacaoCobrancaEnum calcularSituacao(CobrancaOrdinaria cobranca) {
		if (cobranca.getDataPagamento() != null) {
			return SituacaoCobrancaEnum.PAGO;
		}
		return LocalDate.now().isAfter(cobranca.getDataVencimento())
				? SituacaoCobrancaEnum.INADIMPLENTE
				: SituacaoCobrancaEnum.PENDENTE;
	}

	private static String textoOuNulo(String valor) {
		return valor == null || valor.isBlank() ? null : valor.trim();
	}

}
