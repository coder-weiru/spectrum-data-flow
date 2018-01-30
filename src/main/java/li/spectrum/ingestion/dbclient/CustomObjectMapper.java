
package li.spectrum.ingestion.dbclient;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
/**
 * A customization of Jackson's ObjectMapper that configures
 * dates to be serialized as timestamps.
 * TODO Java Client API will probably provide these same
 * defaults, so this can be removed or fetched from Java Client API.
 */
@SuppressWarnings("serial")
public class CustomObjectMapper extends ObjectMapper {

	public static String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ";

	/**
	 * A formatter used to make dateTime strings in JSON serializations
	 */
	public static class ISO8601Formatter {
		public static String format(Date date) {
			return new SimpleDateFormat(ISO_8601_FORMAT).format(date);
		}
	}

    public CustomObjectMapper() {
        super();
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        setDateFormat(new SimpleDateFormat(ISO_8601_FORMAT));
    }
}