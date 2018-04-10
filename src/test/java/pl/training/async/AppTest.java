package pl.training.async;

import java.nio.ByteBuffer;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class AppTest {

	private static ByteBuffer buffer;

	@BeforeClass
	public static void setupClass() {
		buffer = ByteBuffer.allocateDirect(2 * 1024);
	}

	private Parser getParser(String s) {
		return new Parser(getByteBuffer(s));
	}

	private Map<String, byte[]> tokenize(String s) {
		return getParser(s).tokenize2();
	}

	private ByteBuffer getByteBuffer(String s) {
		buffer.clear();
		return buffer.put(s.getBytes(), 0, s.length());
	}

	@Test
	public void paramsSpaceSeparated() {
		String testLine = "cl:1 te:p abc:444 a:99 c:490\r\n\r\n1";
		Map<String, byte[]> props = tokenize(testLine);
		Assert.assertArrayEquals("1".getBytes(), props.get("cl"));
		Assert.assertArrayEquals("p".getBytes(), props.get("te"));
		Assert.assertArrayEquals("444".getBytes(), props.get("abc"));
		Assert.assertArrayEquals("99".getBytes(), props.get("a"));
		Assert.assertArrayEquals("490".getBytes(), props.get("c"));
	}

	@Test
	public void paramsNewLineSeparated() {
		String testLine = "cl:1\r\nte:p\r\nabc:444\r\na:99\r\nc:490\r\n\r\n1";
		Map<String, byte[]> props = tokenize(testLine);
		Assert.assertArrayEquals("1".getBytes(), props.get("cl"));
		Assert.assertArrayEquals("p".getBytes(), props.get("te"));
		Assert.assertArrayEquals("444".getBytes(), props.get("abc"));
		Assert.assertArrayEquals("99".getBytes(), props.get("a"));
		Assert.assertArrayEquals("490".getBytes(), props.get("c"));
	}

	@Test
	public void tokenized1() {
		String testLine = "a:1 b:2 c:3\r\n\r\n";
		Map<String, byte[]> props = tokenize(testLine);
		Assert.assertArrayEquals("1".getBytes(), props.get("a"));
		Assert.assertArrayEquals("2".getBytes(), props.get("b"));
		Assert.assertArrayEquals("3".getBytes(), props.get("c"));
	}

	@Test
	public void tokenized2() {
		String testLine = "a:1\r\n\r\n";
		Map<String, byte[]> props = tokenize(testLine);
		Assert.assertArrayEquals("1".getBytes(), props.get("a"));
	}

	@Test
	public void tokenized3() {
		String testLine = "aaa:1\r\n\r\n";
		Map<String, byte[]> props = tokenize(testLine);
		Assert.assertArrayEquals("1".getBytes(), props.get("aaa"));
	}

	@Test
	public void tokenized4() {
		String testLine = "aaa:1 bbb:222\r\n\r\n";
		Map<String, byte[]> props = tokenize(testLine);
		Assert.assertArrayEquals("1".getBytes(), props.get("aaa"));
		Assert.assertArrayEquals("222".getBytes(), props.get("bbb"));
	}

	@Test
	public void tokenized5() {
		String testLine = "";
		try {
			tokenize(testLine);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("End of header marker missing", e.getMessage());
		}
	}

	@Test
	public void tokenized6() {
		String testLine = "a";
		try {
			tokenize(testLine);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Cannot parse property value", e.getMessage());
		}
	}

	@Test
	public void failInCaseOfPartiallyReadHeaders() {
		String testLine = "z:9";
		try {
			// partial read was done, detect that headers are incomplete
			tokenize(testLine);
			Assert.fail();
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("End of header marker missing", e.getMessage());
		}
	}

	@Test
	public void newLineBetweenProps() {
		String testLine = "x:0\n\ry:9\r\n\r\n";
		Map<String, byte[]> props = tokenize(testLine);
		Assert.assertArrayEquals("0".getBytes(), props.get("x"));
		Assert.assertArrayEquals("9".getBytes(), props.get("y"));
	}

	@Test
	public void spaceBeforeAndAfterNewLine() {
		String testLine = "x:0 \n\r y:abcd-xyz\r\n\r\n";
		Map<String, byte[]> props = tokenize(testLine);
		Assert.assertArrayEquals("0".getBytes(), props.get("x"));
		Assert.assertArrayEquals("abcd-xyz".getBytes(), props.get("y"));
	}

	@Test
	public void headerFrameWithDataInjected() {
		String testLine = "x:0 \n\r y:abcd-xyz\r\n\r\nDataDataData";
		Map<String, byte[]> props = tokenize(testLine);
		Assert.assertArrayEquals("0".getBytes(), props.get("x"));
		Assert.assertArrayEquals("abcd-xyz".getBytes(), props.get("y"));
	}

	@Test
	public void buildMessageWithContentLength() {
		String testLine = "cl:123\r\n\r\nD";
		Map<String, byte[]> props = tokenize(testLine);
		Message m = Message.build(props);
		Assert.assertEquals(123, m.getContentLength());
	}

	@Test
	public void getInjectedData() {
		String testLine = "cl:1\r\n\r\nD";
		Map<String, byte[]> props = tokenize(testLine);
		Message m = Message.build(props);
		Assert.assertEquals(1, m.getContentLength());
		Assert.assertArrayEquals("D".getBytes(), m.getBody());
	}
}
