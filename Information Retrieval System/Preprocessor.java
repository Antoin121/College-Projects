import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Preprocessor {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		FileReader inputFile = new FileReader("MED.ALL");
		BufferedReader bufferedReader = new BufferedReader(inputFile);
		Scanner scanner = new Scanner(bufferedReader);
		String nextWord = null;
		
		List<List<String>> documentList = new ArrayList<List<String>>();

		Porter porter = new Porter();
		String stopWordsDoc = "stopWords.txt";

		while (scanner.hasNext()) {
			nextWord = scanner.next();
			
			if (nextWord.equalsIgnoreCase(".W")) { //Start of next document
				ArrayList<String> docWords = new ArrayList<String>();
				
				while (scanner.hasNext()) {
					nextWord = scanner.next();
					docWords.add(nextWord); // of document in this loop
					
					if (nextWord.equalsIgnoreCase(".I")) {
						documentList.add(docWords); // DOCUMENT LIST DOESNT CLEAR
						break;
					}
				}
			} // End of first if statement
		}
		
		bufferedReader.close();
		
		ArrayList<String> stopWords = stopWordFileReader(stopWordsDoc); // arrayList of stopWords 
		ArrayList<String> readyForStopWordRemoval = new ArrayList<String>();
		ArrayList<String> stemmedWords = new ArrayList<String>();
		ArrayList<String> stemmedStopWords = new ArrayList<String>();
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("preprocessedDocument.txt")));

		for (String stopWord : stopWords) {
			stemmedStopWords.add(porter.stripAffixes(stopWord));
		}
		
		for (List<String> document : documentList) {
			out.write("DOC.START "); // Start of each document
			
			for (String docWord : document) {
				readyForStopWordRemoval.add(docWord);
			}
			//readyForStopWordRemoval.removeAll(stopWords);
			
			for (String word : readyForStopWordRemoval) {
				stemmedWords.add(porter.stripAffixes(word));
			}
			
			stemmedWords.removeAll(stemmedStopWords);
			
				
			for (String word : stemmedWords) {
				out.write(word + " ");
			}
			out.write("DOC.END "); // End of each document
			stemmedWords.clear();
			readyForStopWordRemoval.clear();
		}
		out.close();
		System.out.println("Done");
	}

	public static ArrayList<String> stopWordFileReader(String fileNameStopWord) throws FileNotFoundException, IOException {

		FileReader inputFile = new FileReader(fileNameStopWord);
		BufferedReader bufferedReader = new BufferedReader(inputFile);

		ArrayList<String> lines = new ArrayList<String>();
		String line = null;

		while ((line = bufferedReader.readLine()) != null) {
			lines.add(line);
		}

		bufferedReader.close();

		return lines;
	}
}
