package br.com.gestaocondial.autogestao.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import br.com.gestaocondial.autogestao.config.EscopoDoCondominio;
import br.com.gestaocondial.autogestao.domain.AreaComum;
import br.com.gestaocondial.autogestao.domain.Condominio;
import br.com.gestaocondial.autogestao.dto.AreaComumDto;
import br.com.gestaocondial.autogestao.enumeration.ModoDeReservaEnum;
import br.com.gestaocondial.autogestao.exception.AreaComumJaExisteException;
import br.com.gestaocondial.autogestao.exception.AreaComumNaoEncontradaException;
import br.com.gestaocondial.autogestao.exception.CondominioNaoEncontradoException;
import br.com.gestaocondial.autogestao.exception.RecursoEmUsoException;
import br.com.gestaocondial.autogestao.mapper.AreaComumMapper;
import br.com.gestaocondial.autogestao.repository.AreaComumRepository;
import br.com.gestaocondial.autogestao.repository.CondominioRepository;
import br.com.gestaocondial.autogestao.repository.EventoRepository;
import br.com.gestaocondial.autogestao.repository.specification.AreaComumSpecifications;
import br.com.gestaocondial.autogestao.service.AreaComumService;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class AreaComumImpl implements AreaComumService {

	private final AreaComumRepository areaRepository;

	private final CondominioRepository condominioRepository;

	private final EventoRepository eventoRepository;

	private final AreaComumMapper mapper;

	private final EscopoDoCondominio escopo;

	@Override
	@Transactional(readOnly = true)
	public List<AreaComumDto> listAreas(Long idCondominio, Boolean ativa, Boolean reservavel) {
		Specification<AreaComum> filtro = Specification.allOf(
				AreaComumSpecifications.doCondominio(escopo.restringir(idCondominio)),
				AreaComumSpecifications.comAtiva(ativa),
				AreaComumSpecifications.comReservavel(reservavel));
		return mapper.toDtoList(areaRepository.findAll(filtro, Sort.by("nome").ascending()));
	}

	@Override
	@Transactional
	public AreaComumDto createArea(AreaComumDto dto) {
		Condominio condominio = condominioDe(dto.getIdCondominio());
		String nome = dto.getNome().trim();
		if (areaRepository.existsByCondominioIdAndNomeIgnoreCase(condominio.getId(), nome)) {
			throw new AreaComumJaExisteException("Este condomínio já tem a área \"" + nome + "\".");
		}

		AreaComum area = new AreaComum();
		area.setCondominio(condominio);
		aplicar(area, dto);
		return mapper.toDto(areaRepository.save(area));
	}

	@Override
	@Transactional
	public AreaComumDto updateArea(Long idArea, AreaComumDto dto) {
		AreaComum area = porId(idArea);
		escopo.exigirAcesso(area.getCondominio().getId());

		String nome = dto.getNome().trim();
		if (areaRepository.existsByCondominioIdAndNomeIgnoreCaseAndIdNot(area.getCondominio().getId(), nome,
				idArea)) {
			throw new AreaComumJaExisteException("Este condomínio já tem a área \"" + nome + "\".");
		}

		aplicar(area, dto);
		return mapper.toDto(areaRepository.save(area));
	}

	@Override
	@Transactional
	public void deleteArea(Long idArea) {
		AreaComum area = porId(idArea);
		escopo.exigirAcesso(area.getCondominio().getId());

		if (eventoRepository.existsByAreaComumId(idArea)) {
			throw new RecursoEmUsoException("A área \"" + area.getNome()
					+ "\" tem eventos na agenda. Desative-a em vez de excluir, para manter o histórico.");
		}
		areaRepository.delete(area);
	}

	private void aplicar(AreaComum area, AreaComumDto dto) {
		boolean diaInteiro = dto.getModoDeReserva() == ModoDeReservaEnum.DIA_INTEIRO;
		if ((dto.getHoraAbertura() == null) != (dto.getHoraFechamento() == null)) {
			throw new IllegalArgumentException("Informe a abertura e o fechamento, ou nenhum dos dois.");
		}
		if (dto.getHoraAbertura() != null && !dto.getHoraFechamento().isAfter(dto.getHoraAbertura())) {
			throw new IllegalArgumentException("O fechamento precisa ser depois da abertura.");
		}

		area.setNome(dto.getNome().trim());
		area.setDescricao(dto.getDescricao() == null || dto.getDescricao().isBlank() ? null : dto.getDescricao().trim());
		area.setReservavel(Boolean.TRUE.equals(dto.getReservavel()));
		area.setModoDeReserva(diaInteiro ? ModoDeReservaEnum.DIA_INTEIRO : ModoDeReservaEnum.POR_HORARIO);
		area.setCapacidade(dto.getCapacidade());
		// Reserva de dia inteiro ignora o horário de funcionamento: guardar um deixaria a regra ambígua.
		area.setHoraAbertura(diaInteiro ? null : dto.getHoraAbertura());
		area.setHoraFechamento(diaInteiro ? null : dto.getHoraFechamento());
		if (dto.getAtiva() != null) {
			area.setAtiva(dto.getAtiva());
		}
	}

	private Condominio condominioDe(Long idPedido) {
		Long id = idPedido != null ? idPedido : escopo.doPerfilAtivo();
		if (id == null) {
			throw new IllegalArgumentException("Informe o condomínio da área.");
		}
		escopo.exigirAcesso(id);
		return condominioRepository.findById(id)
				.orElseThrow(() -> new CondominioNaoEncontradoException("Condomínio " + id + " não encontrado."));
	}

	private AreaComum porId(Long idArea) {
		return areaRepository.findById(idArea)
				.orElseThrow(() -> new AreaComumNaoEncontradaException("Área " + idArea + " não encontrada."));
	}

}
