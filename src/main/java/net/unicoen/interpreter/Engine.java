package net.unicoen.interpreter;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBinOp;
import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIf;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMemberDec;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniNode;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniWhile;

public class Engine {

	public PrintStream out = System.out;
	public List<ExecutionListener> listeners;

	public void addListener(ExecutionListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<>();
		}
		listeners.add(listener);
	}

	private void firePreExecAll(Scope global) {
		if (listeners != null) {
			for (ExecutionListener listener : listeners) {
				listener.preExecuteAll(global);
			}
		}
	}

	private void firePostExecAll(Scope global, Object value) {
		if (listeners != null) {
			for (ExecutionListener listener : listeners) {
				listener.postExecuteAll(global);
			}
		}
	}

	private void firePreExec(UniNode node, Scope scope) {
		if (listeners != null) {
			for (ExecutionListener listener : listeners) {
				listener.preExecute(node, scope);
			}
		}
	}

	private void firePostExec(UniNode node, Scope scope, Object value) {
		if (listeners != null) {
			for (ExecutionListener listener : listeners) {
				listener.postExecute(node, scope, value);
			}
		}
	}

	public static Object executeSimple(UniExpr expr, String key, Object value) {
		Engine engine = new Engine();
		Scope scope = Scope.createGlobal();
		scope.setTop(key, value);
		return engine.execExpr(expr, scope);
	}

	public Object execute(UniClassDec dec) {
		UniFuncDec fdec = getEntryPoint(dec);
		if (fdec != null) {
			Scope global = Scope.createGlobal();
			StdLibLoader.initialize(global);
			firePreExecAll(global);
			Object value = execFunc(fdec, global);
			firePostExecAll(global, value);
			return value;
		} else {
			throw new RuntimeException("No entry point in " + dec);
		}
	}

	private UniFuncDec getEntryPoint(UniClassDec dec) {
		for (UniMemberDec m : dec.members) {
			if (m instanceof UniFuncDec) {
				UniFuncDec fdec = (UniFuncDec) m;
				if ("start".equals(fdec.funcName)) {
					return fdec;
				}
			}
		}
		return null;
	}

	private Object execFunc(UniFuncDec fdec, Scope global) {
		Scope funcScope = Scope.createLocal(global);
		// TODO: set argument to func scope
		return execExprMany(fdec.body, funcScope);
	}

	private Object execExprMany(List<UniExpr> exprs, Scope scope) {
		Object lastValue = null;
		for (UniExpr expr : exprs) {
			lastValue = execExpr(expr, scope);
		}
		return lastValue;
	}

	public Object runCallMethod(Object instance, String methodName, Object[] args) {
		throw new RuntimeException("Not support call method for: " + instance);
	}

	private Object execExpr(UniExpr expr, Scope scope) {
		firePreExec(expr, scope);
		Object value = _execExpr(expr, scope);
		firePostExec(expr, scope, value);
		return value;
	}

	private Object _execExpr(UniExpr expr, Scope scope) {
		assert expr != null;

		if (expr instanceof UniMethodCall) {
			UniMethodCall mc = (UniMethodCall) expr;

			Object[] args = new Object[mc.args == null ? 0 : mc.args.size()];
			for (int i = 0; i < args.length; i++) {
				args[i] = execExpr(mc.args.get(i), scope);
			}
			if (mc.receiver != null) {
				Object receiver = execExpr(mc.receiver, scope);
				return execMethodCall(receiver, mc.methodName, args);
			} else {
				Object func = scope.get(mc.methodName);
				return execFuncCall(func, args);
			}
		}
		if (expr instanceof UniIdent) {
			return scope.get(((UniIdent) expr).name);
		}
		if (expr instanceof UniIntLiteral) {
			return ((UniIntLiteral) expr).value;
		}
		if (expr instanceof UniStringLiteral) {
			return ((UniStringLiteral) expr).value;
		}
		if (expr instanceof UniBoolLiteral) {
			return ((UniBoolLiteral) expr).value;
		}
		if (expr instanceof UniBinOp) {
			UniBinOp bin = (UniBinOp) expr;
			return execBinOp(bin.operator, bin.left, bin.right, scope);
		}
		if (expr instanceof UniIf) {
			UniIf ui = (UniIf) expr;
			if (toBool(execExpr(ui.cond, scope))) {
				return execExprMany(ui.trueBlock, Scope.createLocal(scope));
			} else {
				return execExprMany(ui.falseBlock, Scope.createLocal(scope));
			}
		}
		if (expr instanceof UniWhile) {
			UniWhile w = (UniWhile) expr;
			Object lastEval = null;
			while (toBool(execExpr(w.cond, scope))) {
				lastEval = execExprMany(w.body, Scope.createLocal(scope));
			}
			return lastEval;
		}
		throw new RuntimeException("Not support expr type: " + expr);
	}

	private Object execMethodCall(Object receiver, String methodName, Object[] args) {
		assert receiver != null;

		if (receiver instanceof Scope) {
			Object func = ((Scope) receiver).get(methodName);
			return execFuncCall(func, args);
		} else {
			Method m = findMethod(receiver, methodName, args);
			String msg = String.format("Method not found: %s.%s", receiver.getClass().getName(), methodName);
			if (m == null) {
				throw new RuntimeException(msg);
			}
			try {
				return m.invoke(receiver, args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(msg, e);
			}
		}
	}

	private Object execFuncCall(Object func, Object[] args) {
		assert func != null;

		if (func instanceof FunctionWithEngine) {
			return ((FunctionWithEngine) func).invoke(this, args);
		} else {
			Method m = findFunctionMethod(func.getClass());
			if (m != null) {
				try {
					return m.invoke(func, args);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException("Fail to invoke", e);
				}
			}
		}
		throw new RuntimeException("Not support function type: " + func);
	}

	private Object execBinOp(String op, UniExpr left, UniExpr right, Scope scope) {
		switch (op) {
		case "+":
			return toInt(execExpr(left, scope)) + toInt(execExpr(right, scope));
		case "-":
			return toInt(execExpr(left, scope)) - toInt(execExpr(right, scope));
		case "*":
			return toInt(execExpr(left, scope)) * toInt(execExpr(right, scope));
		case "/":
			return toInt(execExpr(left, scope)) / toInt(execExpr(right, scope));
		case "%":
			return toInt(execExpr(left, scope)) % toInt(execExpr(right, scope));

		case "&&":
			return toBool(execExpr(left, scope)) && toBool(execExpr(right, scope));
		case "||":
			return toBool(execExpr(left, scope)) || toBool(execExpr(right, scope));
		}
		throw new RuntimeException("Unkown binary operator: " + op);
	}

	public static int toInt(Object obj) {
		if (obj instanceof Integer) {
			return ((Integer) obj).intValue();
		}
		throw new RuntimeException("Cannot covert to integer: " + obj);
	}

	public static boolean toBool(Object obj) {
		if (obj instanceof Boolean) {
			return ((Boolean) obj).booleanValue();
		}
		throw new RuntimeException("Cannot covert to boolean: " + obj);
	}

	private static Method findMethod(Object receiver, String methodName, Object[] args) {
		Class<?> clazz = receiver.getClass();
		for (Method m : clazz.getMethods()) {
			if (methodName.equals(m.getName()) == false) {
				continue;
			}
			Class<?>[] argTypes = m.getParameterTypes();
			if (argTypes.length != args.length) {
				continue;
			}
			for (int i = 0; i < argTypes.length; i++) {
				Object obj = args[i];
				Class<?> argType = argTypes[i];
				if (argType.isPrimitive()) {
					argType = getBoxType(argType);
				}
				boolean isOK = (obj == null || argType.isAssignableFrom(obj.getClass()));
				if (!isOK) {
					continue;
				}
			}
			return m;
		}
		return null;
	}

	private static Method findFunctionMethod(final Class<?> clazz) {
		boolean isFunction = false;
		Class<?> funcClazz = clazz;
		find: while (funcClazz != null && Object.class != funcClazz) {
			for (Annotation an : funcClazz.getAnnotations()) {
				if (an instanceof FunctionalInterface) {
					isFunction = true;
					break find;
				}
			}
			for (Class<?> ic : funcClazz.getInterfaces()) {
				for (Annotation an : ic.getAnnotations()) {
					if (an instanceof FunctionalInterface) {
						funcClazz = ic;
						isFunction = true;
						break find;
					}
				}
			}
		}
		if (isFunction == false) {
			return null;
		}
		Method ret = null;
		for (Method m : funcClazz.getMethods()) {
			if ((m.getModifiers() & Modifier.ABSTRACT) == 0) {
				// Its not abstract.
				continue;
			}
			if (ret != null) {
				throw new RuntimeException(String.format("Ambiguous: %s or %s", ret, m));
			}
			ret = m;
		}
		if (ret == null) {
			throw new RuntimeException("Method not found.");
		}
		return ret;
	}

	private static Class<?> getBoxType(Class<?> clazz) {
		assert clazz.isPrimitive();
		if (clazz == int.class) {
			return Integer.class;
		}
		if (clazz == boolean.class) {
			return Boolean.class;
		}
		if (clazz == double.class) {
			return Double.class;
		}
		throw new RuntimeException("Not supported primitive type: " + clazz);
	}
}
