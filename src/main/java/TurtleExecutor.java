
import java.util.function.Consumer;

public class TurtleExecutor {

	private static TurtleExecutor executor = null;

	private Consumer<Turtle> nextCommand = null;
	private final Object lock = new Object();
	private boolean finished;

	public class ProxyTurtle extends Turtle {
		@Override
		public void start() {
			while (!finished) {
				nextCommand.accept(this);
				// Inform the finish of the current command to the main thread
				synchronized (lock) {
					lock.notify();
				}
				// Wait for a next command
				synchronized (lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	public static void send(Consumer<Turtle> command) {
		if (executor == null) {
			executor = new TurtleExecutor();
		}
		executor.privateDoCommand(command);
	}

	public TurtleExecutor() {
		final ProxyTurtle proxyTurtle = new ProxyTurtle();
		new Thread() {
			@Override
			public void run() {
				Turtle.startTurtle(proxyTurtle);
			}
		}.start();
	}

	private void privateDoCommand(Consumer<Turtle> command) {
		nextCommand = command;
		// Inform the receive of a next command to the turtle thread
		synchronized (lock) {
			lock.notify();
		}
		// Wait for the turtle's move
		try {
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
		}
		nextCommand = null;
	}

	public static void finish() {
		executor.finished = true;
		// Inform the termination to the turtle thread
		synchronized (executor.lock) {
			executor.lock.notify();
		}
		executor = null;
	}
}