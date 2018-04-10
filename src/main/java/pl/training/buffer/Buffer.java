package pl.training.buffer;

import java.nio.ByteBuffer;

public class Buffer {
	private ByteBuffer buffer;

	public Buffer(int capacity) {
		this.buffer = ByteBuffer.allocate(capacity);
	}

	public byte read() {
		return buffer.get();
	}

	public void write(byte b) {
		buffer.put(b);
	}
}
