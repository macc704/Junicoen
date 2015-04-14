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
				synchronized (lock) {
					lock.notify();
				}
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
		synchronized (lock) {
			lock.notify();
		}
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
		synchronized (executor.lock) {
			executor.lock.notify();
		}
		executor = null;
	}
}