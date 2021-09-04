package p6.codeclarity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

import support.AdditionalKeywordsManager;
import support.LibJavaExtractor;
import support.LibPythonExtractor;
import support.LibTuple;

public class CodeClarityContentGenerator {

	public static void execute(String path, String additional_keywords_path, String programmingLanguageCode,
			String languageCode, boolean reindex, boolean isTooShortIdentSuggested,
			boolean isNotDescriptiveIdentSuggested, boolean isIncorrectlyWrittenIdentSuggested,
			boolean isInconsistentTransitionIdentSuggested, boolean isTooShortCommentSuggested,
			boolean isNotDescriptiveCommentSuggested, boolean isIncorrectlyWrittenCommentSuggested,
			boolean isCommentPerSyntaxBlockSuggested, boolean isOutText) {
		// read the keywords if needed
		ArrayList<ArrayList<String>> additional_keywords = new ArrayList<ArrayList<String>>();
		if (additional_keywords_path != null)
			additional_keywords = AdditionalKeywordsManager.readAdditionalKeywords(additional_keywords_path);

		// get the tokens based on the programming language
		ArrayList<LibTuple> tokens = null;
		if (programmingLanguageCode.equalsIgnoreCase("py"))
			tokens = LibPythonExtractor.getDefaultTokenString(path, additional_keywords);
		else if (programmingLanguageCode.equalsIgnoreCase("java"))
			tokens = LibJavaExtractor.getDefaultTokenString(path, additional_keywords);

		// get the messages
		ArrayList<ClaritySuggestionTuple> messages = Suggester.getSuggestionMessages(tokens, programmingLanguageCode,
				languageCode, reindex, isTooShortIdentSuggested, isNotDescriptiveIdentSuggested,
				isIncorrectlyWrittenIdentSuggested, isInconsistentTransitionIdentSuggested, isTooShortCommentSuggested,
				isNotDescriptiveCommentSuggested, isIncorrectlyWrittenCommentSuggested,
				isCommentPerSyntaxBlockSuggested);

		// convert all "iden" to "id" given that the html needs to show indonesian text
		if (languageCode.contentEquals("iden"))
			languageCode = "id";

		// get the html template based on given human language
		String targetHTMLPath = "code_clarity_html_template_en.html";
		if (languageCode.equals("id"))
			targetHTMLPath = "code_clarity_html_template_id.html";

		if (isOutText) {
			try {
				FileWriter fw = new FileWriter(new File("out.txt"));
				for (int i = 0; i < messages.size(); i++) {
					fw.write(messages.get(i).toString().replaceAll("\n", System.lineSeparator()));
					fw.write(System.lineSeparator() + System.lineSeparator());
				}
				fw.close();
				System.out.println("The result can be seen in \"out.txt\"");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				generateHtml(tokens, messages, targetHTMLPath, "out.html", languageCode);
				System.out.println("The result can be seen in \"out.html\"");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void generateHtml(ArrayList<LibTuple> tokens, ArrayList<ClaritySuggestionTuple> messages,
			String templateHTMLPath, String outputHTMLPath, String humanLanguage) throws Exception {
		String code = generateCode1(tokens, messages);
		String tablecontent = generateTableContent(messages, humanLanguage);
		String explanation = generateExplanation(messages, humanLanguage);

		File templateFile = new File(templateHTMLPath);
		File outputFile = new File(outputHTMLPath);
		BufferedReader fr = new BufferedReader(new FileReader(templateFile));
		BufferedWriter fw = new BufferedWriter(new FileWriter(outputFile));
		String line;
		while ((line = fr.readLine()) != null) {

			// for default data
			if (line.contains("@code")) {
				line = line.replace("@code", code);
			}
			if (line.contains("@tablecontent")) {
				line = line.replace("@tablecontent", tablecontent);
			}
			if (line.contains("@explanation")) {
				line = line.replace("@explanation", explanation);
			}

			fw.write(line);
			fw.write(System.lineSeparator());
		}
		fr.close();
		fw.close();
	}

	public static String generateExplanation(ArrayList<ClaritySuggestionTuple> messages, String humanLanguage) {
		StringBuffer s = new StringBuffer();
		// add explanation for each fragment
		for (ClaritySuggestionTuple m : messages) {
			// append the string
			s.append("<div class=\"explanationcontent\" id=\"" + m.getVisualId() + "he\">\n\t");
			s.append(m.getMessage(humanLanguage).replaceAll("\n", "<br />").replaceAll("\t",
					"&nbsp;&nbsp;&nbsp;&nbsp;"));
			s.append("\n</div>\n");
		}

		return s.toString();
	}

	public static String generateTableContent(ArrayList<ClaritySuggestionTuple> list, String humanLanguage) {
		String tableId = "origtablecontent";

		StringBuffer s = new StringBuffer();

		// start generating the resulted string
		for (int i = 0; i < list.size(); i++) {
			ClaritySuggestionTuple cur = list.get(i);

			// set the first line
			s.append("<tr id=\"" + cur.getVisualId() + "hr\" onclick=\"markSelectedWithoutChangingTableFocus('"
					+ cur.getVisualId() + "','" + tableId + "')\">");

			/*
			 * Get table ID from visual ID and then aligns it for readability.
			 */
			String visualId = cur.getVisualId();
			// search for the numeric ID part
			int curIdNumPos = 0;
			for (int k = 0; k < visualId.length(); k++) {
				if (Character.isLetter(visualId.charAt(k)) == false) {
					curIdNumPos = k;
					break;
				}
			}
			// merge them together
			String alignedTableID = visualId.toUpperCase().charAt(0) + "";
			int curIdNum = Integer.parseInt(visualId.substring(curIdNumPos));
			if (curIdNum < 10) {
				alignedTableID += "00" + curIdNum;
			} else if (curIdNum < 100) {
				alignedTableID += "0" + curIdNum;
			} else {
				alignedTableID += curIdNum;
			}

			// visualising the rest of the lines
			s.append("\n\t<td><a href=\"#" + cur.getVisualId() + "a\" id=\"" + cur.getVisualId() + "hl\">"
					+ alignedTableID + "</a></td>");

			// hint text
			s.append("\n\t<td style='text-align:left'>" + cur.getHintTokenText().trim() + "</td>");

			// get the text based on human language
			String targetedContent = cur.getTargetedContent();
			String potentialIssue = cur.getPotentialIssue();
			if (humanLanguage.equals("id")) {
				if (targetedContent.equals("comment"))
					targetedContent = "komentar";

				if (potentialIssue.equals("too short"))
					potentialIssue = ("terlalu pendek");
				else if (potentialIssue.equals("not meaningful"))
					potentialIssue = ("tidak bermakna");
				else if (potentialIssue.equals("incorrectly written"))
					potentialIssue = ("tidak tertulis dengan benar");
				else if (potentialIssue.equals("inconsistent transition capitalisation"))
					potentialIssue = ("transisi tak konsisten");
				else if (potentialIssue.equals("inconsistent transition underscore"))
					potentialIssue = ("transisi tak konsisten");
				else if (potentialIssue.equals("not descriptive"))
					potentialIssue = ("tidak deskriptif");
			}

			// targeted content
			s.append("\n\t<td>" + (char) (targetedContent.charAt(0) - 32) + targetedContent.substring(1) + "</td>");
			// potential issue
			if (potentialIssue.startsWith("inconsistent transition"))
				s.append("\n\t<td>Inconsistent transition</td>");
			else
				// this capitalise the first char
				s.append("\n\t<td>" + (char) (potentialIssue.charAt(0) - 32) + potentialIssue.substring(1) + "</td>");

			s.append("\n</tr>\n");
		}

		return s.toString();
	}

	public static String generateCode1(ArrayList<LibTuple> tokenString, ArrayList<ClaritySuggestionTuple> messages) {
		String codeClass = "syntaxsim";

		StringBuffer s = new StringBuffer();

		// starting from the first message, take all the required data
		int matchIdx = 0;
		ClaritySuggestionTuple m = messages.get(matchIdx);
		String visualIdForM = m.getVisualId();
		int targetedIdx = tokenString.indexOf(m.getTargetedToken());

		// for each token from code1
		for (int i = 0; i < tokenString.size(); i++) {
			LibTuple cur = tokenString.get(i);

			// to make sure the code is not wrongly visualised, replace all HTML escape
			// characters
			cur.setRawText(cur.getRawText().replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;"));

			if (i == targetedIdx) {
				// add an opening link tag
				s.append("<a class='" + codeClass + "' id='" + visualIdForM + "a' href=\"#" + visualIdForM
						+ "a\" onclick=\"markSelected('" + visualIdForM + "','origtablecontent')\" >");
				// append the raw text
				s.append(cur.getRawText());
				// add a closing link tag
				s.append("</a>");
				// check for next message if any
				if (matchIdx + 1 < messages.size()) {
					// increment the idx
					matchIdx++;
					// take the new data
					m = messages.get(matchIdx);
					visualIdForM = m.getVisualId();
					targetedIdx = tokenString.indexOf(m.getTargetedToken());
				}
			} else {
				// append the raw text
				s.append(cur.getRawText());
			}
		}
		return s.toString();
	}
}
