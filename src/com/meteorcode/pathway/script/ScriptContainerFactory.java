package com.meteorcode.pathway.script;

import java.io.IOException;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.InterpreterError;

import com.meteorcode.pathway.io.FileHandle;

/**
 * The means by which one obtains a ScriptContainer.
 *
 * @author xyzzy
 *
 */
public class ScriptContainerFactory {

	/** used only for testing */
	private Interpreter replacementInterpreter;

	/**
	 * ScriptContainer for Beanshell scripts.
	 *
	 * @author Hawk Weisman
	 * @version 0.0.1
	 * @since May 17th, 2014
	 */
	private class BeanshellScriptContainer extends ScriptContainer {

		Interpreter i;
		public final String[] javaKeywords = { "abstract", "assert", "boolean",
				"break", "byte", "case", "catch", "char", "class", "const",
				"continue", "do", "double", "else", "enum", "extends", "final",
				"finally", "float", "for", "if", "goto", "implements",
				"import", "instanceof", "int", "interface", "long", "native",
				"new", "package", "private", "protected", "public", "return",
				"short", "static", "strictfp", "super", "switch",
				"synchronized", "this", "throw", "throws", "transient", "try",
				"void", "volatile", "while" };

		/**
		 * Init a new Beanshell ScriptContainer with the default environment.
		 */
		public BeanshellScriptContainer() {
			super();
			i = new Interpreter();
		}

		/**
		 * Init an new Beanshell ScriptContainer and link in a
		 * ScriptEnvironment.
		 *
		 * @param environment
		 *            the ScriptEnvironment to link in.
		 * @throws ScriptException
		 *             if an error occurs during linking.
		 */
		public BeanshellScriptContainer(ScriptEnvironment environment)
				throws ScriptException {
			super();
			i = new Interpreter();
			environment.link(this);
		}

		/**
		 * Evaluates a literal expression or multiple expressions and returns
		 * the possible result.
		 * <p>
		 * If there is no return, you get back null instead of an object.
		 * </p>
		 * <p>
		 * If an exception is thrown from the scripts, it is re-wrapped as a
		 * generic ScriptException, with the exception's cause being the actual
		 * exception thrown by the script.
		 * </p>
		 *
		 * @param script
		 *            The string of literal text to interpret as a script.
		 * @return The object result of the evaluation, or null if there was no
		 *         result.
		 * @throws ScriptException
		 *             if an error takes place during script execution. The
		 *             ScriptException wraps the native exceptions thrown by
		 *             Beanshell.
		 * @see com.meteorcode.pathway.script.ScriptContainer#eval(String)
		 */
		@Override
		public Object eval(String script) throws ScriptException {
			try {
				return i.eval(script);
			} catch (EvalError e) {
				throw new ScriptException("Script evaluation caused EvalError",
						e);
			} catch (InterpreterError e) {
				throw new ScriptException(
						"Script evaluation caused InterpreterError", e);
			} catch (NullPointerException e) {
				throw new ScriptException("Null is not a valid script.", e);
			}
		}

		/**
		 * Evaluates a BeanShell script stored in a file.
		 * <p>
		 * If there is no return, you get back null instead of an object.
		 * </p>
		 * <p>
		 * If an exception is thrown from the scripts, it is re-wrapped as a
		 * generic ScriptException, with the exception's cause being the actual
		 * exception thrown by the script.
		 * </p>
		 *
		 * @param file
		 *            a {@link com.meteorcode.pathway.io.FileHandle FileHandle}
		 *            containing the script to execute.
		 * @return The object result of the evaluation, or null if there was no
		 *         result.
		 * @throws ScriptException
		 *             if an error takes place during script execution. The
		 *             ScriptException wraps the native exceptions thrown by
		 *             Beanshell.
		 * @throws IOException
		 * 				if an error takes place while accessing the FileHandle.
		 * @see com.meteorcode.pathway.script.ScriptContainer#eval(FileHandle)
		 */
		@Override
		public Object eval(FileHandle file) throws ScriptException, IOException {
			try {
				String script = file.readString();
				return i.eval(script);
			} catch (EvalError e) {
				throw new ScriptException(
						"Script evaluation from file caused EvalError", e);
			} catch (InterpreterError e) {
				throw new ScriptException(
						"Script evaluation from file caused InterpreterError",
						e);
			} /*catch (IOException e) {
				throw new ScriptException("Could not open script file", e);
			}*/
		}

		/**
		 * Put data into the scripting environment globally.
		 *
		 * @param scriptName
		 *            The name of the variable exposed to scripts
		 * @param object
		 *            The object to bind to this variable name
		 * @throws ScriptException
		 *             if the injection attempt fails.
		 * @see
		 *      {com.meteorcode.spaceshipgame.script.ScriptContainer#injectObject
		 *      (String , Object)
		 * @see {bsh.Interpreter#set(String, Object)}
		 */
		@Override
		public void injectObject(String scriptName, Object object)
				throws ScriptException {
			try {
				i.set(scriptName, object);
			} catch (EvalError e) {
				throw new ScriptException("Error injecting " + scriptName
						+ " into Beanshell", e);
			}
		}

		/**
		 * Remove a variable put into the script environment.
		 *
		 * @param scriptName
		 *            The name of the variable exposed to scripts
		 * @throws ScriptException
		 *             if the removal attempt fails.
		 * @see
		 *      {com.meteorcode.spaceshipgame.script.ScriptContainer#removeObject
		 *      (String , Object)
		 * @see {bsh.Interpreter#unset(String, Object)}
		 */
		@Override
		public void removeObject(String scriptName) throws ScriptException {
			try {
				i.unset(scriptName);
			} catch (EvalError e) {
				throw new ScriptException("Error unbinding " + scriptName
						+ " from Beanshell", e);
			}
		}

		/**
		 * <p>
		 * Accesses the contents of a variable in the script environment.
		 * </p>
		 *
		 * @param variable
		 *            the name of the script variable to access
		 * @return the contents of the variable if it exists, null if the
		 *         variable does not exist.
		 * @throws ScriptException
		 *             if the script container cannot access the variable
		 * @throws IllegalArgumentException
		 *             if the variable name to access is an invalid token or an
		 */
		@Override
		public Object access(String variable) throws ScriptException,
				IllegalArgumentException {

			// check if the requested variable name is a Java reserved word
			for (String keyword : javaKeywords)
				if (keyword.equals(variable))
					// variable names cannot be Java reserved words.
					throw new IllegalArgumentException(
							"Variable name cannot be a Java reserved word.");

			// check if the requested variable is a valid Java identifier
			char[] letters = variable.toCharArray();

			if (Character.isJavaIdentifierStart(letters[0]) == false)
				throw new IllegalArgumentException(
						"Variable name was not a valid Java identifier; illegal character at position 0");

			for (int i = 0; i < letters.length; i++)
				if (Character.isJavaIdentifierPart(letters[i]) == false)
					throw new IllegalArgumentException(
							"Variable name was not a valid Java identifier; illegal character at position "
									+ i);
			// return the result of evaluating the variable name against the
			// BeanshellScriptContainer's own eval method; this does not require
			// catching EvalErrors and throwing new ScriptExceptions, as
			// ScriptContainer.eval() already wraps Beanshell's errors
			return eval(variable);
		}
	}

	public ScriptContainerFactory() {
		this.replacementInterpreter = null;
	}

	public ScriptContainerFactory(Interpreter replacement) {
		this.replacementInterpreter = replacement;
	}

	public ScriptContainer getNewInstance() {
		if (this.replacementInterpreter == null) {
			return new BeanshellScriptContainer();
		} else {
			BeanshellScriptContainer s = new BeanshellScriptContainer();
			s.i = this.replacementInterpreter;
			return s;
		}
	}

	public ScriptContainer getNewInstanceWithEnvironment(
			ScriptEnvironment environment) throws ScriptException {
		if (this.replacementInterpreter == null) {
			return new BeanshellScriptContainer(environment);
		} else {
			BeanshellScriptContainer s = new BeanshellScriptContainer(
					environment);
			s.i = this.replacementInterpreter;
			return s;
		}
	}
}
