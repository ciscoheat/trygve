package info.fulloo.trygve.editor;

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

public class TestRunner {
	private enum TestSource { UseUrl, UseLocalFile };
	TestSource testSource_;
	private final static String urlPrefix_ = "file:tests/";
	private final static String localTestDir_ = "file:tests/";
	private final static String localPrefix_ = "tests/";
	private final static String fileNames_[] = {
		"ctor1.k",
		"exprtest.k",
		"for1.k",
		"forloop1.k",
		"forloop2.k",
		"forloop3.k",
		"forloop4.k",
		"inheritance.k",
		"inheritance2.k",
		"inheritance3.k",
		"inheritance4.k",
		"inheritance5.k",
		"inhertest.k",
		"roletest.k",
		"roletest2.k",
		"roletest3.k",
		"roletest4.k",
		"roletest5.k",
		"trygve1.k",
		"simplemain.k",
		"simplearray.k",
		"arraydup.k",
		"simpleincrement.k",
		"switchtest.k",
		"unaryop1.k",
		"trygve2.k",
		"trygve3.k",
		"whiletest.k",
		"roleplaytest.k",
		"roleplaytest2.k",
		"roleplaytest3.k",
		"assignchain.k",
		"forloop2.k",
		"fact.k",
		"simpleprintchain.k",
		"simpletemplate.k",
		"basectortest.k",
		"rolevec1.k",
		"access1.k",
		"roleclass1.k",
		"roleclass2.k",
		"sqrt.k",
		"interface1.k",
		"interface2.k",
		"interface3.k",
		"quadratic1.k",
		"rightAssociativeSum2.k",
		"thisqualbug1.k",
		"simplestring1.k",
		"simplest_role.k",
		"initordertest.k",
		"returntest1.k",
		"arraysizetest1.k",
	};
	public static int numberOfTestCases() {
		return fileNames_.length;
	}
	public static String urlForTestCase(final int i) {
		return urlPrefix_ + fileNames_[i];
	}
	public static String fileNameForTestCase(final int i) {
		return fileNames_[i];
	}
	public TestRunner(final TextEditorGUI gui) {
		gui_ = gui;
		underscores_ = "___________________________________________________________";
		plusses_ = " +  +  +  ";
		passCounter_ = failCounter_ = 0;
		testSource_ = TestSource.UseLocalFile;
	}
	public void runTests() {
		final String saveFileNameField = gui_.getFileNameField();
		String testResults = gui_.errorPanelContents();
		String lastTestResults = testResults;
		passCounter_ = failCounter_ = 0;
		for (final String filename : fileNames_) {
			runATest(filename);
			if (gui_.compiledWithoutError() == false) {
				// break;
			}
			System.out.flush();
			System.err.flush();
			testResults = gui_.errorPanelContents();
			checkTestResults(lastTestResults, testResults);
			lastTestResults = gui_.errorPanelContents();
		}
		
		gui_.console().redirectErr(java.awt.Color.BLUE, null);
		System.err.println(underscores_);
		
		if (failCounter_ > 0) {
			gui_.console().redirectErr(java.awt.Color.RED, null);
		} else {
			gui_.console().redirectErr(new java.awt.Color(20, 210, 20), null);
		}
		System.err.print(passCounter_);
		System.err.print(" tests passed; ");
		System.err.print(failCounter_);
		System.err.println(" tests failed.");
		gui_.console().redirectErr(java.awt.Color.RED, null);
		if (0 == failCounter_) {
			gui_.setFileNameField(saveFileNameField);
		}
	}
	private void runATest(final String filename) {
		String url = null;
		switch (testSource_) {
		case UseUrl:
			url = urlPrefix_ + filename; break;
		case UseLocalFile:
			url = localTestDir_ + filename; break;
		}
		
		gui_.console().redirectErr(java.awt.Color.BLUE, null);
		System.err.println(underscores_);
		System.err.print(plusses_); System.err.print(url); System.err.println(plusses_);
		gui_.console().redirectErr(java.awt.Color.RED, null);
		gui_.resetCompiledWithoutError();
		gui_.setFileNameField(localPrefix_ + filename);	// just in case user edits / saves - goes to the right place
		gui_.setWWWFileNameField(url);
		gui_.wwwButtonActionPerformed(null);
		gui_.parseButtonActionPerformed(null);
		if (gui_.compiledWithoutError()) {
			gui_.runButtonActionPerformed(null);
		}
	}
	/*
	private void printHelper(String s1, String s2, int i) {
		System.err.print(i);
		System.err.print(": (\\0");
		System.err.print(Integer.toOctalString(s1.charAt(i)));
		System.err.print("), (\\0");
		System.err.print(Integer.toOctalString(s2.charAt(i)));
		System.err.print(").");
	}
	private void specialErrorAnalysis(String s1, String s2) {
		for (int i = 0; i < s1.length() && i < s2.length(); i++) {
			if (s1.charAt(i) != s2.charAt(i)) {
				System.err.print("Difference in test results at byte offset: ");
				printHelper(s1, s2, i);
				System.err.println();
				i++;
				if (i < s1.length() && i < s2.length()) {
					for (int j = 0; j < 40; j++) {
						if (j >= s1.length()) break;
						System.err.print("(\\0");
						System.err.print(Integer.toOctalString(s1.charAt(j)));
						System.err.print(")");
					}
					System.err.println();
					for (int j = 0; j < 40; j++) {
						if (j >= s2.length()) break;
						System.err.print("(\\0");
						System.err.print(Integer.toOctalString(s2.charAt(j)));
						System.err.print(")");
					}
					System.err.println();
				}
				
				break;
			}
		}
	}
	*/
	private void checkTestResults(final String lastTestResults, final String rawTestResults) {
		String testResults = thisTestResults(lastTestResults, rawTestResults);
		String goldContents = thisRunGoldContents();
		/*
		testResults = testResults.replaceAll("\012", "");
		goldContents = goldContents.replaceAll("\012", "");
		testResults = testResults.replaceAll("\014", "");
		goldContents = goldContents.replaceAll("\014", "");
		testResults = testResults.replaceAll("\015", "");
		goldContents = goldContents.replaceAll("\015", "");
		*/

		if (testResults.equals(goldContents)) {
			gui_.console().redirectErr(new java.awt.Color(20, 210, 20), null);
			System.err.println("Test passed");
			gui_.console().redirectErr(java.awt.Color.BLUE, null);
			passCounter_++;
		} else {
			/*
			specialErrorAnalysis(testResults, goldContents);
			*/
			gui_.console().redirectErr(java.awt.Color.RED, null);
			System.err.println("Test failed");
			gui_.console().redirectErr(java.awt.Color.BLUE, null);
			failCounter_++;
		}
	}

	private String thisTestResults(final String lastTestResults, final String rawTestResults) {
		final int lastTestResultsLength = lastTestResults.length();
		String testResults = rawTestResults.substring(lastTestResultsLength + underscores_.length() + 1);
		while (testResults.substring(0,1).equals("\n") || testResults.substring(0,1).equals("\r")) {
			testResults = testResults.substring(1);
		}
		/*
		System.err.print("beginning of testResults in thisTestResults: ");
		for (int i = 0; i < 10; i++) {
			System.err.print("(\\0");
			System.err.print(Integer.toOctalString(testResults.charAt(i)));
			System.err.print(" / ");
			System.err.print(testResults.charAt(i));
			System.err.print(")");
		}
		System.err.println();
		*/

		if (testResults.length() > 10 && testResults.substring(0, plusses_.length()).equals(plusses_)) {
			testResults = testResults.substring(plusses_.length());
			final int indexOfDelimitingSpace = testResults.indexOf(' ');
			final String fileName = testResults.substring(0, indexOfDelimitingSpace);
			testResults = testResults.substring(fileName.length());
			if (testResults.length() > 10 && testResults.substring(0,plusses_.length()).equals(plusses_)) {
				// +1 for newline after plusses, +1 for newline after underscores
				testResults = testResults.substring(plusses_.length() + 1);
			}
		} else {
			;
		}
		return testResults;
	}
	private String thisRunGoldContents() {
		final String goldDelimiter = "/* GOLD:\n", endDelimiter = "\n*/\n";
		String program = gui_.editPanelContents();
		final int goldIndex = program.indexOf(goldDelimiter);
		if (0 < goldIndex) {
			program = program.substring(goldIndex + goldDelimiter.length());
		}
		final int closingCommentIndex = program.indexOf(endDelimiter);
		if (0 < closingCommentIndex) {
			program = program.substring(0, closingCommentIndex);
		}
		return program;
	}

	private final TextEditorGUI gui_;
	private final String underscores_, plusses_;
	private int passCounter_, failCounter_;
}
