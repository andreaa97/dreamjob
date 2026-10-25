package com.andrea.remoteflow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifica che il contesto Spring si avvii con la configurazione dell'applicazione
 * (application.properties): se manca un bean o una proprieta' il test fallisce.
 */
@SpringBootTest
class DreamJobTests {

	@Test
	void contextLoads() {
	}

}
