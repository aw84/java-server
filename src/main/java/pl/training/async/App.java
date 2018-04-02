package pl.training.async;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		try {
			new EchoServer("localhost", 9898);
			try {
				System.out.println("Thread sleep");
				Thread.sleep(1000*1000*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
