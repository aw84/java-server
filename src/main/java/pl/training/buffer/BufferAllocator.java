package pl.training.buffer;

import java.nio.ByteBuffer;

public class BufferAllocator {

	public static Buffer allocate(int capacity) {
		ByteBuffer newByteBuffer = ByteBuffer.allocateDirect(capacity);
		return new Buffer(newByteBuffer);
	}

	public void free(Buffer buffer) {

	}

	public static Buffer resize(Buffer b, int newLength) {
		ByteBuffer newByteBuffer = ByteBuffer.allocateDirect(newLength);
		newByteBuffer.clear();
		b.buffer().flip();
		if (b.buffer().remaining() > newLength) {
			newByteBuffer.put(b.buffer().array(), b.buffer().position(), newLength);
		} else {
			newByteBuffer.put(b.buffer());
		}
		newByteBuffer.flip();
		b.setBuffer(newByteBuffer);
		return b;
	}
}
