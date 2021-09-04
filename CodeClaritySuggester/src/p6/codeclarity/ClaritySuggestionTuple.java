package p6.codeclarity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import support.LibTuple;

public class ClaritySuggestionTuple implements Comparable<ClaritySuggestionTuple> {
	private int line, col;
	private LibTuple targetedToken;

	private String visualId;

	// highlighted token text
	private String hintTokenText;
	// either identifier or comment
	private String targetedContent;
	// either "too short", "not meaningful", "incorrectly written",
	// "inconsistent transition capitalisation", "inconsistent transition
	// underscore", and "not descriptive".
	// The first three are for both identifier names and comments.
	// The fourth and fifth are exclusive to identifier names while the sixth is
	// exclusive to
	// comments.
	private String potentialIssue;

	// required when an ident has two suggestions, one ident-related and one special
	// suggestion about the need of comment before the syntax.
	private boolean isRequireCommentAroundSyntax;
	// required when an ident is inconsistently written for underscore transition
	private boolean isInconsistentIdentUnderscore;
	// required when an ident is inconsistently written for capitalisation
	// transition
	private boolean isInconsistentIdentCapitalisation;

	// this var pairs each incorrect word with its corrected form
	private HashMap<String, String> correctedWords;

	public ClaritySuggestionTuple(int line, int col, LibTuple targetedToken, String hintTokenText,
			String targetedContent, String potentialIssue, HashMap<String, String> correctedWords,
			boolean isRequireCommentAroundSyntax, boolean isInconsistentIdentUnderscore,
			boolean isInconsistentIdentCapitalisation) {
		super();
		this.line = line;
		this.col = col;
		this.targetedToken = targetedToken;
		this.hintTokenText = hintTokenText;
		this.targetedContent = targetedContent;
		this.potentialIssue = potentialIssue;
		this.isRequireCommentAroundSyntax = isRequireCommentAroundSyntax;
		this.isInconsistentIdentUnderscore = isInconsistentIdentUnderscore;
		this.isInconsistentIdentCapitalisation = isInconsistentIdentCapitalisation;
		this.correctedWords = correctedWords;
		this.visualId = null;
	}

	public ClaritySuggestionTuple(int line, int col, LibTuple targetedToken, String hintTokenText,
			String targetedContent, String potentialIssue, HashMap<String, String> correctedWords) {
		this(line, col, targetedToken, hintTokenText, targetedContent, potentialIssue, correctedWords, false, false,
				false);
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public boolean isRequireCommentAroundSyntax() {
		return isRequireCommentAroundSyntax;
	}

	public void setRequireCommentAroundSyntax(boolean isRequireCommentBeforeSyntax) {
		this.isRequireCommentAroundSyntax = isRequireCommentBeforeSyntax;
	}
	
	public HashMap<String, String> getCorrectedWords(){
		return correctedWords;
	}

	public String getMessage(String languageCode) {
		String message = "";
		if (getTargetedContent().equals("identifier")) {
			if (getPotentialIssue().equals("too short")) {
				message = languageCode.equalsIgnoreCase("en")
						? "The identifier name '" + getHintTokenText() + "' is too short and might be not meaningful"
						: "Nama identifier '" + getHintTokenText() + "' terlalu pendek dan mungkin tidak bermakna";

			} else if (getPotentialIssue().equals("not meaningful")) {
				message = languageCode.equalsIgnoreCase("en")
						? "The identifier name '" + getHintTokenText()
								+ "' has no meaningful words and might not be descriptive"
						: "Nama identifier '" + getHintTokenText()
								+ "' tidak mengandung kata dan mungkin tidak deskriptif";

			} else if (getPotentialIssue().equals("incorrectly written")) {
				message = languageCode.equalsIgnoreCase("en")
						? "Part of an identifier '" + getHintTokenText() + "' might be incorrectly written"
						: "Bagian dari identifier '" + getHintTokenText() + "' mungkin salah tulis";

				// per corrected word
				Iterator<Entry<String, String>> it = correctedWords.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> c = it.next();
					message += languageCode.equalsIgnoreCase("en")
							? ("\n\tDid you mean '" + c.getValue() + "' instead of '" + c.getKey() + "'?")
							: ("\n\tApakah maksud kamu '" + c.getValue() + "' bukannya '" + c.getKey() + "'?");
				}

				// message to deal with students forgetting to use proper transition for
				// readability
				message += languageCode.equalsIgnoreCase("en")
						? "\nDid you forget to put proper transition between words (capitalisation or underscore) in the identifier?"
						: "\nApakah kamu lupa menggunakan transisi antar kata yang baik (kapitalisasi atau underskor) di nama identifier?";

			} else if (getPotentialIssue().equals("inconsistent transition capitalisation")) {
				return languageCode.equalsIgnoreCase("en")
						? "The identifier name '" + getHintTokenText()
								+ "' seems not to consistently use capitalisation for word transition"
						: "Nama identifier '" + getHintTokenText()
								+ "' tampak tidak menggunakan kapitalisasi sebagai transisi antar kata";
			} else if (getPotentialIssue().equals("inconsistent transition underscore")) {
				return languageCode.equalsIgnoreCase("en")
						? "The identifier name '" + getHintTokenText()
								+ "' seems not to consistently use underscore for word transition"
						: "Nama identifier '" + getHintTokenText()
								+ "' tampak tidak menggunakan underskor sebagai transisi antar kata";
			}

			// additional message about explaining comment per syntax block
			if (isRequireCommentAroundSyntax()) {
				message += "\n" + getMessageForNonDescriptiveComment(languageCode);
			}
			// additional message about inconsistent identifier
			if (isInconsistentIdentCapitalisation()) {
				message += languageCode.equalsIgnoreCase("en")
						? "The identifier name '" + getHintTokenText()
								+ "' seems not to consistently use capitalisation for word transition"
						: "Nama identifier '" + getHintTokenText()
								+ "' tampak tidak menggunakan kapitalisasi sebagai transisi antar kata";
			} else if (isInconsistentIdentUnderscore()) {
				message += languageCode.equalsIgnoreCase("en")
						? "The identifier name '" + getHintTokenText()
								+ "' seems not to consistently use underscore for word transition"
						: "Nama identifier '" + getHintTokenText()
								+ "' tampak tidak menggunakan underskor sebagai transisi antar kata";
			}
		} else if (getTargetedContent().equals("comment")) {

			if (getPotentialIssue().equals("too short")) {
				message = languageCode.equalsIgnoreCase("en")
						? "A comment '" + getHintTokenText() + "' is too short and might be not meaningful"
						: "Komentar '" + getHintTokenText() + "' terlalu pendek dan mungkin tidak bermakna";
			} else if (getPotentialIssue().equals("not meaningful")) {
				// if it is all stop words and/or numbers, add another message
				message = languageCode.equalsIgnoreCase("en")
						? "A comment starting with '" + getHintTokenText()
								+ "' has no meaningful words and might not be descriptive"
						: "Komentar diawali dengan '" + getHintTokenText()
								+ "' tidak memiliki kata bermakna dan mungkin tidak deskriptif";

				// message to deal with students commenting their code
				message += languageCode.equalsIgnoreCase("en")
						? "\nIs it a part of program code commented out? if unnecessary, you might want to remove it"
						: "\nApakah ini bagian dari kode program yang dijadikan komentar? jika tidak diperlukan, kamu mungkin ingin membuangnya";

			} else if (getPotentialIssue().equals("incorrectly written")) {
				message = languageCode.equalsIgnoreCase("en")
						? "Part of a comment started with '" + getHintTokenText() + "' might be incorrectly written"
						: "Bagian dari komentar diawali dengan '" + getHintTokenText() + "' mungkin salah tulis";

				// per corrected word
				Iterator<Entry<String, String>> it = correctedWords.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> c = it.next();
					message += languageCode.equalsIgnoreCase("en")
							? ("\n\tDid you mean '" + c.getValue() + "' instead of '" + c.getKey() + "'?")
							: ("\n\tApakah maksud kamu '" + c.getValue() + "' bukannya '" + c.getKey() + "'?");
				}

				// message to deal with students commenting their code
				message += languageCode.equalsIgnoreCase("en")
						? "\nIs it a part of program code commented out? if unnecessary, you might want to remove it"
						: "\nApakah ini bagian dari kode program yang dijadikan komentar? jika tidak diperlukan, kamu mungkin ingin membuangnya";

			} else if (getPotentialIssue().equals("not descriptive")) {
				message = getMessageForNonDescriptiveComment(languageCode);
			}
		}
		return message;
	}

	public String getMessageForNonDescriptiveComment(String languageCode) {
		// when a syntax block has no explaining comment
		return languageCode.equalsIgnoreCase("en")
				? "A syntax block started with '" + getHintTokenText() + "' has no explaining comments around it; please ignore this if you think the code block is easy to understand even without comments"
				: "Blok sintaks diawali dengan '" + getHintTokenText()
						+ "' tidak memiliki komentar penjelas di baris-baris sekitarnya; abaikan hal ini jika kamu merasa kode terkait mudah dipahami walau tanpa komentar";
	}

	public LibTuple getTargetedToken() {
		return targetedToken;
	}

	public void setTargetedToken(LibTuple targetedToken) {
		this.targetedToken = targetedToken;
	}

	public String getHintTokenText() {
		return hintTokenText;
	}

	public void setHintTokenText(String hintTokenText) {
		this.hintTokenText = hintTokenText;
	}

	public String getTargetedContent() {
		return targetedContent;
	}

	public void setTargetedContent(String targetedContent) {
		this.targetedContent = targetedContent;
	}

	public String getPotentialIssue() {
		return potentialIssue;
	}

	public void setPotentialIssue(String potentialIssue) {
		this.potentialIssue = potentialIssue;
	}

	public boolean isInconsistentIdentUnderscore() {
		return isInconsistentIdentUnderscore;
	}

	public void setInconsistentIdentUnderscore(boolean isInconsistentIdentUnderscore) {
		this.isInconsistentIdentUnderscore = isInconsistentIdentUnderscore;
	}

	public boolean isInconsistentIdentCapitalisation() {
		return isInconsistentIdentCapitalisation;
	}

	public void setInconsistentIdentCapitalisation(boolean isInconsistentIdentCapitalisation) {
		this.isInconsistentIdentCapitalisation = isInconsistentIdentCapitalisation;
	}

	public String getVisualId() {
		return visualId;
	}

	public void setVisualId(String visualId) {
		this.visualId = visualId;
	}

	@Override
	public int compareTo(ClaritySuggestionTuple o) {
		// TODO Auto-generated method stub
		if (this.line != o.getLine())
			return this.line - o.getLine();
		else
			return this.col - o.getCol();
	}

	public String toString() {
		return "Line " + this.getLine() + " column " + this.getCol() + ":\n" + this.getMessage("en");
	}
}
