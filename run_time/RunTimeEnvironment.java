package run_time;

/*
 * Trygve IDE
 *   Copyright �2015 James O. Coplien
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 *  For further information about the trygve project, please contact
 *  Jim Coplien at jcoplien@gmail.com
 * 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import declarations.Declaration.ClassDeclaration;
import declarations.Type;
import declarations.Type.ClassType;
import error.ErrorLogger;
import error.ErrorLogger.ErrorType;
import run_time.RTClass.*;
import run_time.RTExpression.RTNullExpression;
import semantic_analysis.StaticScope;


public class RunTimeEnvironment {
	public static RunTimeEnvironment runTimeEnvironment_;
	public RunTimeEnvironment() {
		super();
		stringToRTContextMap_ = new HashMap<String, RTContext>();
		stringToRTClassMap_ = new HashMap<String, RTClass>();
		pathToTypeMap_ = new HashMap<String, RTType>();
		reboot();
		runTimeEnvironment_ = this;
		allClassList_ = new ArrayList<RTClass>();
		this.preDeclareTypes();
	}
	public void reboot() {
		stack = new Stack<RTStackable>();
		dynamicScopes = new Stack<RTDynamicScope>();
		framePointers_ = new Stack<IntWrapper>();
	}
	private void preDeclareTypes() {
		final StaticScope intScope = new StaticScope(StaticScope.globalScope());
		final ClassDeclaration intClassDecl = new ClassDeclaration("int", intScope, /*Base Class*/ null, 0);
		intScope.setDeclaration(intClassDecl);
		final Type intType = new ClassType("int", intScope, null);
		intClassDecl.setType(intType);
		
		final StaticScope int2Scope = new StaticScope(StaticScope.globalScope());
		final ClassDeclaration int2ClassDecl = new ClassDeclaration("Integer", intScope, /*Base Class*/ null, 0);
		intScope.setDeclaration(int2ClassDecl);
		final Type int2Type = new ClassType("Integer", int2Scope, null);
		intClassDecl.setType(int2Type);
		
		final StaticScope doubleScope = new StaticScope(StaticScope.globalScope());
		final ClassDeclaration doubleClassDecl = new ClassDeclaration("double", doubleScope, /*Base Class*/ null, 0);
		doubleScope.setDeclaration(intClassDecl);
		final Type doubleType = new ClassType("double", doubleScope, null);
		intClassDecl.setType(doubleType);
		
		final StaticScope stringScope = new StaticScope(StaticScope.globalScope());
		final ClassDeclaration stringClassDecl = new ClassDeclaration("String", stringScope, /*Base Class*/ null, 0);
		doubleScope.setDeclaration(stringClassDecl);
		final Type stringType = new ClassType("String", stringScope, null);
		intClassDecl.setType(stringType);
		
		final StaticScope booleanScope = new StaticScope(StaticScope.globalScope());
		final ClassDeclaration booleanClassDecl = new ClassDeclaration("boolean", booleanScope, /*Base Class*/ null, 0);
		doubleScope.setDeclaration(booleanClassDecl);
		final Type booleanType = new ClassType("boolean", booleanScope, null);
		intClassDecl.setType(booleanType);

		this.addTopLevelClass("int", new RTIntegerClass(intClassDecl));
		this.addTopLevelClass("Integer", new RTIntegerClass(int2ClassDecl));
		this.addTopLevelClass("double", new RTDoubleClass(doubleClassDecl));
		this.addTopLevelClass("String", new RTStringClass(stringClassDecl));
		this.addTopLevelClass("boolean", new RTBooleanClass(booleanClassDecl));
	}
	public void addTopLevelContext(String contextName, RTContext context) {
		stringToRTContextMap_.put(contextName, context);
	}
	public void addTopLevelClass(String className, RTClass aClass) {
		stringToRTClassMap_.put(className, aClass);
	}
	public RTContext topLevelContextNamed(String contextName) {
		return stringToRTContextMap_.get(contextName);
	}
	public RTClass topLevelClassNamed(String className) {
		final RTClass retval =  stringToRTClassMap_.get(className);
		return retval;
	}
	public RTType topLevelTypeNamed(String name) {
		RTType retval = this.topLevelContextNamed(name);
		if (null == retval) {
			retval = this.topLevelClassNamed(name);
		}
		return retval;
	}
	public void run(RTExpression mainExpr) {
		// Set up an activation record. We even need one for
		// main so it can declare t$his, if there's an
		// argument to the constructor
		handleMetaInits();
		
		final RTExpression exitNode = new RTNullExpression();
		mainExpr.setNextCode(exitNode);
		
		final RTDynamicScope firstActivationRecord = new RTDynamicScope("_main", null);
		globalDynamicScope = firstActivationRecord;
		RunTimeEnvironment.runTimeEnvironment_.pushDynamicScope(firstActivationRecord);
		
		// And go.
		RTCode pc = mainExpr;
		do {
			final RTCode oldPc = pc;
			pc = pc.run();
			if (null != pc) {
				pc.incrementReferenceCount();
			}
			oldPc.decrementReferenceCount();
		} while (pc != null && pc != exitNode);
	}
	public void setFramePointer() {
		final int stackSize = stack.size();
		framePointers_.push(new IntWrapper(stackSize));
	}
	private static class IntWrapper {
		public IntWrapper(int value) {
			value_ = value;
		}
		public int value() {
			return value_;
		}
		private int value_;
	}
	public RTStackable popDownToFramePointer() {
		RTStackable retval = null;
		final int stackSize = stack.size();
		int framePointer = (framePointers_.pop()).value();
		if (framePointer > stackSize) {
			ErrorLogger.error(ErrorType.Internal, 0, "Stack corruption: framePointer ", String.valueOf(framePointer), 
					" > stackSize ", String.valueOf(stackSize));
			assert false;
		}
		while (stack.size() > framePointer) {
			stack.pop();
		}
		retval = stack.peek();
		return retval;
	}
	public RTStackable popDownToFramePointerMinus1() {
		RTStackable retval = null;
		final int stackSize = stack.size();
		int framePointer = (framePointers_.pop()).value() + 1;
		if (framePointer < stackSize) {
			ErrorLogger.error(ErrorType.Internal, 0, "Stack corruption: framePointer ", String.valueOf(framePointer), 
					" > stackSize ", String.valueOf(stackSize));
			assert false;
		}
		while (stack.size() > framePointer) {
			stack.pop();
		}
		retval = stack.peek();
		return retval;
	}
	
	private void handleMetaInits() {
		for (RTClass aRunTimeClass : allClassList_) {
			aRunTimeClass.metaInit();
		}
	}
	public void addToListOfAllClasses(RTClass aClass) {
		if (allClassList_.contains(aClass)) {
			assert false;
		} else {
			allClassList_.add(aClass);
		}
	}
	
	public void pushStack(RTStackable stackable) {
		if (null != stackable) {
			// Can be null (e.g., the nextCode for end-of-evaluation at the end of the program)
			stackable.incrementReferenceCount();
		}
		stack.push(stackable);
	}
	public RTStackable popStack() {
		final RTStackable retval = stack.pop();
		return retval;
	}
	public RTStackable peekStack() { return stack.peek(); }
	public int stackSize() { return stack.size(); }
	
	public void pushDynamicScope(RTDynamicScope element) {
		// Subtle. Reference count was initialized to one and this is
		// its first use, so we don't increment
		// element.incrementReferenceCount();
		dynamicScopes.push(element);
	}
	public RTDynamicScope popDynamicScope() {
		final RTDynamicScope retval = dynamicScopes.pop();
		
		// We don't decrement the reference count here; that is handled
		// elsewhere (see, e.g., RTReturn>>run())
		return retval;
	}
	public RTDynamicScope currentDynamicScope() {
		return dynamicScopes.peek();
	}
	public void popDynamicScopeInstances(long depth) {
		// May be zero, but usually not
		for (int i = 0; i < depth; i++) {
			final RTDynamicScope retval = dynamicScopes.pop();
			retval.decrementReferenceCount();
		}
	}
	public void registerTypeByPath(String path, RTType rTType) {
		pathToTypeMap_.put(path, rTType);
	}
	public RTType typeFromPath(String path) {
		RTType retval;
		if (pathToTypeMap_.containsKey(path)) {
			retval = pathToTypeMap_.get(path);
		} else {
			retval = null;
		}
		return retval;
	}
	
	
	private Map<String, RTContext> stringToRTContextMap_;
	private Map<String, RTClass> stringToRTClassMap_;
	private Map<String, RTType> pathToTypeMap_;
	private Stack<RTStackable> stack;
	private Stack<IntWrapper> framePointers_;
	private Stack<RTDynamicScope> dynamicScopes;
	private List<RTClass> allClassList_;
	public RTDynamicScope globalDynamicScope;
}