package pl.training.async;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Parser {
	protected final ByteBuffer buffer;

	public Parser(final ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public Message parse() {
		return Message.build(tokenize2());
	}

	public Message parse1() {
		return Message.build(tokenize());
	}

	protected Map<String, byte[]> tokenize2() {
		Map<String, byte[]> props = new HashMap<String, byte[]>();
		int idx = 0;
		int endlineCounter = 0;
		int bufferLength = buffer.position();
		buffer.flip();
		int c = 0;
		StringBuilder propertyNameBuilder = new StringBuilder(100);
		while (idx < bufferLength) {
			propertyNameBuilder.delete(0, 100);
			while (idx < bufferLength && (c = buffer.get(idx++)) != ':') {
				propertyNameBuilder.append((char) c);
			}
			if (propertyNameBuilder.length() == 0)
				throw new IllegalArgumentException("Cannot parse property name");
			int propertyValueStart = idx, propertyValueEnd = idx;
			while (idx < bufferLength && ((c = buffer.get(idx++)) != ' ' && c != '\r' && c != '\n')) {
				propertyValueEnd++;
			}
			if (propertyValueEnd - propertyValueStart == 0)
				throw new IllegalArgumentException("Cannot parse property value");
			byte[] pv = new byte[propertyValueEnd - propertyValueStart];
			buffer.position(propertyValueStart);
			buffer.get(pv, 0, pv.length);
			props.put(propertyNameBuilder.toString(), pv);
			endlineCounter = 0;
			while (idx < bufferLength) {
				c = buffer.get(idx);
				if (c == (int) '\r' || c == (int) '\n') {
					endlineCounter++;
				} else if (c == (int) ' ') {
					endlineCounter--;
				} else {
					break;
				}
				idx++;
			}
			if (endlineCounter == 3) {
				break;
			}
		}
		if (endlineCounter != 3) {
			throw new IllegalArgumentException("End of header marker missing");
		}
		if (idx < bufferLength) {
			byte[] injected_data = new byte[bufferLength - idx];
			buffer.position(idx);
			buffer.get(injected_data, 0, injected_data.length);
			props.put(new String("request-data"), injected_data);
		}
		return props;
	}

	protected Map<String, byte[]> tokenize() {
		Map<String, byte[]> props = new HashMap<String, byte[]>();
		int propertyNameStart, propertyNameEnd, propertyValueStart, propertyValueEnd, endlineCounter = 0, idx = 0;
		while (idx < buffer.position()) {
			// name
			propertyNameStart = propertyNameEnd = idx;
			while (idx < buffer.position() && buffer.get(idx++) != (int) ':') {
				propertyNameEnd++;
			}
			if (propertyNameEnd - propertyNameStart == 0)
				throw new IllegalArgumentException("Cannot parse property name");
			// value
			propertyValueStart = propertyValueEnd = idx;
			int c = -1;
			while (idx < buffer.position()
					&& ((c = buffer.get(idx++)) != (int) ' ' && c != (int) '\r' && c != (int) '\n')) {
				propertyValueEnd++;
			}
			if (propertyValueEnd - propertyValueStart == 0)
				throw new IllegalArgumentException("Cannot parse property value");
			byte[] pv = Arrays.copyOfRange(buffer.array(), propertyValueStart,
					propertyValueStart + propertyValueEnd - propertyValueStart);
			props.put(new String(buffer.array(), propertyNameStart, propertyNameEnd - propertyNameStart), pv);
			// property or line termination
			endlineCounter = 0;
			while (idx < buffer.position()) {
				c = buffer.get(idx);
				if (c == (int) '\r' || c == (int) '\n') {
					endlineCounter++;
				} else if (c == (int) ' ') {
					endlineCounter--;
				} else {
					break;
				}
				idx++;
			}
			if (endlineCounter == 3) {
				break;
			}
		}
		if (endlineCounter != 3) {
			throw new IllegalArgumentException("End of header marker missing");
		}
		int bufferLength = buffer.position();
		if (idx < bufferLength) {
			byte[] injected_data = Arrays.copyOfRange(buffer.array(), idx, bufferLength);
			props.put(new String("request-data"), injected_data);
		}
		return props;
	}
}
