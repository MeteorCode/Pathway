package com.meteorcode.pathway.script;

import java.io.IOException;

import com.meteorcode.pathway.io.FileHandle;

/**
 * A fully formed evaluator for a scripting language, complete with environment
 * and evaluator. For all intents and purposes, this is the "top level" of a
 * script's scope; things evaluated inside a Container evaluate at the
 * **GLOBAL** level inside this container. That can be good and it can be bad.
 * Either way, that's how this design is. In order to separate scripts' scopes,
 * it should be necessary to create multiple script containers.
 *
 * @author xyzzy
 *
 */
public abstract class ScriptContainer {

	/**
	 * Init the script container with the default script environment.
	 */
	public ScriptContainer() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Init an new ScriptContainer and link in a ScriptEnvironment.
	 *
	 * @param environment
	 *            the ScriptEnvironment to link in.
	 * @throws ScriptException
	 *             if an error occurs during linking.
	 */
	public ScriptContainer(ScriptEnvironment environment)
			throws ScriptException {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Evaluates a script stored in a file.
	 * <p>
	 * If there is no return, you get back null instead of an object.
	 * </p>
	 * <p>
	 * If an exception is thrown from the scripts, it is re-wrapped as a generic
	 * ScriptException, with the exception's cause being the actual exception
	 * thrown by the script.
	 * </p>
	 *
	 * @param file
	 *            a {@link com.meteorcode.pathway.io.java_api.FileHandle}
	 *            containing the script to execute.
	 * @return The object result of the evaluation, or null if there was no
	 *         result.
	 * @throws ScriptException
	 *             if an error takes place during script execution.
	 * @throws IOException
	 * 				if an error takes place while accessing the script's file.
	 */
	public abstract Object eval(FileHandle file) throws ScriptException, IOException;

	/**
	 * Evaluates a literal expression or multiple expressions and returns the
	 * possible result.
	 * <p>
	 * If there is no return, you get back null instead of an object.
	 * </p>
	 * <p>
	 * If an exception is thrown from the scripts, it is re-wrapped as a generic
	 * ScriptException, with the exception's cause being the actual exception
	 * thrown by the script.
	 * </p>
	 *
	 * @param script
	 *            The string of literal text to interpret as a script.
	 * @return The object result of the evaluation, or null if there was no
	 *         result.
	 * @throws ScriptException
	 *             if an error takes place during script execution.
	 */
	public abstract Object eval(String script) throws ScriptException;

	/**
	 * Put data into the scripting environment globally.
	 *
	 * @param scriptName
	 *            The name of the variable exposed to scripts
	 * @param object
	 *            The object to bind to this variable name
	 * @throws ScriptException
	 *             if the injection attempt fails.
	 */
	public abstract void injectObject(String scriptName, Object object)
			throws ScriptException;

	/**
	 * Removes an object from the scripting environment.
	 * <p>
	 * The variable name of the unbound object will now test `[name] == void`.
	 * </p>
	 *
	 * @param scriptName
	 *            the name of the variable to unbind
	 * @throws ScriptException
	 */
	public abstract void removeObject(String scriptName) throws ScriptException;

	/**
	 * <p>
	 * Accesses the contents of a variable in the script environment.
	 * </p>
	 *
	 * @param variable
	 *            the name of the script variable to access
	 * @return the contents of the variable if it exists, null if the variable
	 *         does not exist.
	 * @throws ScriptException
	 *             if the script container cannot access the variable
	 * @throws IllegalArgumentException
	 *             if the variable name to access is an invalid token or an
	 *             expression.
	 */
	public abstract Object access(String variable) throws ScriptException,
			IllegalArgumentException;
}
