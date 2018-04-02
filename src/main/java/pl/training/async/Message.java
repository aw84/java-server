package pl.training.async;

import java.util.Map;

public class Message {
	private long contentLength;
	private String transferEncoding;
	private byte[] body;

	public Message() {
		contentLength = -1;
		body = null;
	}

	public static Message build(Map<String, byte[]> props) {
		Message m = new Message();
		for (Map.Entry<String, byte[]> p : props.entrySet()) {
			if ("cl".equals(p.getKey())) {
				m.setContentLength(Long.parseLong(new String(p.getValue())));
			} else if ("te".equals(p.getKey())) {
				m.setTransferEncoding(new String(p.getValue()));
			} else if ("request-data".equals(p.getKey())) {
				m.setBody(p.getValue());
			}
		}
		return m;
	}

	public long getContentLength() {
		return contentLength;
	}

	private void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}

	public String getTransferEncoding() {
		return transferEncoding;
	}

	private void setTransferEncoding(String transferEncoding) {
		this.transferEncoding = transferEncoding;
	}

	public byte[] getBody() {
		return body;
	}

	private void setBody(byte[] body) {
		this.body = body;
	}

	public long getBodyLength() {
		if (getBody() != null) {
			return getBody().length;
		} else {
			return 0;
		}
	}
}