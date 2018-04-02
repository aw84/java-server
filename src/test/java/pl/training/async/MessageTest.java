package pl.training.async;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MessageTest {
	private static Message message;

	@BeforeClass
	public static void initMessage() {
		Map<String, byte[]> props = new HashMap<String, byte[]>();
		props.put("cl", "123".getBytes());
		props.put("te", "chunked".getBytes());
		props.put("request-data", "DataDataData".getBytes());
		message = Message.build(props);
	}

	@Test
	public void getContentLength() {
		Assert.assertEquals(123, message.getContentLength());
	}

	@Test
	public void getBody() {
		String body = new String(message.getBody());
		Assert.assertEquals("DataDataData", body);
	}

	@Test
	public void getTransferEncoding() {
		Assert.assertEquals("chunked", message.getTransferEncoding());
	}
}
