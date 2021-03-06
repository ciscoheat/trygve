package info.fulloo.trygve.declarations;

/*
 * Trygve IDE
 *   Copyright (c)2015 James O. Coplien
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

import info.fulloo.trygve.declarations.Declaration.ObjectDeclaration;
import info.fulloo.trygve.declarations.Type.TemplateParameterType;
import info.fulloo.trygve.declarations.Type.TemplateType;
import info.fulloo.trygve.mylibrary.SimpleList;


public class FormalParameterList extends ParameterListCommon implements ActualOrFormalParameterList {
	public FormalParameterList() {
		super(new SimpleList());
	}
	public void addFormalParameter(final Declaration parameter) {
		insertAtStart(parameter);
	}
	public ObjectDeclaration parameterAtPosition(final int i) {
		final Object backFromParameterAtIndex = parameterAtIndex(i);
		ObjectDeclaration retval = null;
		if (null != backFromParameterAtIndex) {
			retval = (ObjectDeclaration) backFromParameterAtIndex;
		}
		return retval;
	}
	public boolean alignsWith(final ActualOrFormalParameterList pl) {
		return FormalParameterList.alignsWithParameterListIgnoringParam(this, pl, null, false);
	}
	public boolean alignsWithUsingConversion(final ActualOrFormalParameterList pl) {
		return FormalParameterList.alignsWithParameterListIgnoringParam(this, pl, null, true);
	}
	public static boolean alignsWithParameterListIgnoringParam(final ActualOrFormalParameterList pl1,
			final ActualOrFormalParameterList pl2, final String paramToIgnore, final boolean conversionAllowed) {
		boolean retval = true;
		final int myCount = pl1.count();
		if (null == pl2) {
			if (myCount != 0) {
				retval = false;
			} else {
				// Redundant, but clear
				retval = true;
			}
		} else {
			final int plCount = pl2.count();
			if (plCount != myCount) {
				retval = false;
			} else {
				for (int i = 0; i < plCount; i++) {
					final String pl1Name = pl1.nameOfParameterAtPosition(i),
							     pl2Name = pl2.nameOfParameterAtPosition(i);
					if (null != pl2Name && null != paramToIgnore && pl2Name.equals(paramToIgnore)) {
						continue;
					}
					
					// We really should be a bit more dutiful about knowing whether it's l1 or
					// pl2 we're checking. But it's almost always "this" and since it's a
					// reserved word, it won't be aliased with a user variable
					if (null != pl1Name && null != paramToIgnore && pl1Name.equals(paramToIgnore)) {
						continue;
					}
					
					final Type plt = pl2.typeOfParameterAtPosition(i);
					final Type myt = pl1.typeOfParameterAtPosition(i);
					if (plt == null || null == myt) {
						retval = false;
					} else if (plt.enclosedScope() == myt.enclosedScope()) {
						continue;
					} else if (plt.isBaseClassOf(myt)) {
						continue;
					} else if (conversionAllowed) {
						retval = myt.canBeConvertedFrom(plt);
					} else {
						retval = false;
						break;
					}
				}
			}
		}
		return retval;
	}
	public static boolean alignsWithParameterListIgnoringRoleStuff(final ActualOrFormalParameterList pl1, final ActualOrFormalParameterList pl2) {
		boolean retval = true;
		final int pl1Count = pl1.count();
		if (null == pl2) {
			if (pl1Count != 0) {
				retval = false;
			} else {
				// Redundant, but clear
				retval = true;
			}
		} else {
			final int pl2Count = pl2.count();
			int i = 0, j = 0;
			while (i < pl1Count && j < pl2Count) {
				boolean testFlag = true;
				final String pl1Name = pl1.nameOfParameterAtPosition(i),
						     pl2Name = pl2.nameOfParameterAtPosition(j);
				
				if (pl1Name.equals("this") || pl1Name.equals("t$this") ||
						pl1Name.equals("current$context") || pl1Name.equals("current$role")) {
					i++;
					testFlag = false;
				}
				if (pl2Name.equals("this") || pl2Name.equals("t$this") ||
						pl2Name.equals("current$context") || pl2Name.equals("current$role")) {
					j++;
					testFlag = false;
				}
				if (testFlag){
					final Type plt = pl2.typeOfParameterAtPosition(j);
					final Type myt = pl1.typeOfParameterAtPosition(i);
					
					if (plt.enclosedScope() == myt.enclosedScope()) {
						i++; j++;
					} else if (plt.isBaseClassOf(myt)) {
						i++; j++;
					} else {
						retval = false;
						break;
					}
					i++;
				}
			}
		}
		return retval;
	}
	
	@Override public Type typeOfParameterAtPosition(final int i) {
		return parameterAtPosition(i).type();
	}
	@Override public String nameOfParameterAtPosition(final int i) {
		return parameterAtPosition(i).name();
	}
	@Override public ActualOrFormalParameterList mapTemplateParameters(final TemplateInstantiationInfo templateTypes) {
		// templateTypes can be null if we're processing a lookup in an actual template
		final FormalParameterList retval = new FormalParameterList();
		for (int i = count() - 1; i >= 0; --i) {
			final ObjectDeclaration aParameter = parameterAtPosition(i);
			final Type typeOfParameter = typeOfParameterAtPosition(i);
			
			// This method's scope has been been given a templateTypes
			// list only if that scope corresponds to an instantiated
			// class. We can get here for the lookup in the initial template,
			// in which case templateTypes.size() == 0. 
			if (null != typeOfParameter && typeOfParameter instanceof TemplateParameterType && null != templateTypes && templateTypes.size() > 0) {
				assert templateTypes.size() > i - 1;
				final ObjectDeclaration substituteDecl = new ObjectDeclaration(
						aParameter.name(), templateTypes.get(i - 1), aParameter.lineNumber());
				retval.addFormalParameter(substituteDecl);
			} else if (null != typeOfParameter && typeOfParameter instanceof TemplateType && null != templateTypes) {
				final ObjectDeclaration substituteDecl = new ObjectDeclaration(
						aParameter.name(), templateTypes.classType(), aParameter.lineNumber());
				retval.addFormalParameter(substituteDecl);
			} else {
				retval.addFormalParameter(aParameter);
			}
		}
		return retval;
	}
	@Override public String getText() {
		final StringBuffer stringBuffer = new StringBuffer();
		final int l = count();
		for (int i = 0; i < l; i++) {
			final Type type = typeOfParameterAtPosition(i);
			stringBuffer.append(type.getText());
			if (i < l-1) stringBuffer.append(", ");
		}
		return stringBuffer.toString();
	}
}
