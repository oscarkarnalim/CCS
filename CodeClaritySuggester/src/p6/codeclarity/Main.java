package p6.codeclarity;

import java.io.File;
import java.util.HashMap;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		/*
		 * -outtext: set the output to a text file
		 * 
		 * -path: program path
		 * 
		 * -akpath: additional keyword path
		 * 
		 * -proglang: programming language ('py' or 'java')
		 * 
		 * -humanlang: human language ('en', 'id', or 'iden')
		 * 
		 * -reindex: reindex word database, used when the human language is just changed
		 * 
		 * Arguments to determine what kinds of suggestions excluded -excludesi,
		 * -excludemi, -excludeii, -excludeici -excludesc, -excludemc, -excludeic,
		 * -excludebc
		 */

		// if no arguments given, show help
		if (args.length == 0) {
			showHelp();
			return;
		}

		// get all the setting tuples
		HashMap<String, String> settingTuples = new HashMap<String, String>();
		int i = 0;
		while (i < args.length) {
			String cur = args[i];
			if (cur.startsWith("-")) {
				// this is a correct argument
				if (cur.equals("-path") || cur.equals("-akpath") || cur.equals("-proglang")
						|| cur.equals("-humanlang")) {
					// keywords requiring a value
					if (++i < args.length) {
						if (args[i].startsWith("-") == false) {
							// get the value and add a new setting tuple
							settingTuples.put(cur, args[i]);
						} else {
							// error the next element is another keyword
							System.err.println("Argument '" + args[i]
									+ "' is expected to be a setting value but another keyword is given");
							System.err.println("Run this program without arguments to show help");
							return;
						}
					} else {
						// error the next element is expected but empty
						System.err.println(
								"Argument " + (i + 1) + " is expected to be a setting value but none is given");
						System.err.println("Run this program without arguments to show help");
						return;
					}
				} else if (cur.equals("-reindex") || cur.equals("-excludesi") || cur.equals("-excludemi")
						|| cur.equals("-excludeii") || cur.equals("-excludeici") || cur.equals("-excludesc")
						|| cur.equals("-excludemc") || cur.equals("-excludeic") || cur.equals("-excludebc")
						|| cur.equals("-outtext")) {
					// keywords without any values
					settingTuples.put(cur, "true");
				} else {
					// error the read string is not recognised
					System.err.println("Argument '" + args[i] + "' is not recognised as a setting keyword");
					System.err.println("Run this program without arguments to show help");
					return;
				}
			} else {
				// error the read string is not a setting keyword
				System.err.println(
						"Argument '" + args[i] + "' is expected to be a setting keyword started with hypen ('-')");
				System.err.println("Run this program without arguments to show help");
				return;
			}
			i++;
		}

		// checking program path
		String path = settingTuples.get("-path");
		if (path == null) {
			System.err.println("No program path is given");
			System.err.println("Run this program without arguments to show help");
			return;
		} else {
			path = preparePathOrRegex(path);
			if (isPathValidAndExist(path) == false) {
				System.err.println("Program path is not valid or refers to a nonexistent file.");
				System.err.println("Run this program without arguments to show help");
				return;
			}
		}

		// checking programming language
		String programmingLanguageCode = settingTuples.get("-proglang");
		if (programmingLanguageCode == null) {
			System.err.println("No programming language is defined");
			System.err.println("Run this program without arguments to show help");
			return;
		} else {
			if (isProgrammingLanguageValid(programmingLanguageCode) == false) {
				System.err.println("Programming language should be either 'java' or 'py'");
				System.err.println("Run this program without arguments to show help");
				return;
			}
		}

		// checking human language
		String languageCode = settingTuples.get("-humanlang");
		if (languageCode == null) {
			// if no human language is set, define as english ("en")
			languageCode = "en";
		}
		// check whether the human language is valid
		if (isHumanLanguageValid(languageCode) == false) {
			System.err.println("Human language should be either 'en', 'id', or 'iden'");
			System.err.println("Run this program without arguments to show help");
			return;
		}

		// checking additional keywords
		String additionalKeywordsPath = settingTuples.get("-akpath");
		if (additionalKeywordsPath == null) {
			if (programmingLanguageCode.equals("java"))
				additionalKeywordsPath = "java input output keywords.txt";
			else if (programmingLanguageCode.equals("py"))
				additionalKeywordsPath = "python input output keywords.txt";
		}
		// check whether the additional keywords file path is valid
		if (additionalKeywordsPath != null && isPathValidAndExist(additionalKeywordsPath) == false) {
			System.err.println("Additional keywords file path is not valid or refers to a nonexistent file.");
			System.err.println("Run this program without arguments to show help");
			return;
		}

		// remaining boolean arguments

		boolean reindex = false;
		if (settingTuples.get("-reindex") != null)
			reindex = true;

		boolean isTooShortIdentSuggested = true;
		if (settingTuples.get("-excludesi") != null)
			isTooShortIdentSuggested = false;

		boolean isNotDescriptiveIdentSuggested = true;
		if (settingTuples.get("-excludemi") != null)
			isNotDescriptiveIdentSuggested = false;

		boolean isIncorrectlyWrittenIdentSuggested = true;
		if (settingTuples.get("-excludeii") != null)
			isIncorrectlyWrittenIdentSuggested = false;

		boolean isInconsistentTransitionIdentSuggested = true;
		if (settingTuples.get("-excludeici") != null)
			isInconsistentTransitionIdentSuggested = false;

		boolean isTooShortCommentSuggested = true;
		if (settingTuples.get("-excludesc") != null)
			isTooShortCommentSuggested = false;

		boolean isNotDescriptiveCommentSuggested = true;
		if (settingTuples.get("-excludemc") != null)
			isNotDescriptiveCommentSuggested = false;

		boolean isIncorrectlyWrittenCommentSuggested = true;
		if (settingTuples.get("-excludeic") != null)
			isIncorrectlyWrittenCommentSuggested = false;

		boolean isCommentPerSyntaxBlockSuggested = true;
		if (settingTuples.get("-excludebc") != null)
			isCommentPerSyntaxBlockSuggested = false;

		boolean isOutText = false;
		if (settingTuples.get("-outtext") != null)
			isOutText = true;

		// start to process
		CodeClarityContentGenerator.execute(path, additionalKeywordsPath, programmingLanguageCode, languageCode,
				reindex, isTooShortIdentSuggested, isNotDescriptiveIdentSuggested, isIncorrectlyWrittenIdentSuggested,
				isInconsistentTransitionIdentSuggested, isTooShortCommentSuggested, isNotDescriptiveCommentSuggested,
				isIncorrectlyWrittenCommentSuggested, isCommentPerSyntaxBlockSuggested, isOutText);
	}

	private static void showHelp() {
		println("Code clarity suggester (CCS) aims to educate computing students about code clarity in programming.");
		println("It scans Java/Python program code and highlights any identifier names or comments whose clarity");
		println("needs to be improved. For convenience, the suggestion is mapped to an interactive HTML file.");

		println("\nCCS provides eight suggestion types:");
		println("1. Suggestion to update identifier names that are too short.");
		println("2. Suggestion to update identifier names that are not meaningful.");
		println("3. Suggestion to update identifier names that are incorrectly written.");
		println("4. Suggestion to update identifier names that use inconsistent word transition compared to");
		println("   other names. The transition is either capitalisation ('thisIsIdent') or underscore");
		println("   ('this_is_ident').");
		println("5. Suggestion to update comments that are too short.");
		println("6. Suggestion to update comments that are not meaningful.");
		println("7. Suggestion to update comments that are incorrectly written.");
		println("8. Suggestion to add an explaining comment for each syntax block.");

		println("\nMinimum command: -path <program_path> -proglang <programming_language>");
		println("  <program_path> is the complete path of targeted program code file.");
		println("  <programming_language> refers to the programming language used in the program code.");
		println("    values: 'java' (for Java) or 'py' (for Python).");
		println("  The result can be seen in 'out.html'.");

		println("\nAdditional arguments:");
		println("  -akpath <additional_keyword_path>");
		println("    It is applicable when some identifiers need to be recognised as keywords. This typically");
		println("    happens when the program uses third-party libraries. <additional_keywords_path> refers");
		println("    to a file containing additional keywords with newline as the delimiter. Keywords with");
		println("    more than one token should be written by embedding spaces between the tokens. For example,");
		println("    'System.out.print' should be written as 'System . out . print'.");
		println("  -humanlang <human_language>");
		println("    This changes the human language used while delivering the suggestions.");
		println("    <human_language> values: 'en' for both British and American English (default), 'id' for ");
		println("      Indonesian, and 'iden' for Indonesian and English.");
		println("  -reindex");
		println("    This forces CCS to reindex word database for spell checking. Please use it only when the ");
		println("    human language is changed as the process is quite time consuming.");
		println("  -outtext");
		println("    This alters the resulted output to a standard text file ('out.txt'). This might be useful");
		println("    when CCS is integrated to larger system.");
		println("  -excludesi");
		println("    This removes suggestions about identifier names that are too short.");
		println("  -excludemi");
		println("    This removes suggestions about identifier names that are not meaningful.");
		println("  -excludeii");
		println("    This removes suggestions about identifier names that are incorrectly written.");
		println("  -excludeici");
		println("    This removes suggestions about identifier names that use inconsistent word transition.");
		println("  -excludesc");
		println("    This removes suggestions about comments that are too short.");
		println("  -excludemc");
		println("    This removes suggestions about comments that are not meaningful.");
		println("  -excludeic");
		println("    This removes suggestions about comments that are incorrectly written.");
		println("  -excludebc");
		println("    This removes suggestions about adding an explaining comment per syntax block.");
	}

	private static boolean isHumanLanguageValid(String humanLang) {
		if (humanLang != null && (humanLang.equals("id") || humanLang.equals("en") || humanLang.equals("iden")))
			return true;
		else
			return false;
	}

	// copied from STRANGE
	private static void println(String s) {
		System.out.println(s);
	}

	private static String preparePathOrRegex(String path) {
		if (path != null && (path.startsWith("'") || path.startsWith("\"")))
			return path.substring(1, path.length() - 1);
		else
			return path;
	}

	private static boolean isPathValidAndExist(String path) {
		// check the validity of the string
		if (isPathValid(path) == false)
			return false;

		// check whether such file exists
		File f = new File(path);
		if (f.exists() == false)
			return false;

		return true;
	}

	private static boolean isPathValid(String path) {
		// check the validity of the string
		if (path == null || path.length() == 0)
			return false;
		else
			return true;
	}

	private static boolean isProgrammingLanguageValid(String prog) {
		if (prog != null && (prog.equals("java") || prog.equals("py")))
			return true;
		else
			return false;
	}
}
