package p6.codeclarity;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SpellCheckerManager {
	// object responsible for spell checking
	private SpellChecker spellchecker;
	private String languageCode;

	public SpellCheckerManager(String languageCode, boolean reindex) {
		// set the language code
		this.languageCode = languageCode;

		// create the spell check object based on lucene_index
		try {
			File file = new File("lucene_index");
			Directory directory = FSDirectory.open(file.toPath());
			spellchecker = new SpellChecker(directory);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (reindex) {
			if (languageCode.equals("en")) {
				setSpellCheckToAnotherLanguage(
						new String[] { "american_english_lowercased_dict.txt", "british_english_lowercased_dict.txt" });
			} else if (languageCode.equals("id")) {
				setSpellCheckToAnotherLanguage(new String[] { "indonesian_lowercased_dict.txt" });
			} else if (languageCode.equals("iden")) {
				setSpellCheckToAnotherLanguage(new String[] { "indonesian_lowercased_dict.txt",
						"american_english_lowercased_dict.txt", "british_english_lowercased_dict.txt" });
			}
		}
	}

	public void terminate() {
		try {
			spellchecker.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setSpellCheckToAnotherLanguage(String[] wordFilePaths) {
		// set the database to other language. The text file should be formatted as
		// Lucene's plain text dictionary format (one word per line)

		try {
			StandardAnalyzer analyzer = new StandardAnalyzer();
			spellchecker.clearIndex();
			for (String wordFilePath : wordFilePaths) {
				IndexWriterConfig config = new IndexWriterConfig(analyzer);
				spellchecker.indexDictionary(new PlainTextDictionary(new File(wordFilePath).toPath()), config, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String suggest(String word) {
		// given a word, return a new suggested word if that word is not valid. The word
		// should at least 3 chars long

		String out = null;

		// skip 'args' as it is common for programmers
		if (word.equals("args"))
			return out;

		try {
			// somehow the suggester only lists words with length higher than 2 chars
			if (word.length() > 2 && spellchecker.exist(word) == false) {
				String[] suggested = spellchecker.suggestSimilar(word, 1);
				if (suggested.length > 0)
					out = suggested[0];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return out;
	}

	public String getLanguageCode() {
		return languageCode;
	}

}
