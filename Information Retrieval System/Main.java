import java.io.*;
import java.util.*;

public class Main {

	static int queryStart = 683;
	static int queryEnd =696;
	static String queryName ="preprocessedQuery30.txt";
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		long startTime = System.currentTimeMillis();
	
		//Read both preprocessed document and query and place in lists
		List<List<String>> relevance = readRelevance();
		//System.out.println("This is the relevance "+relevance);
		List<List<String>> preprocessedDocumentList = readPPfile();
		//System.out.println("\n" + preprocessedDocumentList + " is the list of preprocessed documents\n");
		List<List<String>> preprocessedQueryList = readPPQueryfile();
		//System.out.println("\n\n" + preprocessedQueryList + " is the list of preprocessed queries\n" );
		
		//Find Unique Words
		List<String> uniqueWords = new ArrayList<String>();
		uniqueWords = removeDuplicatesFromReadyWords(preprocessedDocumentList);
		//System.out.println("Unique Words: " + uniqueWords);
		
		//Word Document TF map
		Map<String, Map<String, Double>> wordToDocumentToTFMap = mapTF(preprocessedDocumentList);
		//System.out.println("\n\nWordToDocumentToTFMap: " + wordToDocumentToTFMap);	
		//Word IDF map
		Map<String, Double> wordToIDFMap = mapIDF(uniqueWords, preprocessedDocumentList);
		//Query TF map
		Map<String, Double> queryToTFMap = mapQueryTF(preprocessedQueryList);
		//Query TFIDF map
		Map<String, Double> queryToTFIDFMap = mapTFIDFofQuery(queryToTFMap, wordToIDFMap);
		//Word Document TFIDF map
		Map<String, Map<String, Double>> wordToDocumentToTFIDFMap = calculateTFIDF(wordToDocumentToTFMap, wordToIDFMap);
		//System.out.println(wordToDocumentToTFIDFMap);
		//Must remove duplicate words from document list before calculating dot product and doc length
		List<List<String>> uniqueWordsDocumentList = removeDuplicatesFromDocumentList(preprocessedDocumentList);
		//System.out.println(uniqueWordsDocumentList);
		//Document DotProduct Map
		Map<String, Double> documentToDotProductMap = dotProductCalculator(uniqueWordsDocumentList, queryToTFIDFMap, wordToDocumentToTFIDFMap);
		//System.out.println("Dot Product: "+documentToDotProductMap);
		//QueryLength Value
		Double queryLength = queryLengthCalculator(queryToTFIDFMap, wordToIDFMap);
		//System.out.println("Query length: "+queryLength);
		//Document to DocumentLength Map
		Map<String, Double> docToDocLengthMap = docToDocLengthMapping(uniqueWordsDocumentList, queryToTFIDFMap, wordToDocumentToTFIDFMap);
		//System.out.println("Document length: "+docToDocLengthMap);
		//Document to CosineSimilarity map
		Map<String, Double> docToCosineMap = docToCosineMapping(documentToDotProductMap, queryLength, docToDocLengthMap);
		//System.out.println("Cosine similarity: "+docToCosineMap);
		//Sort Cosine map (highest to lowest relavance)
		Map<String, Double> sortedMap = sortByComparator(docToCosineMap);
        //System.out.println("Sorted Map, Highest relevance to lowest: "+sortedMap);
        int counter =0;
        int countRel=0;
        String actualRelDoc = null;
        List<String> actualRelevance = new ArrayList<String>();
        int shouldHave = (queryEnd-queryStart)+1;
        System.out.println(sortedMap);
        System.out.println(relevance);
        for(Map.Entry<String, Double> document : sortedMap.entrySet()){
        	
        	
        		//System.out.println(document);
        		counter++;
        	
        	for(int i =queryStart-1;i<queryEnd;i++){
	        	 actualRelDoc = "Document #" +relevance.get(i).get(1);
	        	if(document.getKey().equals(actualRelDoc)){
	        		countRel++;
	        		//System.out.println("The actual relevant documents "+actualRelDoc);
	        		actualRelevance.add(actualRelDoc);
	        		break;
	        	}
        	}
        	
        	//counter++;
        }
        System.out.println("Total returned:" + counter);
        System.out.println("Relevant of those returned" + countRel);
        System.out.println("Total that should be returned" + shouldHave);
        System.out.println(actualRelevance);
        double precision = ((double)countRel/(double)counter)*100;
        double recall = 0.0;
        if(countRel == shouldHave){
        	recall = 100.0;
        }
        else{
        	recall = ((double)countRel/(double)shouldHave)*100;
        }

        System.out.println("Precision: " + String.format( "%.2f", precision) + "% Recall: " + String.format( "%.2f", recall) + "%");
        
       // System.out.println(actualRelevance);
    	long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Time taken to run "+totalTime);
	}
	
	//Read preprocessed Document
	private static List<List<String>> readPPfile() throws FileNotFoundException, IOException{
		
		List<List<String>> List = new ArrayList<List<String>>();
		
		FileReader inputFile = new FileReader("preprocessedDocument.txt");
		BufferedReader bufferedReader = new BufferedReader(inputFile);
		Scanner scanner = new Scanner(bufferedReader);
		String nextWord = null;

		while (scanner.hasNext()) {
			nextWord = scanner.next();
			
			if (nextWord.equalsIgnoreCase("DOC.START")) { //Start of next document
				ArrayList<String> docWords = new ArrayList<String>();
				
				while (scanner.hasNext()) {
					nextWord = scanner.next();
					docWords.add(nextWord);
					
					if (nextWord.equalsIgnoreCase("DOC.END")) {
						docWords.remove("DOC.END");
						List.add(docWords);
						break;
					}
				}
			}
		}
		bufferedReader.close();
		return List;
	}
	
	//Read preprocessed Query
	private static List<List<String>> readPPQueryfile() throws FileNotFoundException, IOException{
			
		List<List<String>> List = new ArrayList<List<String>>();
		
		FileReader inputFile = new FileReader(queryName);
		BufferedReader bufferedReader = new BufferedReader(inputFile);
		Scanner scanner = new Scanner(bufferedReader);
		String nextWord = null;

		while (scanner.hasNext()) {
			nextWord = scanner.next();
			
			if (nextWord.equalsIgnoreCase("QUERY.START")) { //Start of next document
				ArrayList<String> queryWords = new ArrayList<String>();
				
				while (scanner.hasNext()) {
					nextWord = scanner.next();
					queryWords.add(nextWord);
					
					if (nextWord.equalsIgnoreCase("QUERY.END")) {
						queryWords.remove("QUERY.END");
						List.add(queryWords);
						break;
					}
				}
			}
		}
		bufferedReader.close();
		return List;
	}
	
	//Unique words
	private static List<String> removeDuplicatesFromReadyWords(List<List<String>> preprocessedDocumentList) {
		List<String> uniqueWords = new ArrayList<String>(); 
		
		for (List<String> doc : preprocessedDocumentList) {
			for(String eachWord : doc){
				if (!uniqueWords.contains(eachWord)) { 
	            	uniqueWords.add(eachWord);
				}
			}
		}
		return uniqueWords;
	}
	
	//Map TF
	private static Map<String, Map<String, Double>> mapTF(List<List<String>> preprocessedDocumentList) {
		Map<String, Map<String, Double>> wordToDocumentToTFMap = new HashMap<String, Map<String, Double>>();
		Map<String, Double> documentToTFMap = new HashMap<String, Double>();
	
		for(int x = 0; x<preprocessedDocumentList.size(); x++){
		
			LinkedList<String> wordsInThisDoc = new LinkedList<String>();
			for(String eachWord : preprocessedDocumentList.get(x)){
				wordsInThisDoc.add(eachWord);
			}
		
			String currentDocName = "Document #" + (x+1);
		
			for(String eachWord : wordsInThisDoc){
				documentToTFMap = wordToDocumentToTFMap.get(eachWord); // Gets Doc
			
				if (documentToTFMap == null) { 
					documentToTFMap = new HashMap<String, Double>();
					wordToDocumentToTFMap.put(eachWord, documentToTFMap);
				}
				Double currentCount = documentToTFMap.get(currentDocName);
				if (currentCount == null) {
					currentCount = 0.0;
				}
				documentToTFMap.put("Document #" + (x+1) , currentCount + 1.0);		
			}	
		} return wordToDocumentToTFMap;	
	}
	
	// Map IDF
	private static Map<String, Double> mapIDF(List<String> uniqueWords, List<List<String>> preprocessedDocumentList) {
		Map<String, Double> wordToIDFMap = new HashMap<String, Double>();
		double idf;	
		
		for(String word : uniqueWords){
			double count = 0;
			for(List<String> document : preprocessedDocumentList){
				for(String string : document){
					if(string.equalsIgnoreCase(word)){
						count++;
						break;
					}
				}
			}
			idf = Math.log10(preprocessedDocumentList.size() / count);
			wordToIDFMap.put(word, idf);
		}
		//System.out.println("\n\nWordToIDFMap " + wordToIDFMap); 
		return wordToIDFMap;
	}
	
	
	// Map Query TF
	private static Map<String, Double> mapQueryTF(List<List<String>> preprocessedQueryList) {
		Map<String, Double> queryToTFMap = new HashMap<String, Double>();
		
		List<String> wordsInQueries = new ArrayList<String>();
		
		for(int x = 0; x<preprocessedQueryList.size(); x++){
			for(String eachWord : preprocessedQueryList.get(x)){
				wordsInQueries.add(eachWord);
			}
		}
		//System.out.println(wordsInQueries + " is the list of all query terms");
		
		for(String queryReadyWord : wordsInQueries) {
			double count = 0;
			for(String string : wordsInQueries){
				if(string.equalsIgnoreCase(queryReadyWord)){
					count++;
				}
				queryToTFMap.put(queryReadyWord, count);
			}
		}
		//System.out.println("\nQueryToTFMap " + queryToTFMap + "\n");
		return queryToTFMap;
	}
	
	//TFIDF
	private static Map<String, Map<String, Double>> calculateTFIDF(Map<String, Map<String, Double>> wordToDocumentToTFMap, Map<String, Double> wordToIDFMap) {

		Map<String, Map<String, Double>> wordToDocumentToTFIDFMap = new HashMap<String, Map<String, Double>>();
		
		for (Map.Entry<String, Double> IDFofWord : wordToIDFMap.entrySet()) {
			String currentWord = IDFofWord.getKey();
			Double IDF = IDFofWord.getValue();		
			Map<String, Double> documentToTFIDFMap = new HashMap<String, Double>();

				Map<String, Double> DocTF = wordToDocumentToTFMap.get(currentWord);
				
				for (Map.Entry<String, Double> Doc : DocTF.entrySet()) {
					String document = Doc.getKey();
					Double TF = Doc.getValue();
					Double TFIDF = TF*IDF;
					
					
					documentToTFIDFMap.put(document, TFIDF);
					wordToDocumentToTFIDFMap.put(currentWord, documentToTFIDFMap);
				}
			}
		return wordToDocumentToTFIDFMap;
		}
	
	
	// Map Query TFIDF
	private static Map<String, Double> mapTFIDFofQuery(Map<String, Double> queryToTFMap, Map<String, Double> wordToIDFMap) {
		Map<String, Double> queryToTFIDFMap = new HashMap<String, Double>();
		
		for(Map.Entry<String, Double> TFofWord : queryToTFMap.entrySet()) {
			String currentWord = TFofWord.getKey();
			Double TF = TFofWord.getValue();
				
			Double IDF = wordToIDFMap.get(currentWord);
			if(IDF!=null){
			Double TFIDF = TF*IDF;
			queryToTFIDFMap.put(currentWord, TFIDF);
			}
			else{
				queryToTFIDFMap.put(currentWord, 0.0);
				}
			//System.out.println("\nQuery word: " + currentWord + " TFIDF = " + TFIDF);
		}
		//System.out.println("\nQueryToTFIDFMap " + queryToTFIDFMap + "\n\n");
		return queryToTFIDFMap;
	}
	
	//Remove duplicates from document list for use in doc product and doc length functions
	private static List<List<String>> removeDuplicatesFromDocumentList(List<List<String>> preprocessedDocumentList){
		List<List<String>> uniqueDocumentList = new ArrayList<List<String>>();
		
		for (List<String> doc : preprocessedDocumentList) {
			List<String> uniqueWords = new ArrayList<String>(); // readyWords no duplicates
			for(String eachWord : doc){
				if (!uniqueWords.contains(eachWord)) {  // avoid duplicate entry
	            	uniqueWords.add(eachWord);
				}
			}
			uniqueDocumentList.add(uniqueWords);
		}
		return uniqueDocumentList;
	}
	
	//Dot Product
	private static Map<String, Double> dotProductCalculator(List<List<String>> uniqueWordsDocumentList, Map<String, Double> queryToTFIDFMap, Map<String, Map<String, Double>> wordToDocumentToTFIDFMap) {

		Map<String, Double> documentToDotMap = new HashMap<String, Double>();
		int x = 0;
		for (List<String>document : uniqueWordsDocumentList) {
			double dotProduct=0;
			double sum=0;
			String docName = "Document #" + (x+1);
			for (String currentWord : document) {
		
		for (Map.Entry<String, Double> queTFIDFofWord : queryToTFIDFMap.entrySet()) {
			String queCurrentWord = queTFIDFofWord.getKey();
			Double queTFIDF = queTFIDFofWord.getValue();
			
			if (currentWord.equalsIgnoreCase(queCurrentWord)){
				Map<String, Double> docToTFIDF = wordToDocumentToTFIDFMap.get(queCurrentWord);
				Double docWordTFIDF = docToTFIDF.get(docName);
					dotProduct = queTFIDF * docWordTFIDF;
					sum += dotProduct;
			}
		}
			}
			documentToDotMap.put(docName, sum);
			x++;
		}
		return documentToDotMap;
	}
	
	//Query length (TFIDF values squared, summed and sqrt)
	private static Double queryLengthCalculator(Map<String, Double> queryToTFIDFMap, Map<String, Double> wordToIDFMap){
		Double length = 0.0;
		
		for(Map.Entry<String, Double> TFIDFofWord : queryToTFIDFMap.entrySet()) {
			Double TFIDF = TFIDFofWord.getValue();			
			length += (TFIDF*TFIDF);
		}
		return Math.sqrt(length);
	}
	
	//Doc length map
	private static Map<String, Double> docToDocLengthMapping(List<List<String>> uniqueWordsDocumentList, Map<String, Double> queryToTFIDFMap, Map<String, Map<String, Double>> wordToDocumentToTFIDFMap){
		Map<String, Double> docToDocLengthMap = new HashMap<String, Double>();
		int x = 0;
		for (List<String>document : uniqueWordsDocumentList) {
			double docLength=0;
			String docName = "Document #" + (x+1);
			for (String currentWord : document) {
		
		for (Map.Entry<String, Double> queTFIDFofWord : queryToTFIDFMap.entrySet()) {
			String queCurrentWord = queTFIDFofWord.getKey();
			
			if (currentWord.equalsIgnoreCase(queCurrentWord)){
				Map<String, Double> docToTFIDF = wordToDocumentToTFIDFMap.get(queCurrentWord);
				Double docWordTFIDF = docToTFIDF.get(docName);					
					docLength += (docWordTFIDF*docWordTFIDF);
			}
		}
		}
			docLength = Math.sqrt(docLength);
			docToDocLengthMap.put(docName, docLength);
			x++;
		}
		return docToDocLengthMap;
	}
	
	//Cosine similarity
	private static Map<String, Double> docToCosineMapping(Map<String, Double> documentToDotProductMap, Double queryLength, Map<String, Double> docToDocLengthMap){
		Map<String, Double> docToCosineMap = new HashMap<String, Double>();
		
		for (Map.Entry<String, Double> documentDotProduct : documentToDotProductMap.entrySet()) {
			String currentDoc = documentDotProduct.getKey();
			Double dotProduct = documentDotProduct.getValue();
			
			Double documentLength = docToDocLengthMap.get(currentDoc);
			if(dotProduct!=0.0 && documentLength!=0.0){
			Double cosineSimilarity = (dotProduct/(queryLength*documentLength));
			if(cosineSimilarity>0.0){ 
			docToCosineMap.put(currentDoc, cosineSimilarity);
			}
			}
		}
		return docToCosineMap;
	}
	
	//Sort the cosine map
	private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {

		// Convert Map to List
		List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted list back to a Map
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	private static List<List<String>> readRelevance() {
		List<List<String>> relevance = new ArrayList<List<String>>();
		try {
			String line;
			FileReader file = new FileReader("MED.REL.OLD");
			BufferedReader buffer = new BufferedReader(file);

			while ((line = buffer.readLine()) != null) {
				relevance.add(relevancetoArrayList(line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return relevance;
	}

	private static ArrayList<String> relevancetoArrayList(String line) {
		ArrayList<String> list = new ArrayList<String>();

		String[] columns = line.split("\\s+");
		for (int i = 0; i < 2; i++) {
			if ((columns[i] != null)) {
				list.add(columns[i]);
			}
		}
		return list;
	}
}
	



	
	
