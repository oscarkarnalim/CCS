package p6.codeclarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import support.LibTuple;
import support.NaturalLanguageProcesser;

public class Suggester {
	public static ArrayList<ClaritySuggestionTuple> getSuggestionMessages(ArrayList<LibTuple> tokens,
			String programmingLanguageCode, String languageCode, boolean reindex) {
		return getSuggestionMessages(tokens, programmingLanguageCode, languageCode, reindex, true, true, true, true,
				true, true, true, true);
	}

	public static ArrayList<ClaritySuggestionTuple> getSuggestionMessages(ArrayList<LibTuple> tokens,
			String programmingLanguageCode, String languageCode, boolean reindex, boolean isTooShortIdentSuggested,
			boolean isNotDescriptiveIdentSuggested, boolean isIncorrectlyWrittenIdentSuggested,
			boolean isInconsistentTransitionIdentSuggested, boolean isTooShortCommentSuggested,
			boolean isNotDescriptiveCommentSuggested, boolean isIncorrectlyWrittenCommentSuggested,
			boolean isCommentPerSyntaxBlockSuggested) {

		// to store all feedback messages about code clarity
		ArrayList<ClaritySuggestionTuple> messages = new ArrayList<ClaritySuggestionTuple>();

		// get only identifiers and comments
		ArrayList<LibTuple> identifiers = new ArrayList<LibTuple>();
		ArrayList<LibTuple> comments = new ArrayList<LibTuple>();

		// get the commented lines
		HashSet<Integer> commentedLines = new HashSet<>();

		// start extracting the data
		for (int i = 0; i < tokens.size(); i++) {
			String type = tokens.get(i).getType();
			if (type.toLowerCase().equals("identifier")) {
				identifiers.add(tokens.get(i));
			} else if (type.toLowerCase().endsWith("comment")) {
				comments.add(tokens.get(i));

				// mark all commented lines
				if (programmingLanguageCode.equals("py"))
					// python
					commentedLines.add(tokens.get(i).getLine());
				else {
					// Java
					int counter = tokens.get(i).getRawText().split("\n").length;
					for (int j = 0; j < counter; j++) {
						commentedLines.add(tokens.get(i).getLine() + j);
					}
				}
			}
		}

		if (isCommentPerSyntaxBlockSuggested) {
			
			// check if each syntax has a comment around
			for (int i = 0; i < tokens.size(); i++) {

				// checking whether there is a syntax block (starts with ident or keyword) that
				// is featured with no comments. Adapted from DisguiseGenerator
				if (tokens.get(i).getRawText().matches("[a-zA-Z0-9_]+")) {

					// a little tuning to make the result more realistic
					if (programmingLanguageCode.equalsIgnoreCase("py") && tokens.get(i).getRawText().equals("import")) {
						// for Python, skip if the token is 'import'
						continue;
					} else if (programmingLanguageCode.equalsIgnoreCase("java")
							&& (tokens.get(i).getRawText().equals("import")
									|| tokens.get(i).getRawText().equals("package"))) {
						// for Java, skip if the token is 'import' and 'package'
						continue;
					}

					LibTuple prevSyntaxT = null;
					for (int j = i - 1; j >= 0; j--) {
						LibTuple temp = tokens.get(j);
						if (!temp.getType().endsWith("COMMENT") && !temp.getType().equals("WS")) {
							prevSyntaxT = temp;
							break;
						}
					}
					

					if (prevSyntaxT != null && tokens.get(i).getLine() - prevSyntaxT.getLine() >= 2) {

						boolean isCommentInBetween = false;

						// check whether there is at least one comment between this line to
						// prevSyntaxT's line
						int startLineToTop = tokens.get(i).getLine() - 1;
						while (startLineToTop > prevSyntaxT.getLine()) {
							if (commentedLines.contains(startLineToTop)) {
								isCommentInBetween = true;
								break;
							}
							startLineToTop--;
						}

						// check whether there is a comment after the line or even in the next line
						int startLineToBottom = tokens.get(i).getLine();

						// start searching the next syntax token with different line
						LibTuple nextSyntaxT = null;
						for (int j = i + 1; j < tokens.size(); j++) {
							LibTuple temp = tokens.get(j);
							// if it is neither comments nor whitespace
							if (!temp.getType().endsWith("COMMENT") && !temp.getType().equals("WS")) {
								// if the token's line is larger than startLineToBottom then set it to
								// nextSyntaxT. This will be the first token from different line.
								if (temp.getLine() != startLineToBottom) {
									nextSyntaxT = temp;
									break;
								}
							}
						}

						// set next syntax line (the threshold where the bottom iteration will stop)
						int nextSyntaxTLine = -1;

						if (nextSyntaxT != null) {
							// if next syntax exists, set the 'finish' line with that line
							nextSyntaxTLine = nextSyntaxT.getLine();
						} else {
							// otherwise, set next syntax line as the end of the code.
							nextSyntaxTLine = tokens.get(tokens.size() - 1).getLine();
							// if the last token is whitespace, add one line to nextSyntaxTLine so that it
							// can detect comments in the last position right before the whitespace
							if (tokens.get(tokens.size() - 1).getType().equals("WS")) {
								nextSyntaxTLine += 1;
							}
						}

						// check whether there is at least one comment between this line to
						// nextSyntaxT's line
						while (startLineToBottom < nextSyntaxTLine) {
							if (commentedLines.contains(startLineToBottom)) {
								isCommentInBetween = true;
								break;
							}
							startLineToBottom++;
						}

						// if not, generate a message
						if (!isCommentInBetween) {
							messages.add(new ClaritySuggestionTuple(tokens.get(i).getLine(), tokens.get(i).getColumn(),
									tokens.get(i), tokens.get(i).getRawText(), "comment", "not descriptive", null));
						}
					}
				}
			}
		}

		// get only unique identifier
		identifiers = retainOnlyDistinctIdentifiers(identifiers);

		// create spell checker manager
		SpellCheckerManager scm = new SpellCheckerManager(languageCode, reindex);

		// check spelling and length for ident
		messages = identSpellingAndLengthCheck(messages, identifiers, scm, isTooShortIdentSuggested,
				isNotDescriptiveIdentSuggested, isIncorrectlyWrittenIdentSuggested,
				isInconsistentTransitionIdentSuggested);

		// check spelling and length for comment
		messages = commentSpellingAndLengthCheck(messages, comments, scm, programmingLanguageCode,
				isTooShortCommentSuggested, isNotDescriptiveCommentSuggested, isIncorrectlyWrittenCommentSuggested);

		// sort
		Collections.sort(messages);

		// assign visual id
		for (int i = 0; i < messages.size(); i++) {
			messages.get(i).setVisualId("s" + (i + 1));
		}

		scm.terminate();

		return messages;
	}

	private static ArrayList<ClaritySuggestionTuple> identSpellingAndLengthCheck(
			ArrayList<ClaritySuggestionTuple> result, ArrayList<LibTuple> identifiers, SpellCheckerManager scm,
			boolean isTooShortIdentSuggested, boolean isNotDescriptiveIdentSuggested,
			boolean isIncorrectlyWrittenIdentSuggested, boolean isInconsistentTransitionIdentSuggested) {
		/*
		 * this will update the result given in parameter as some tokens share the same
		 * position.
		 */

		// store the transition of each identifier
		ArrayList<Integer> identTransitionTypes = new ArrayList<Integer>();

		for (int i = 0; i < identifiers.size(); i++) {
			LibTuple cur = identifiers.get(i);

			// add the transition type
			identTransitionTypes.add(getIdentTransitionType(cur.getRawText()));

			// identifier a,b,c,i,j,k,x,y,z are exempted as they are often used as iterator
			if (cur.getRawText().equals("a") || cur.getRawText().equals("b") || cur.getRawText().equals("c")
					|| cur.getRawText().equals("i") || cur.getRawText().equals("j") || cur.getRawText().equals("k")
					|| cur.getRawText().equals("x") || cur.getRawText().equals("y") || cur.getRawText().equals("z"))
				continue;

			// check whether the identifier is too short
			if (isTooShortIdentSuggested && cur.getRawText().length() < 3) {
				// check message with the same pos, resulted from suggesting comment before
				// syntax block
				int existingPos = getMessageWithTheSamePos(result, cur.getLine(), cur.getColumn());
				boolean isSyntaxBlockRequireComment = false;
				if (existingPos != -1) {
					// if found, mark the boolean as true and remove that old element
					isSyntaxBlockRequireComment = true;
					result.remove(existingPos);
				}

				result.add(new ClaritySuggestionTuple(cur.getLine(), cur.getColumn(), cur, cur.getRawText(),
						"identifier", "too short", null, isSyntaxBlockRequireComment, false, false));
				continue;
			}

			// tokenise each identifier
			ArrayList<String> terms = CommentAndIdentTokenizer.tokenizeIdentifier(cur.getRawText());

			// identify whether the identifier is all stop words
			boolean isAllStopWordsAndNumbers = true;

			// this var pairs each incorrect word with its corrected form
			HashMap<String, String> correctedWords = new HashMap<>();

			// loop per subword
			for (int j = 0; j < terms.size(); j++) {
				String s = terms.get(j);

				// check whether it is a number
				boolean isNumber = false;
				try {
					Double.parseDouble(s);
					isNumber = true;
				} catch (Exception e) {
				}

				// if not, try to suggest a new word if that word is incorrectly written
				if (isNumber == false) {
					String newS = scm.suggest(s);
					if (newS != null && newS.matches("^[a-zA-Z0-9]*$")) {
						// add to list of corrected words
						correctedWords.put(s, newS);
					}

					// once it is neither a stop word nor a number, set isAllStopWordsAndNumbers to
					// false
					if (NaturalLanguageProcesser.isStopWord(s, scm.getLanguageCode()) == false) {
						isAllStopWordsAndNumbers = false;
					}
				}

			}

			if (isNotDescriptiveIdentSuggested && isAllStopWordsAndNumbers) {
				// if it is all stop words, add another message

				// check message with the same pos, resulted from suggesting comment before
				// syntax block
				int existingPos = getMessageWithTheSamePos(result, cur.getLine(), cur.getColumn());
				boolean isSyntaxBlockRequireComment = false;
				if (existingPos != -1) {
					// if found, mark the boolean as true and remove that old element
					isSyntaxBlockRequireComment = true;
					result.remove(existingPos);
				}

				result.add(new ClaritySuggestionTuple(cur.getLine(), cur.getColumn(), cur, cur.getRawText(),
						"identifier", "not meaningful", null, isSyntaxBlockRequireComment, false, false));
			} else if (isIncorrectlyWrittenIdentSuggested && correctedWords.size() > 0) {
				// generate spell correction messages

				// check message with the same pos, resulted from suggesting comment before
				// syntax block
				int existingPos = getMessageWithTheSamePos(result, cur.getLine(), cur.getColumn());
				boolean isSyntaxBlockRequireComment = false;
				if (existingPos != -1) {
					// if found, mark the boolean as true and remove that old element
					isSyntaxBlockRequireComment = true;
					result.remove(existingPos);
				}

				result.add(
						new ClaritySuggestionTuple(cur.getLine(), cur.getColumn(), cur, cur.getRawText(), "identifier",
								"incorrectly written", correctedWords, isSyntaxBlockRequireComment, false, false));
			}

		}

		if (isInconsistentTransitionIdentSuggested) {
			// determine the primary style of the programmer
			int capitalisationTransitionCounter = 0;
			int underscoreTransitionCounter = 0;
			for (int i = 0; i < identTransitionTypes.size(); i++) {
				int type = identTransitionTypes.get(i);
				if (type == 0)
					capitalisationTransitionCounter++;
				else if (type == 1)
					underscoreTransitionCounter++;
			}

			// set the suggestion message according to the most commonly-used style
			if (capitalisationTransitionCounter > underscoreTransitionCounter) {
				for (int i = 0; i < identTransitionTypes.size(); i++) {
					int type = identTransitionTypes.get(i);
					LibTuple cur = identifiers.get(i);

					if (type != 0 && type != 3) {
						// check message with the same pos, resulted from other suggestion
						int existingPos = getMessageWithTheSamePos(result, cur.getLine(), cur.getColumn());
						if (existingPos != -1) {
							// if found, mark the marker of inconsistent identifier name
							result.get(existingPos).setInconsistentIdentCapitalisation(true);
						} else {
							result.add(new ClaritySuggestionTuple(cur.getLine(), cur.getColumn(), cur, cur.getRawText(),
									"identifier", "inconsistent transition capitalisation", null));
						}
					}
				}
			} else if (underscoreTransitionCounter > capitalisationTransitionCounter) {
				for (int i = 0; i < identTransitionTypes.size(); i++) {
					int type = identTransitionTypes.get(i);
					LibTuple cur = identifiers.get(i);

					if (type != 1 && type != 3) {
						// check message with the same pos, resulted from other suggestion
						int existingPos = getMessageWithTheSamePos(result, cur.getLine(), cur.getColumn());
						if (existingPos != -1) {
							// if found, mark the marker of inconsistent identifier name
							result.get(existingPos).setInconsistentIdentUnderscore(true);
						} else {
							result.add(new ClaritySuggestionTuple(cur.getLine(), cur.getColumn(), cur, cur.getRawText(),
									"identifier", "inconsistent transition underscore", null));
						}
					}
				}
			}
		}

		return result;
	}

	private static int getMessageWithTheSamePos(ArrayList<ClaritySuggestionTuple> messages, int line, int col) {
		for (int i = 0; i < messages.size(); i++) {
			if (messages.get(i).getLine() == line && messages.get(i).getCol() == col)
				return i;
		}
		return -1;
	}

	private static int getIdentTransitionType(String identName) {
		boolean isUnderscoreTransition = false;
		boolean isCapitalisationTransition = false;

		for (int j = 0; j < identName.length(); j++) {
			char c = identName.charAt(j);

			if (c >= 65 && c <= 90) {

				/*
				 * if it is located not in the first pos and the previous one is lowercased one,
				 * mark isCapitalisationTransition to true
				 */
				if (j != 0 && identName.charAt(j - 1) >= 97 && identName.charAt(j - 1) <= 122)
					isCapitalisationTransition = true;
			}

			/*
			 * if _ is located in the middle of identifier, mark isUnderscoreTransition to
			 * true
			 */
			if (c == '_' && j != 0 && j != identName.length() - 1)
				isUnderscoreTransition = true;
		}

		if (isCapitalisationTransition && !isUnderscoreTransition)
			return 0; // capitalisation
		else if (!isCapitalisationTransition && isUnderscoreTransition)
			return 1; // underscore
		else if (isCapitalisationTransition && isUnderscoreTransition)
			return 2; // mixed
		else
			return 3; // unidentified
	}

	private static ArrayList<ClaritySuggestionTuple> commentSpellingAndLengthCheck(
			ArrayList<ClaritySuggestionTuple> result, ArrayList<LibTuple> comments, SpellCheckerManager scm,
			String programmingLanguageCode, boolean isTooShortCommentSuggested,
			boolean isNotDescriptiveCommentSuggested, boolean isIncorrectlyWrittenCommentSuggested) {

		/*
		 * used to count how many chars used to prefix the comment in a particular
		 * programming language
		 */
		int additionalMinLimitForComment = 3; // for Java
		if (programmingLanguageCode.equalsIgnoreCase("py"))
			additionalMinLimitForComment = 2; // for python

		for (int i = 0; i < comments.size(); i++) {
			LibTuple cur = comments.get(i);

			// check whether the comment is too short
			if (isTooShortCommentSuggested && cur.getRawText().length() < 3 + additionalMinLimitForComment) {
				result.add(new ClaritySuggestionTuple(cur.getLine(), cur.getColumn(), cur, cur.getRawText(), "comment",
						"too short", null));
				continue;
			}

			// tokenise each identifier
			ArrayList<String> terms = CommentAndIdentTokenizer.tokenizeComment(cur.getRawText());

			// identify whether the comment is all stop words
			boolean isAllStopWordsAndNumbers = true;

			// this var pairs each incorrect word with its corrected form
			HashMap<String, String> correctedWords = new HashMap<>();
			// loop per subword
			for (int j = 0; j < terms.size(); j++) {
				String s = terms.get(j);

				// check whether it is a number
				boolean isNumber = false;
				try {
					Double.parseDouble(s);
					isNumber = true;
				} catch (Exception e) {
				}

				// if not, suggest a new word
				if (isNumber == false) {
					String newS = scm.suggest(s);
					if (newS != null && newS.matches("^[a-zA-Z0-9]*$")) {
						// add to list of corrected words
						correctedWords.put(s, newS);
					}

					// once it is neither a stop word nor a number, set isAllStopWordsAndNumbers to
					// false
					if (NaturalLanguageProcesser.isStopWord(s, scm.getLanguageCode()) == false) {
						isAllStopWordsAndNumbers = false;
					}
				}
			}

			/*
			 * the comment content is shortened for conciseness. Just the first 10 chars but
			 * with all whitespaces replaced with a single space.
			 */
			String subComment = cur.getRawText().replaceAll("\\s+", " ");
			if (subComment.length() > 10) {
				// this complex syntax just to assure that the subcomment is readable
				subComment = subComment.substring(0, Math.max(10, 10 + subComment.substring(10).indexOf(" ")));
			}

			if (isNotDescriptiveCommentSuggested && isAllStopWordsAndNumbers && terms.size() > 0) {
				// just when all words in comment are either stop words or numbers and there are
				// words to be parsed. This is to handle the fact that some comments are used to
				// group program code.
				result.add(new ClaritySuggestionTuple(cur.getLine(), cur.getColumn(), cur, subComment, "comment",
						"not meaningful", null));
			} else if (isIncorrectlyWrittenCommentSuggested && correctedWords.size() > 0) {
				result.add(new ClaritySuggestionTuple(cur.getLine(), cur.getColumn(), cur, subComment, "comment",
						"incorrectly written", correctedWords));
			}

		}

		return result;
	}

	private static ArrayList<LibTuple> retainOnlyDistinctIdentifiers(ArrayList<LibTuple> identifiers) {
		/*
		 * this method returns a list of distinct identifiers.
		 */

		// to store the result
		ArrayList<LibTuple> distinctIdentifiers = new ArrayList<LibTuple>();

		// per token
		for (int i = 0; i < identifiers.size(); i++) {
			LibTuple cur = identifiers.get(i);
			String identName = cur.getRawText();

			// check whether the result has such ident name
			boolean isFound = false;
			for (int j = 0; j < distinctIdentifiers.size(); j++) {
				if (distinctIdentifiers.get(j).getRawText().equals(identName)) {
					isFound = true;
					break;
				}
			}

			// if not, add
			if (isFound == false) {
				distinctIdentifiers.add(cur);
			}
		}

		return distinctIdentifiers;
	}
}
