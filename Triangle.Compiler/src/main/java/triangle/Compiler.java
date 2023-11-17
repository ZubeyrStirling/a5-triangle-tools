/*
 * @(#)Compiler.java                        2.1 2003/10/07
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package triangle;

import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.optimiser.SummaryStatistics;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.treeDrawer.Drawer;
import java.io.StringReader;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;
import com.opencsv.CSVReader;
import java.util.List;

/**
 * The main driver class for the Triangle compiler.
 *
 * @version 2.1 7 Oct 2003
 * @author Deryck F. Brown
 */
public class Compiler {

	/** The filename for the object program, normally obj.tam. */
	static String objectName = "obj.tam";
	
	@Argument(alias = "showTree", description = "Shows AST", required = false)
	private static boolean showTree = false;
	@Argument(alias = "folding", description = "fold constant", required = false)
	private static boolean folding = false;
	@Argument(alias = "foldingAndShowTree", description = "fold constant and show AST", required = false)
	private static boolean foldingAndShowTree = false;
	@Argument(alias = "count", description = "count", required = false)
	private static boolean count = false;
	@Argument(alias = "-o", description = "-o", required = true)
	private static boolean o = false;


	

	private static Scanner scanner;
	private static Parser parser;
	private static Checker checker;
	private static Encoder encoder;
	private static Emitter emitter;
	private static ErrorReporter reporter;
	private static Drawer drawer;

	/** The AST representing the source program. */
	private static Program theAST;

	/**
	 * Compile the source program to TAM machine code.
	 *
	 * @param sourceName   the name of the file containing the source program.
	 * @param objectName   the name of the file containing the object program.
	 * @param showingAST   true iff the AST is to be displayed after contextual
	 *                     analysis
	 * @param showingTable true iff the object description details are to be
	 *                     displayed during code generation (not currently
	 *                     implemented).
	 * @return true iff the source program is free of compile-time errors, otherwise
	 *         false.
	 */
	static boolean compileProgram(String sourceName, String objectName, boolean showingAST, boolean showingTable) {

		System.out.println("********** " + "Triangle Compiler (Java Version 2.1)" + " **********");

		System.out.println("Syntactic Analysis ...");
		SourceFile source = SourceFile.ofPath(sourceName);

		if (source == null) {
			System.out.println("Can't access source file " + sourceName);
			System.exit(1);
		}

		scanner = new Scanner(source);
		reporter = new ErrorReporter(false);
		parser = new Parser(scanner, reporter);
		checker = new Checker(reporter);
		emitter = new Emitter(reporter);
		encoder = new Encoder(emitter, reporter);
		drawer = new Drawer();

		// scanner.enableDebugging();
		theAST = parser.parseProgram(); // 1st pass
		if (reporter.getNumErrors() == 0) {
			// if (showingAST) {
			// drawer.draw(theAST);
			// }
			System.out.println("Contextual Analysis ...");
			checker.check(theAST); // 2nd pass
			if (showingAST) {
				drawer.draw(theAST);
			}
			if (folding) {
				theAST.visit(new ConstantFolder());
			}
			if(foldingAndShowTree) {
				theAST.visit(new ConstantFolder());
				drawer.draw(theAST);
			}
			
			if(count) {
				
				
				SummaryStatistics ss = new SummaryStatistics();
				
				theAST.visit(ss);
				
			    int cc = ss.getCharacterExpressionCount();
			    int ic = ss.getIntegerExpressionCount();

			    System.out.println("Character Expressions: " + cc);
			    System.out.println("Integer Expressions: " + ic);
			}
			
			if (reporter.getNumErrors() == 0) {
				System.out.println("Code Generation ...");
				encoder.encodeRun(theAST, showingTable); // 3rd pass
			}
		}

		boolean successful = (reporter.getNumErrors() == 0);
		if (successful) {
			emitter.saveObjectProgram(objectName);
			System.out.println("Compilation was successful.");
		} else {
			System.out.println("Compilation was unsuccessful.");
		}
		return successful;
	}

	/**
	 * Triangle compiler main program.
	 *
	 * @param args the only command-line argument to the program specifies the
	 *             source filename.
	 */
	public static void main(String[] args) {
		
		Compiler compiler = new Compiler();
		
		
		if (args.length < 1) {
			System.out.println("Usage: tc filename [-o outputfilename] [tree] [folding] [foldingAndShowTree] [count]");
			System.exit(1);
		}
		
		
		List<String> parsed = Args.parseOrExit(compiler, args);
		   
		

     
	    
		
	    parseArgs(parsed);
	    
		String sourceName = args[0];
		
		
		var compiledOK = compileProgram(sourceName, objectName, showTree, false);

		if (!showTree) {
			System.exit(compiledOK ? 0 : 1);
		}
	}
	
	
	
//	private static void parseArgs(List<String> args) {
//	    
//		for (String s : args) {
//			var sl = s.toLowerCase();
//			if (sl.equals("tree")) {
//				showTree = true;
//			} else if (sl.startsWith("-o=")) {
//				System.out.println(sl);
//				objectName = s.substring(3);
//			} else if (sl.equals("folding")) {
//				folding = true;
//			} else if (sl.equals("foldingAndShowTree")) {
//				foldingAndShowTree = true;
//			}
//			else if (sl.equals("count")) {
//				count = true;
//			}
//			
//		}
//	}
	
	private static void parseArgs(List<String> args) {
	for (String s : args) {
		var sl = s.toLowerCase();
		if (sl.equals("tree")) {
			showTree = true;
		} else if (sl.endsWith("tam")) {
			objectName = sl;
		} else if (sl.equals("folding")) {
			folding = true;
		}
	}
}
	
}
