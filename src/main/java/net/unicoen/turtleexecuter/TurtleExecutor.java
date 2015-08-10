package net.unicoen.turtleexecuter;

import java.util.function.Consumer;

import obpro.turtle.Turtle;
import clib.common.thread.CThread;

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
				System.out.println("accept" + nextCommand);
				// Inform the finish of the current command to the main thread
				synchronized (lock) {
					System.out.println("notify from proxy");
					lock.notify();
				}
				// Wait for a next command
				synchronized (lock) {
					try {
						System.out.println("wait proxy");
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
			public void run() {
				System.out.println("start Turtle");
				Turtle.startTurtle(proxyTurtle, new String[0]);
				System.out.println("started Turtle");
			}
		}.start();
	}

	private void privateDoCommand(Consumer<Turtle> command) {
		nextCommand = command;
		System.out.println("do " + command);
		// Inform the receive of a next command to the turtle thread
		synchronized (lock) {
			System.out.println("notify");
			lock.notify();
		}
		// Wait for the turtle's move
		try {
			synchronized (lock) {
				System.out.println("wait");
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