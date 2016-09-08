
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
 
public class Main {
	
	public static void main(String[] args) {
		
		double learningRate=0.01;
		int maxLoops=4000;
		
		
		List<List<String>> owlData = readCSV();
		System.out.println("The owlData after it is read in "+owlData+ "\n");
		int numColumns = numColumns(owlData.get(1));
		int trainingSize = ((owlData.size()/3)*2);
		int[] correctTotal = new int[10];
		int sum=0;
		double percentage, averageCorrect;
		
		//double min = min(owlData);
		//double max = max(owlData,numColumns);
		//owlData= normalization(owlData,numColumns,min, max);
		
		//System.out.println("Min =  "+min+" Max = " + max);

		// adding the extra columns for the oneVsAll and storing the index of each column
		oneVsAll(owlData,"LongEaredOwl" ,numColumns);
		int longVsAll = numColumns;
		System.out.println(owlData);
		oneVsAll(owlData, "SnowyOwl" ,numColumns);
		int snowyVsAll = numColumns+1;
		System.out.println(owlData);
		oneVsAll(owlData, "BarnOwl" ,numColumns);
		int barnVsAll = numColumns+2;
		System.out.println(owlData);
		
		for(int i=0;i<10;i++){
			Collections.shuffle(owlData); // randomizing the data
			List<List<String>> trainingData = splitData(owlData,0,trainingSize);// splitting the training data
			List<List<String>> testData =  splitData(owlData, trainingSize, owlData.size()); // splitting the test data
			
			// generating the three different oneVsAll weights 
			double [] longWeights = training(learningRate,maxLoops, trainingData, longVsAll, numColumns);
			double [] snowyWeights = training(learningRate,maxLoops, trainingData, snowyVsAll,numColumns);
			double [] barnWeights = training(learningRate,maxLoops, trainingData, barnVsAll,numColumns);
			
			int correct;
				// testing the 3 weights on the validation dataset
				correct = testing(longWeights,snowyWeights,barnWeights, testData, numColumns,i);
				correctTotal[i]=correct;
				
		
		}
		// printing out the number correct and percentage for each fold and overall averages
		 sum=0;
		 int foldNum;
		 double percentFold;
		for(int i=0;i<10;i++){
			foldNum=i+1;
			percentFold = ((double)correctTotal[i]/(double)(owlData.size()/3))*100;
			System.out.println("Fold"+foldNum+": Correctly predicted= " +correctTotal[i] +"\tAccuracy= " + percentFold);
			sum+=correctTotal[i];
		}
		averageCorrect=sum/10;
		percentage = ((double)averageCorrect/(double)(owlData.size()/3))*100;
		System.out.println("Average number correct= "+averageCorrect);
		System.out.println("Average accuracy= "+ percentage);

	}
	
	public static List<List<String>> readCSV() {
		List<List<String>> owlData = new ArrayList<List<String>>();
		try {
			String line;
			FileReader file = new FileReader("owls15.csv");
			BufferedReader buffer = new BufferedReader(file);// reading in the csv file

			while ((line = buffer.readLine()) != null) {
				owlData.add(CSVtoArrayList(line));//adding the returned list to the list within a list
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return owlData;
	}
	
	
	public static ArrayList<String> CSVtoArrayList(String line) {
		ArrayList<String> list = new ArrayList<String>();
		
			String[] columns = line.split(",");// splitting by ,
			for (int i = 0; i < columns.length; i++) {
				if ((columns[i] != null)) { 
					list.add(columns[i]); // adding everything in the column to the a list 
				}
			}
		return list;
	}
	
	public static int numColumns(List<String> aRow) { // counting the number of columns in the dataset
		int size=0;
		for(String column : aRow){
			size++;
		}
		return size;
	}
	
	public static double[] weightCal(int numColumns) //randomly generating weights between 0 and 1
	{
		int min=0; 
		int max=1;
		
		double weights[] = new double[numColumns]; // weights array size = the number of columns
		for(int i=0;i<weights.length;i++){
			weights[i]= min + Math.random() * (max-min);
		}
		return weights;
	}
	
	
	
	public static double prediction(double weights[],List<String> owl)
	{
		double sum=0;
		for(int i=0; i<weights.length-2;i++){
			sum+=weights[i]*Double.parseDouble(owl.get(i)); // summing together the multiple of the weight by the aligning variable 
			
		}
	       sum+=weights[weights.length-1]; // adding the bias to the sum
	       return sum;
	}
	
	public static double[] updateWeights(double weights[],double learningRate,double error,List<String> owl)
	{
	
		for(int i=0;i<weights.length-2;i++){
			weights[i]+= learningRate* error *Double.parseDouble(owl.get(i)); // updating the weights by the learning rate and the error multiplied by the variable 
		}
	       weights[weights.length-1]+=learningRate* error;
	       return weights;
	   
	}

	public static double[] training(double learningRate,int maxLoops, List<List<String>> trainingData, int owl, int numColumn)
	{
	int numLoops=0;
	int predictionInt;
	double error,totalError, prediction;
	double [] weights = weightCal(numColumn);

	
	for (int x = 0; x <= maxLoops; x++) {
			totalError = 0;
			for(int i=0;i<trainingData.size()-1;i++){
				prediction= prediction(weights, trainingData.get(i)); // calculating the prediction
				predictionInt = (prediction>=0)?1:0; // changing the prection to 1 or 0 to compare to the actual output
				
				error = Integer.parseInt(trainingData.get(i).get(owl)) - predictionInt; // taking the predicted away from the actual
				
				weights = updateWeights(weights,learningRate,error, trainingData.get(i)); // updating the weights
				
				totalError += (error*error); // summed RMSE
			}
			if (totalError == 0) { // breaking out of the loop if the total RMSE = 0
				break;
			}
		}
		
		return weights; // returning the weights vector
	}
	
	public static int testing(double [] weightsLong,double [] weightsSnowy,double [] weightsBarn, List<List<String>> testData, int numColumns,int numfolds) 
	{
		double outputLong,outputSnowy,outputBarn;
		String prediction=null;
		int correct=0;
		numfolds++;
		
		for(int i=0;i<testData.size();i++){
			// for each owl calculating what sum each of the 3 weight vectors gives
		outputLong= prediction(weightsLong, testData.get(i));
		outputSnowy= prediction(weightsSnowy, testData.get(i));
		outputBarn= prediction(weightsBarn, testData.get(i));
		
		ArrayList<Double> outputs = new ArrayList<Double>();
		outputs.add(outputLong);
		outputs.add(outputSnowy);
		outputs.add(outputBarn);
		double max = Collections.max(outputs); // getting the max sum
		
		if(max==outputLong){ // setting what the prediction is going by the max sum
			
			prediction="LongEaredOwl";
		}
		
		else if(max==outputSnowy){
			
			prediction="SnowyOwl";
		}
		
		else if(max==outputBarn){
			
			prediction="BarnOwl";
		}
		
		if(testData.get(i).get(numColumns-1).equals(prediction)){ // counting the correct amount
			correct++;
			//System.out.println("A right prediction: Actual " + testData.get(i).get(numColumns-1)+ " Prediction " + prediction);
		}
		
		
		else{ // printing out what ones it gets wrong
			System.out.println("A wrong prediction: Actual " + testData.get(i).get(numColumns-1)+"\t"+ " Prediction " + prediction);
		}
		
	}
		System.out.println("End of fold " + numfolds);
		return correct;
	}
	
	public static void oneVsAll(List<List<String>> owlData, String s ,int numColumns) // takes in a string to compare to the class
	{
		for(int i=0; i<owlData.size();i++){
			if(owlData.get(i).get(numColumns-1).equals(s)){ // adding the one vs all column 

				owlData.get(i).add("1");	
			}
			else if(!(owlData.get(i).get(numColumns-1).equals(s))){
				owlData.get(i).add("0");
				
			}	
		}
	}
	
	public static List<List<String>> splitData(List<List<String>> owlData,int start, int end) 
	{
		List<List<String>> data = new ArrayList<List<String>>();
		
		for(int i =start;i<end;i++){
			data.add(owlData.get(i));
		}
		return data;
	}
	
	// below is the normalization code I wrote but it takes too long to run the code so I left it out and just modified the data in .csv file
	
	
//	public static double min(List<List<String>> owlData) {
//	String minOld = Collections.min(owlData.get(0));
//	String minNew;
//	
//	for(int i =1; i<owlData.size();i++){
//		minNew = Collections.min(owlData.get(i));
//		if(Double.parseDouble(minNew)<Double.parseDouble(minOld)){
//			minOld=minNew;
//		}
//	
//	}
//	return Double.parseDouble(minOld);
//}
//
//public static double max(List<List<String>> owlData, int numColumns) {
//	String maxOld = owlData.get(0).get(0);
//	String maxNew;
//	
//	for(int i =0; i<owlData.size();i++){
//		for(int x=0;x<numColumns-1;x++){
//			maxNew = owlData.get(i).get(x);
//			if(Double.parseDouble(maxNew)>Double.parseDouble(maxOld)){
//			maxOld=maxNew;
//			}
//		}
//	}
//	return Double.parseDouble(maxOld);
//}
//
//public static List<List<String>> normalization(List<List<String>> owlData, int numColumns, double min, double max) {
//	
//	double value;
//	double normValue;
//	String normVal;
//	List<List<String>> normData = new ArrayList<List<String>>();
//	List<String> normOwl = new ArrayList<String>();
//	
//	for(int i =0; i<owlData.size();i++){
//		for(int x=0;x<numColumns-1;x++){
//			value = Double.parseDouble(owlData.get(i).get(x));
//			normValue= (value-min)/(max-min);
//			
//			normVal = Double.toString(normValue);
//			
//			normOwl.add(normVal);
//		
//			}
//			normOwl.add(owlData.get(i).get(numColumns-1));
//			normData.add(normOwl);
//		}
//	return normData;
//}
	
}