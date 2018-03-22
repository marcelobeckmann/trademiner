package weka.agent;

import java.io.IOException;

class OutputReader extends Thread {
	Process process;
	StringBuilder output = new StringBuilder();
	StringBuilder err = new StringBuilder();

	Thread thread;

	public OutputReader(Process process) {
		this.process = process;

		thread = new Thread(this);

		thread.start();
	}

	public void run() {

		final int BLOCKSIZE=64;
		byte[] bytes = new byte[BLOCKSIZE];

		try {
			while (!thread.interrupted()) {
				bytes = new byte[BLOCKSIZE];
				process.getInputStream().read(bytes);
				output.append(new String(bytes));
				
				bytes = new byte[BLOCKSIZE];
				process.getErrorStream().read(bytes);
				err.append(new String(bytes));
				this.sleep(5);
			
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void interrupt() {
		thread.interrupt();

	}

}