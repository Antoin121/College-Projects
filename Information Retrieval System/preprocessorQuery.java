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

public class preprocessorQuery {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		FileReader inputFile = new FileReader("MED.QRY");
		BufferedReader bufferedReader = new BufferedReader(inputFile);
		Scanner scanner = new Scanner(bufferedReader);
		String nextWord = null;
		
		List<List<String>> queryList = new ArrayList<List<String>>();

		Porter porter = new Porter();
		String stopWordsDoc = "stopWords.txt";

		while (scanner.hasNext()) {
			nextWord = scanner.next();
			
			if (nextWord.equalsIgnoreCase(".W")) { //Start of next document
				ArrayList<String> queryWords = new ArrayList<String>();
				
				while (scanner.hasNext()) {
					nextWord = scanner.next();
					queryWords.add(nextWord); // of document in this loop
					
					if (nextWord.equalsIgnoreCase(".I")) {
						queryList.add(queryWords);
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
		
		
		int x=1;
		for (List<String> query : queryList) {
			String fileName = "preprocessedQuery" + (x)+".txt";
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
			out.write("QUERY.START "); // Start of each document
			
			for (String queryWord : query) {
				readyForStopWordRemoval.add(queryWord);
			}
			//readyForStopWordRemoval.removeAll(stopWords);
			
			for (String word : readyForStopWordRemoval) {
				stemmedWords.add(porter.stripAffixes(word));
			}
			
			for (String stopWord : stopWords) {
				stemmedStopWords.add(porter.stripAffixes(stopWord));
			}
			
			stemmedWords.removeAll(stemmedStopWords);
			
			for (String word : stemmedWords) {
				out.write(word + " ");
			}
			out.write("QUERY.END "); // End of each document
			stemmedWords.clear();
			readyForStopWordRemoval.clear();
			out.close();
			x++;
		}
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
