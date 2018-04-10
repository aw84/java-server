package pl.training.async;

import org.junit.Assert;
import org.junit.Test;

import pl.training.buffer.Buffer;
import pl.training.buffer.BufferAllocator;

public class BufferAllocatorTest {

	private final static BufferAllocator bufferAllocator = new BufferAllocator();

	@Test
	public void allocate() {
		Buffer b = bufferAllocator.allocate(10);
		b.read();
		b.write((byte) 'a');
		Assert.assertTrue(true);
	}
}
