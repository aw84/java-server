package pl.training.async;

import org.junit.Assert;
import org.junit.Test;

import pl.training.buffer.Buffer;
import pl.training.buffer.BufferAllocator;

public class BufferAllocatorTest {

	@Test
	public void allocate() {
		Buffer b = BufferAllocator.allocate(10);
		Assert.assertEquals(10, b.size());
	}

	@Test
	public void upsize() {
		Buffer b = BufferAllocator.allocate(10);
		b.resize(15);
		Assert.assertEquals(15, b.size());
	}

	@Test
	public void downsize() {
		Buffer b = BufferAllocator.allocate(10);
		b.resize(5);
		Assert.assertEquals(5, b.size());
	}

	// TODO: @Test
	public void dataPreservation() {
		Buffer b = BufferAllocator.allocate(3);
		b.write((byte) 'a');
		b.write((byte) 'b');
		b.write((byte) 'c');
		b.resize(5);
		b.write((byte) 'd');
		Assert.assertEquals((byte) 'a', b.read());
		Assert.assertEquals((byte) 'b', b.read());
		Assert.assertEquals((byte) 'c', b.read());
	}
}
