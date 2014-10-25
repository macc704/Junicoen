package net.unicoen.interpreter;

public class StdLibLoader {

	private final Scope global;

	private StdLibLoader(Scope global) {
		this.global = global;
	}

	public static void initialize(Scope global) {
		StdLibLoader loader = new StdLibLoader(global);
		loader.initMyLib();
		loader.initTurtle();
	}

	private void initMyLib() {
		Scope scope = Scope.createObject(global);
		scope.setTop("printInt", new FunctionWithEngine() {
			@Override
			public Object invoke(Engine engine, Object[] args) {
				engine.out.println(args[0]);
				return null;
			}
		});
		scope.setTop("print", new FunctionWithEngine() {
			@Override
			public Object invoke(Engine engine, Object[] args) {
				engine.out.println(args[0]);
				return null;
			}
		});
		global.setTop("MyLib", scope);
	}

	private void initTurtle() {
		global.setTop("rt", new FunctionWithEngine() {
			@Override
			public Object invoke(Engine engine, Object[] args) {
				engine.out.print("R" + args[0]);
				return null;
			}
		});
		global.setTop("fd", new FunctionWithEngine() {
			@Override
			public Object invoke(Engine engine, Object[] args) {
				engine.out.print("F" + args[0]);
				return null;
			}
		});
	}
}
