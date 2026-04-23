package com.deustosport.my_app;

import com.deustosport.my_app.config.DataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:deustosport_app_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.sql.init.mode=never",
		"spring.jpa.show-sql=false"
})
class MyAppApplicationTests {

	@MockBean
	private DataInitializer dataInitializer;

	@Test
	void contextLoads() {
	}

}
