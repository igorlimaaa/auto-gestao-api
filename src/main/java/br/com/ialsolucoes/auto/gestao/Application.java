package br.com.ialsolucoes.auto.gestao;

import javax.annotation.Resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.com.ialsolucoes.auto.gestao.testes.FilesStorageService;

@SpringBootApplication
public class Application {

	@Resource
	FilesStorageService storageService;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	public void run(String... arg) throws Exception {
		storageService.deleteAll();
		storageService.init();
	}

}
