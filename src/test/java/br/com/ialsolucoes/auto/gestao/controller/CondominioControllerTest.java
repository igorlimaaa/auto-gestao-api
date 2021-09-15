package br.com.ialsolucoes.auto.gestao.controller;

import static org.junit.jupiter.api.Assertions.fail;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@TestPropertySource(properties = { "DB_NAME=gestao_test", "spring.jpa.hibernate.ddlAuto:create-drop" })
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Transactional
class CondominioControllerTest {

	@Autowired
	private MockMvc mockmvc;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void should() {
		
	}

}
