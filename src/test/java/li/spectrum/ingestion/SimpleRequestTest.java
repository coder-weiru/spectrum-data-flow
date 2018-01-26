package li.spectrum.ingestion;

import java.text.ParseException;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppConfig.class, MessageChannels.class })
public class SimpleRequestTest {

	@Autowired
	SimpleRequest simpleRequest;

	@Test
	public final void testLaunch() throws ParseException {
		Map<String, String> map = simpleRequest.launch();

		System.out.println(map);
	}

}
