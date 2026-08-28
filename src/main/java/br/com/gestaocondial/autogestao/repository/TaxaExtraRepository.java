package br.com.gestaocondial.autogestao.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gestaocondial.autogestao.domain.TaxaExtra;

public interface TaxaExtraRepository extends JpaRepository<TaxaExtra, Long> {

	List<TaxaExtra> findAllByCondominioId(Long condominioId);

}
