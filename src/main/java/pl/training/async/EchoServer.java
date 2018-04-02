package pl.training.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

public class EchoServer implements CompletionHandler<AsynchronousSocketChannel, Object> {
	private static final int MAX_MESSAGE_SIZE = 512;
	// TODO: private static final int MAX_REQUEST_SIZE = 4096;
	AsynchronousServerSocketChannel serverChannel;

	public EchoServer(String address, int port) throws IOException {
		InetSocketAddress socketAddress = new InetSocketAddress(address, port);
		serverChannel = AsynchronousServerSocketChannel.open().bind(socketAddress);
		accept();
	}

	public void completed(AsynchronousSocketChannel result, Object attachment) {
		accept();
		ByteBuffer buffer = ByteBuffer.allocate(MAX_MESSAGE_SIZE); // Note: allocateDirect?
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("operation", "read-headers");
		buffer.clear();
		context.put("buffer", buffer);
		context.put("clientChannel", result);
		result.read(buffer, result, new ReadWriteHandler(context));
	}

	public void failed(Throwable exc, Object attachment) {
		exc.printStackTrace();
	}

	private void accept() {
		serverChannel.accept(serverChannel, this);
	}

	class ReadWriteHandler implements CompletionHandler<Integer, Object> {
		private Map<String, Object> context;
		private Message request;

		public ReadWriteHandler(Map<String, Object> context) {
			this.context = context;
		}

		public void completed(Integer result, Object attachment) {
			System.out.println("Resutl: " + result + "  Op: " + context.get("operation"));
			ByteBuffer buffer = (ByteBuffer) context.get("buffer");
			AsynchronousSocketChannel ch = (AsynchronousSocketChannel) context.get("clientChannel");
			if ("read-headers".equals(context.get("operation"))) {
				Parser parser = new Parser(buffer);
				request = parser.parse();
				if (request.getContentLength() > 0) {
					if (request.getBodyLength() < request.getContentLength()) {
						context.put("operation", "write-ack");
						buffer.clear();
						buffer.put("100-continue\r\n".getBytes());
						buffer.flip();
						ch.write(buffer, result, this);
					} else {
						context.put("operation", "write-headers");
						reverse(buffer);
						buffer.flip();
						ch.write(buffer, result, this);
					}
				}
			} else if ("read-body".equals(context.get("operation"))) {
				reverse(buffer);
				context.put("operation", "write-headers");
				buffer.flip();
				ch.write(buffer, result, this);
			} else if ("write-ack".equals(context.get("operation"))) {
				context.put("operation", "read-body");
				buffer.flip();
				ch.read(buffer, result, this);
			} else if ("write-headers".equals(context.get("operation"))) {
				try {
					buffer.compact();
					if (buffer.position() > 0) {
						// partial write was done
						ch.write(buffer, result, this);
					} else {
						ch.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void reverse(ByteBuffer buffer) {
			int bufferPossition = buffer.position();
			for (int i = 0; i < bufferPossition / 2; i++) {
				byte toFlip = buffer.array()[i];
				buffer.array()[i] = buffer.array()[bufferPossition - i - 1];
				buffer.array()[bufferPossition - i - 1] = toFlip;
			}
		}

		public void failed(Throwable exc, Object attachment) {
			exc.printStackTrace();
		}

	}
}
