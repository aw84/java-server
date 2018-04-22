package pl.training.buffer;

import java.nio.ByteBuffer;

public class Buffer {
	private ByteBuffer buffer;

	public static Buffer create(int length) {
		return BufferAllocator.allocate(length);
	}

	public Buffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public int size() {
		return buffer.capacity();
	}

	public Buffer resize(int newSize) {
		return BufferAllocator.resize(this, newSize);
	}

	public byte read() {
		return buffer.get();
	}

	public void write(byte b) {
		buffer.put(b);
	}

	public void write(Buffer b) {
		buffer.put(b.buffer);
	}

	public ByteBuffer buffer() {
		return buffer;
	}

	public void setBuffer(ByteBuffer newByteBuffer) {
		this.buffer = newByteBuffer;
	}
}
