package pl.training.buffer;

public class BufferAllocator {

	public Buffer allocate(int capacity) {
		return new Buffer(capacity);
	}

	public void free(Buffer buffer) {

	}

	public Buffer reallocate(int newLength) {
		return new Buffer(newLength);
	}
}
