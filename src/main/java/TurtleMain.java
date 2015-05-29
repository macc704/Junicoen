
import java.io.File;
import java.io.UnsupportedEncodingException;

import net.unicoen.interpreter.Engine;
import net.unicoen.interpreter.ExecutionListener;
import net.unicoen.interpreter.FunctionWithEngine;
import net.unicoen.interpreter.Scope;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniNode;
import net.unicoen.parser.blockeditor.ToBlockEditorParser;

public class TurtleMain {
	public static void main(String[] args) throws UnsupportedEncodingException {
		String file = "Turtles.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);

		UniClassDec dec = ToBlockEditorParser.parse(targetXml);

		Engine engine = new Engine();
		engine.addListener(libOverrider);
		engine.execute(dec);
	}

	private static ExecutionListener libOverrider = new ExecutionListener() {
		@Override
		public void preExecuteAll(Scope global) {
			global.setTop("rt", new FunctionWithEngine() {
				@Override
				public Object invoke(Engine engine, Object[] args) {
					double angle = ((Number) args[0]).doubleValue();
					TurtleExecutor.send(t -> t.rt(angle));
					return null;
				}
			});
			global.setTop("lt", new FunctionWithEngine() {
				@Override
				public Object invoke(Engine engine, Object[] args) {
					double angle = ((Number) args[0]).doubleValue();
					TurtleExecutor.send(t -> t.lt(angle));
					return null;
				}
			});
			global.setTop("fd", new FunctionWithEngine() {
				@Override
				public Object invoke(Engine engine, Object[] args) {
					double length = ((Number) args[0]).doubleValue();
					TurtleExecutor.send(t -> t.fd(length));
					return null;
				}
			});
			global.setTop("bk", new FunctionWithEngine() {
				@Override
				public Object invoke(Engine engine, Object[] args) {
					double length = ((Number) args[0]).doubleValue();
					TurtleExecutor.send(t -> t.bk(length));
					return null;
				}
			});
		}

		@Override
		public void preExecute(UniNode node, Scope scope) {
		}

		@Override
		public void postExecuteAll(Scope global) {
		}

		@Override
		public void postExecute(UniNode node, Scope scope, Object value) {
		}
	};
}
