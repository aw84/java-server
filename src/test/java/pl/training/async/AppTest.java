package pl.training.async;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import pl.training.async.Message;
import pl.training.async.Parser;

public class AppTest extends TestCase {
	public AppTest(String testName) {
		super(testName);
	}

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

	public static Test suite() {
		return new TestSuite(AppTest.class);
	}

	private ByteBuffer getByteBuffer(String s) {
		ByteBuffer b = ByteBuffer.allocate(s.length()).put(s.getBytes(), 0, s.length());
		return b;
	}

	public void testParamsSpaceSeparated() {
		String testLine = "cl:1 te:p abc:444 a:99 c:490\r\n\r\n1";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("cl"), "1".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("te"), "p".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("abc"), "444".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("a"), "99".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("c"), "490".getBytes()));
	}

	public void testParamsNewLineSeparated() {
		String testLine = "cl:1\r\nte:p\r\nabc:444\r\na:99\r\nc:490\r\n\r\n1";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("cl"), "1".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("te"), "p".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("abc"), "444".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("a"), "99".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("c"), "490".getBytes()));
	}

	public void testTokenized1() {
		String testLine = "a:1 b:2 c:3\r\n\r\n";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("a"), "1".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("b"), "2".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("c"), "3".getBytes()));
	}

	public void testTokenized2() {
		String testLine = "a:1\r\n\r\n";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("a"), "1".getBytes()));
	}

	public void testTokenized3() {
		String testLine = "aaa:1\r\n\r\n";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("aaa"), "1".getBytes()));
	}

	public void testTokenized4() {
		String testLine = "aaa:1 bbb:222\r\n\r\n";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("aaa"), "1".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("bbb"), "222".getBytes()));
	}

	public void testTokenized5() {
		String testLine = "";
		try {
			getParser(testLine).publicTokenizer();
			Assert.fail();
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("End of header marker missing", e.getMessage());
		}
	}

	public void testTokenized6() {
		String testLine = "a";
		try {
			getParser(testLine).publicTokenizer();
			Assert.fail();
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("Cannot parse property value", e.getMessage());
		}
	}

	public void testFailInCaseOfPartiallyReadHeaders() {
		String testLine = "z:9";
		try {
			// partial read was done, detect that headers are incomplete
			getParser(testLine).publicTokenizer();
			Assert.fail();
		} catch (IllegalArgumentException e) {
			Assert.assertEquals("End of header marker missing", e.getMessage());
		}
	}

	public void testNewLineBetweenProps() {
		String testLine = "x:0\n\ry:9\r\n\r\n";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("x"), "0".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("y"), "9".getBytes()));
	}

	public void testSpaceBeforeAndAfterNewLine() {
		String testLine = "x:0 \n\r y:abcd-xyz\r\n\r\n";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("x"), "0".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("y"), "abcd-xyz".getBytes()));
	}

	public void testHeaderFrameWithDataInjected() {
		String testLine = "x:0 \n\r y:abcd-xyz\r\n\r\nDataDataData";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Assert.assertTrue(Arrays.equals(props.get("x"), "0".getBytes()));
		Assert.assertTrue(Arrays.equals(props.get("y"), "abcd-xyz".getBytes()));
	}

	public void testBuildMessageWithContentLength() {
		String testLine = "cl:123\r\n\r\nD";
		Map<String, byte[]> props = getParser(testLine).publicTokenizer();
		Message m = Message.build(props);
		Assert.assertEquals(123, m.getContentLength());
	}
}
