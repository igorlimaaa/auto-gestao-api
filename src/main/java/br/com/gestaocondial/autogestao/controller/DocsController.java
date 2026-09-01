package br.com.gestaocondial.autogestao.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DocsController {

	@GetMapping({"/docs", "/docs/"})
	public String docs() {
		return "forward:/docs/index.html";
	}

}
