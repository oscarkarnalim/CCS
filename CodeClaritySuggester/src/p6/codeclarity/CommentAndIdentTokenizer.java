package p6.codeclarity;

import java.util.ArrayList;

public class CommentAndIdentTokenizer {
	/*
	 * copied from the code used in 'Programming Style On Source Code Plagiarism And
	 * Collusion Detection'. The idea for identifier splitting is based on 'Java
	 * archives search engine using byte code as information source'.
	 */
	public static ArrayList<String> tokenizeComment(String sentence) {
		ArrayList<String> tokens = new ArrayList<String>();
		// split by nonalphanumeric
		String[] tokenstemp = sentence.split("[^A-Za-z0-9]");
		// remove empty element
		for (int i = 0; i < tokenstemp.length; i++) {
			if (tokenstemp[i].length() > 0)
				tokens.addAll(tokenizeIdentifier(tokenstemp[i]));
		}
		return tokens;
	}

	public static ArrayList<String> tokenizeIdentifier(String ident) {
		/*
		 * Method ini berfungsi untuk melakukan tokenisasi dan mengembalikan kumpulan
		 * term pada identifier. Metoda tokenisasi : Hanya mengambil karakter
		 * alphanumeric dimana transisi karakter kapital, biasa, dan angka atau _ akan
		 * berperan sebagai separator. Karakter kapital akan tetap disertakan dengan
		 * format lowercase sebagai karakter awal term berikutnya. Term harus minimal
		 * berisi satu karakter.
		 */
		ArrayList<String> output = new ArrayList<String>();
		String tempTerm = "";
		/*
		 * lastType merupakan tipe karakter sebelumnya. 0 merupakan karakter biasa, 1
		 * kapital, 2 angka
		 */
		int lastType = -1;
		for (int i = 0; i < ident.length(); i++) {
			char c = ident.charAt(i);
			if (c >= 'a' && c <= 'z') {
				/*
				 * Jika berbeda tipe dan jumlah karakter lebih besar dari 1, lakukan proses
				 * pemotongan.
				 */
				if (lastType != 0) {
					if (lastType == 1) {
						/*
						 * Jika karakter sebelumnya kapital, tambahkan substring dari tempterm tampa
						 * melibatkan karakter terakhir, set tempterm dengan karakter terakhir yang
						 * dilowercase
						 */
						// ambil semua karakter awal
						String tempTerm2 = tempTerm.substring(0, tempTerm.length() - 1);
						// tambahkan dalam list
						if (tempTerm2.length() > 0)
							output.add(tempTerm2);
						// set dengan karakter pertama
						tempTerm = tempTerm.charAt(tempTerm.length() - 1) + "";
					} else {
						if (tempTerm.length() > 0)
							output.add(tempTerm);
						tempTerm = "";
					}
				}
				tempTerm += c;
				lastType = 0;
			} else if (c >= '0' && c <= '9') {
				if (lastType != 2) {
					if (tempTerm.length() > 0)
						output.add(tempTerm);
					tempTerm = "";
				}
				tempTerm += c;
				lastType = 2;
			} else if (c >= 'A' && c <= 'Z') {
				/*
				 * jika karakter sebelumnya bukan kapital, tambahkan dulu string tersebut dalam
				 * termList. kasus osCar jadi os dan car
				 */
				if (lastType != 1) {
					if (tempTerm.length() > 0)
						output.add(tempTerm);
					tempTerm = "";
				}
				c += 32; // to lower case
				tempTerm += c;
				lastType = 1;
			} else {
				if (tempTerm.length() > 0)
					output.add(tempTerm);
				tempTerm = "";
				lastType = -1;
			}
		}
		if (tempTerm.length() > 0)
			output.add(tempTerm);

		return output;
	}
}
