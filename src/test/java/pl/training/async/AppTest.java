package pl.training.async;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class AppTest {
	class ParserTestable extends Parser {

		public ParserTestable(ByteBuffer buffer) {
			super(buffer);
		}

		public Map<String, byte[]> publicTokenizer() {
			return tokenize2();
		}
	}

	private ParserTestable getParser(String s) {
		return new ParserTestable(getByteBuffer(s));
	}

	private ByteBuffer getByteBuffer(String s) {
		ByteBuffer b = ByteBuffer.allocate(s.length()).put(s.getBytes(), 0, s.length());
		return b;
	}

	@Test
	public void paramsSpaceSeparated() {
		String testLine = "cl:1 te:p abc:444 a:99 c:490\r\n\r\n1";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("cl"), "1".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("te"), "p".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("abc"), "444".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("a"), "99".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("c"), "490".getBytes()));
	}

	@Test
	public void paramsNewLineSeparated() {
		String testLine = "cl:1\r\nte:p\r\nabc:444\r\na:99\r\nc:490\r\n\r\n1";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("cl"), "1".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("te"), "p".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("abc"), "444".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("a"), "99".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("c"), "490".getBytes()));
	}

	@Test
	public void tokenized1() {
		String testLine = "a:1 b:2 c:3\r\n\r\n";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("a"), "1".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("b"), "2".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("c"), "3".getBytes()));
	}

	@Test
	public void tokenized2() {
		String testLine = "a:1\r\n\r\n";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("a"), "1".getBytes()));
	}

	@Test
	public void tokenized3() {
		String testLine = "aaa:1\r\n\r\n";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("aaa"), "1".getBytes()));
	}

	@Test
	public void tokenized4() {
		String testLine = "aaa:1 bbb:222\r\n\r\n";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("aaa"), "1".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("bbb"), "222".getBytes()));
	}

	@Test
	public void tokenized5() {
		String testLine = "";
		try {
			getParser(testLine).publicTokenizer();
			Assert.fail();
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("End of header marker missing", e.getMessage());
		}
	}

	@Test
	public void tokenized6() {
		String testLine = "a";
		try {
			getParser(testLine).publicTokenizer();
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
			getParser(testLine).publicTokenizer();
			Assert.fail();
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("End of header marker missing", e.getMessage());
		}
	}

	@Test
	public void newLineBetweenProps() {
		String testLine = "x:0\n\ry:9\r\n\r\n";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("x"), "0".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("y"), "9".getBytes()));
	}

	@Test
	public void spaceBeforeAndAfterNewLine() {
		String testLine = "x:0 \n\r y:abcd-xyz\r\n\r\n";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("x"), "0".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("y"), "abcd-xyz".getBytes()));
	}

	@Test
	public void headerFrameWithDataInjected() {
		String testLine = "x:0 \n\r y:abcd-xyz\r\n\r\nDataDataData";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("x"), "0".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("y"), "abcd-xyz".getBytes()));
	}

	@Test
	public void buildMessageWithContentLength() {
		String testLine = "cl:123\r\n\r\nD";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Message m = Message.build(props);
		Assert.assertEquals(123, m.getContentLength());
	}
}
