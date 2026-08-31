package br.com.gestaocondial.autogestao.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gestaocondial.autogestao.domain.MeioContato;

public interface MeioContatoRepository extends JpaRepository<MeioContato, Long> {

	/** Meios de contato acompanham o morador: excluí-lo sem isso deixaria órfãos com FK quebrada. */
	void deleteByPessoaId(Long idPessoa);

}
