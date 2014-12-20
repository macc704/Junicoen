package net.unicoen.interpreter;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.unicoen.node.UniBinOp;
import net.unicoen.node.UniBlock;
import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniBreak;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniCondOp;
import net.unicoen.node.UniContinue;
import net.unicoen.node.UniDecVar;
import net.unicoen.node.UniDecVarWithValue;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniFor;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIf;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMemberDec;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniNode;
import net.unicoen.node.UniReturn;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniUnaryOp;
import net.unicoen.node.UniWhile;

public class Engine {

	@SuppressWarnings("serial")
	private static class ControlException extends RuntimeException {
	}

	@SuppressWarnings("serial")
	private static class Break extends ControlException {
	}

	@SuppressWarnings("serial")
	private static class Continue extends ControlException {
	}

	@SuppressWarnings("serial")
	private static class Return extends ControlException {
		public final Object value;

		public Return(Object value) {
			this.value = value;
		}
	}

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
		try {
			return execBlock(fdec.block, funcScope);
		} catch (Return e) {
			return e.value;
		}
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
		if (expr instanceof UniUnaryOp) {
			return execUnaryOp((UniUnaryOp) expr, scope);
		}
		if (expr instanceof UniBinOp) {
			return execBinOp((UniBinOp) expr, scope);
		}
		if (expr instanceof UniCondOp) {
			UniCondOp condOp = (UniCondOp) expr;
			return toBool(execExpr(condOp.cond, scope)) ? execExpr(condOp.trueExpr, scope) : execExpr(condOp.falseExpr,
					scope);
		}
		if (expr instanceof UniBreak) {
			throw new Break();
		}
		if (expr instanceof UniContinue) {
			throw new Continue();
		}
		if (expr instanceof UniReturn) {
			UniReturn uniRet = (UniReturn) expr;
			Object retValue = execExpr(uniRet, scope);
			throw new Return(retValue);
		}
		if (expr instanceof UniDecVar) {
			UniDecVar decVar = (UniDecVar) expr;
			scope.setTop(decVar.name, null);
			return null;
		}
		if (expr instanceof UniDecVarWithValue) {
			UniDecVarWithValue decVar = (UniDecVarWithValue) expr;
			Object value = execExpr(decVar, scope);
			scope.setTop(decVar.name, value);
			return value;
		}
		if (expr instanceof UniIf) {
			UniIf ui = (UniIf) expr;
			if (toBool(execExpr(ui.cond, scope))) {
				return execBlock(ui.trueBlock, scope);
			} else {
				return execBlock(ui.falseBlock, scope);
			}
		}
		if (expr instanceof UniFor) {
			UniFor uniFor = (UniFor) expr;
			Scope forScope = Scope.createLocal(scope);
			try {
				Object lastEval = null;
				for (execExpr(uniFor.init, forScope); toBool(execExpr(uniFor.cond, forScope)); execExpr(uniFor.step,
						forScope)) {
					try {
						lastEval = execBlock(uniFor.block, forScope);
					} catch (Continue e) { /* do nothing*/
					}
				}
				return lastEval;
			} catch (Break e) {
				return null;
			}
		}
		if (expr instanceof UniWhile) {
			UniWhile uniWhile = (UniWhile) expr;
			try {
				Object lastEval = null;
				while (toBool(execExpr(uniWhile.cond, scope))) {
					try {
						lastEval = execBlock(uniWhile.block, scope);
					} catch (Continue e) { /* do nothing*/
					}
				}
				return lastEval;
			} catch (Break e) {
				return null;
			}
		}
		throw new RuntimeException("Not support expr type: " + expr);
	}

	private Object execMethodCall(Object receiver, String methodName, Object[] args) {
		assert receiver != null;

		String msg = String.format("Method not found: %s.%s", receiver.getClass().getName(), methodName);
		if (receiver instanceof Scope) {
			Object func = ((Scope) receiver).get(methodName);
			return execFuncCall(func, args);
		} else if (receiver instanceof Class<?>) {
			Predicate<Method> isStatic = m -> (m.getModifiers() | Modifier.STATIC) != 0;
			Method method = findMethod((Class<?>) receiver, methodName, args, isStatic);
			if (method == null) {
				throw new RuntimeException(msg);
			}
			try {
				return method.invoke(null, args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(msg, e);
			}
		} else {
			Method method = findMethod(receiver.getClass(), methodName, args, m -> true);
			if (method == null) {
				throw new RuntimeException(msg);
			}
			try {
				return method.invoke(receiver, args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(msg, e);
			}
		}
	}

	private Object execBlock(UniBlock block, Scope scope) {
		Object lastValue = null;
		for (UniExpr expr : block.body) {
			lastValue = execExpr(expr, scope);
		}
		return lastValue;
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

	private Object execUnaryOp(UniUnaryOp uniOp, Scope scope) {
		switch (uniOp.operator) {
		case "-":
			return -toInt(execExpr(uniOp.expr, scope));
		case "!":
			return !toBool(execExpr(uniOp.expr, scope));
		}
		throw new RuntimeException("Unkown binary operator: " + uniOp.operator);
	}

	private Object execBinOp(UniBinOp binOp, Scope scope) {
		String op = binOp.operator;
		UniExpr left = binOp.left;
		UniExpr right = binOp.right;
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

	private static Method findMethod(Class<?> clazz, String methodName, Object[] args, Predicate<Method> cond) {
		for (Method m : clazz.getMethods()) {
			if (methodName.equals(m.getName()) == false) {
				continue;
			}
			if (cond != null && cond.test(m) == false) {
				continue;
			}
			if ((m.getModifiers() | Modifier.STATIC) == 0) {
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
