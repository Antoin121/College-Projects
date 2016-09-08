package org.mapreduce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;





public class MapReduce {
        
	 public static void main(String[] args) throws InterruptedException {

	    	// get number of files to be reduced from the user
//	        Scanner reader = new Scanner(System.in);
//	        System.out.println("Enter the number of files in database:");
//	        int numfiles = reader.nextInt();
	        int poolSize = 3;
//
//
//	        String[] xfiles = new String[numfiles];
//	        for(int x=0; x<xfiles.length;x++) {
//	            System.out.println("Enter the filenames one by one and press enter");
//	            xfiles[x] = reader.next();
//	        }
		 
		 String[] xfiles = {"file1.txt","file2.txt","file3.txt"};
		 
	        // create an input with the name of a file and a String
	        // containing every word in the file
	        Map<String, String> input = new HashMap<String, String>();
	        Map<String, String> inputConcurrent = new ConcurrentHashMap<String, String>();
	        // get names of the files to search from the program arguments
	        for(String fileName:xfiles) {
	            String everything = "";
	            try {
	                // create a buffered reader and read each line of the file
	                BufferedReader br = new BufferedReader(new FileReader(fileName));
	                // for concatenating multiple strings in a loop
	                // much faster than String object in loop concatenation
	                StringBuilder sb = new StringBuilder();
	                // read a line from the file
	                String line = br.readLine();

	                // if the line of the file contains text
	                while (line != null) {
	                    // add the line of the text to the string builder
	                    sb.append(line);
	                    // add line separator after each line
	                    // this ensures words at the beginning and end of new lines
	                    // do not concatenate as one
	                    sb.append(System.lineSeparator());
	                    // read the next line of the file
	                    line = br.readLine();
	                }
	                // convert the string builder to a String containing
	                // the entire file
	                everything = sb.toString();
	                br.close();
	            }
	            catch (Exception e) {
	                e.printStackTrace();
	            }
	            // add the words to the Hash map
	            // contains 3 entries, one for each file
	            input.put(fileName,everything);
	            inputConcurrent.put(fileName,everything);
	        }
                
                
                        // APPROACH #1: Brute force
                {
                        Map<String, Map<String, Integer>> outputBrute = new HashMap<String, Map<String, Integer>>();
                        
                        Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
                        long startTime = System.nanoTime();
                        while(inputIter.hasNext()) {
                                Map.Entry<String, String> entry = inputIter.next();
                                String file = entry.getKey();
                                String contents = entry.getValue();
                                
                                String[] words = contents.trim().split("\\s+");
                                
                                for(String word : words) {
                                        
                                        Map<String, Integer> files = outputBrute.get(word);
                                        if (files == null) {
                                                files = new HashMap<String, Integer>();
                                                outputBrute.put(word, files);
                                        }
                                        
                                        Integer occurrences = files.remove(file);
                                        if (occurrences == null) {
                                                files.put(file, 1);
                                        } else {
                                                files.put(file, occurrences.intValue() + 1);
                                        }
                                }
                        }
                        long endTime = System.nanoTime();

                        long duration = (endTime - startTime);
                        double seconds = (double)duration / 1000000000.0;
                        // show me:
                        System.out.println("Brute Force Output "+outputBrute);
                        System.out.println("Brute force took "+seconds);
                }

                
                // APPROACH #2: MapReduce
                {
                        Map<String, Map<String, Integer>> outputApr2 = new HashMap<String, Map<String, Integer>>();
                        
                        // MAP:
                        
                        List<MappedItem> mappedItems = new LinkedList<MappedItem>();
                        
                        Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
                        long startTime = System.nanoTime();
                        while(inputIter.hasNext()) {
                                Map.Entry<String, String> entry = inputIter.next();
                                String file = entry.getKey();
                                String contents = entry.getValue();
                                
                                map(file, contents, mappedItems);
                        }
                        
                        // GROUP:
                        
                        Map<String, List<String>> groupedItems = new HashMap<String, List<String>>();
                        
                        Iterator<MappedItem> mappedIter = mappedItems.iterator();
                        while(mappedIter.hasNext()) {
                                MappedItem item = mappedIter.next();
                                String word = item.getWord();
                                String file = item.getFile();
                                List<String> list = groupedItems.get(word);
                                if (list == null) {
                                        list = new LinkedList<String>();
                                        groupedItems.put(word, list);
                                }
                                list.add(file);
                        }
                        
                        // REDUCE:
                        
                        Iterator<Map.Entry<String, List<String>>> groupedIter = groupedItems.entrySet().iterator();
                        while(groupedIter.hasNext()) {
                                Map.Entry<String, List<String>> entry = groupedIter.next();
                                String word = entry.getKey();
                                List<String> list = entry.getValue();
                                
                                reduce(word, list, outputApr2);
                        }
                        long endTime = System.nanoTime();

                        long duration = (endTime - startTime);
                        double seconds = (double)duration / 1000000000.0;
                        System.out.println("Aprroach 2 output "+outputApr2);
                        System.out.println("MapReduce took "+seconds);
                }
                
                
                // APPROACH #3: Distributed MapReduce
                {
                        final Map<String, Map<String, Integer>> outputApr3 = new HashMap<String, Map<String, Integer>>();
                        
                        // MAP:
                        
                        final List<MappedItem> mappedItems = new LinkedList<MappedItem>();
                        
                        final MapCallback<String, MappedItem> mapCallback = new MapCallback<String, MappedItem>() {
                                @Override
                public synchronized void mapDone(String file, List<MappedItem> results) {
                    mappedItems.addAll(results);
                }
                        };
                        
                       // List<Thread> mapCluster = new ArrayList<Thread>(input.size());
                        
                        Iterator<Map.Entry<String, String>> inputIter = input.entrySet().iterator();
                        ExecutorService mapPool = Executors.newFixedThreadPool(poolSize);
                        
                        long startTime = System.nanoTime();
                        while(inputIter.hasNext()) {
                                Map.Entry<String, String> entry = inputIter.next();
                                final String file = entry.getKey();
                                final String contents = entry.getValue();
                                
                                Thread t = new Thread(new Runnable() {
                                        @Override
                    public void run() {
                                                map(file, contents, mapCallback);
                    }
                                });
                                mapPool.execute(t);
                        }
                        //Ensures all threads complete
                        mapPool.shutdown();
                        //Not sure if this is good practice
                        mapPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
                      
                        long endTime = System.nanoTime();

                        long duration = (endTime - startTime);
                        double seconds = (double)duration / 1000000000.0;
                        System.out.println("The Distributed MapReduce map threads took "+seconds);
                        
                        // GROUP:
                        
                        Map<String, List<String>> groupedItems = new HashMap<String, List<String>>();
                        
                        Iterator<MappedItem> mappedIter = mappedItems.iterator();
                        while(mappedIter.hasNext()) {
                                MappedItem item = mappedIter.next();
                                String word = item.getWord();
                                String file = item.getFile();
                                List<String> list = groupedItems.get(word);
                                if (list == null) {
                                        list = new LinkedList<String>();
                                        groupedItems.put(word, list);
                                }
                                list.add(file);
                        }
                        
                        // REDUCE:
                        
                        final ReduceCallback<String, String, Integer> reduceCallback = new ReduceCallback<String, String, Integer>() {
                                @Override
                public synchronized void reduceDone(String k, Map<String, Integer> v) {
                                	outputApr3.put(k, v);
                }
                        };
                        
                       // List<Thread> reduceCluster = new ArrayList<Thread>(groupedItems.size());
                        
                        Iterator<Map.Entry<String, List<String>>> groupedIter = groupedItems.entrySet().iterator();
                        ExecutorService reducePool = Executors.newFixedThreadPool(poolSize);
                        
                        startTime = System.nanoTime();
                        while(groupedIter.hasNext()) {
                                Map.Entry<String, List<String>> entry = groupedIter.next();
                                final String word = entry.getKey();
                                final List<String> list = entry.getValue();
                                
                                Thread t = new Thread(new Runnable() {
                                        @Override
                    public void run() {
                                                reduce(word, list, reduceCallback);
                                        }
                                });
                                reducePool.execute(t);
                        }

                        //Ensures all threads complete
                        reducePool.shutdown();
                        
                        reducePool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

                        endTime = System.nanoTime();

                        duration = (endTime - startTime);
                         seconds = (double)duration / 1000000000.0;
                        System.out.println("Approach 3 output "+outputApr3);
                        System.out.println("Distributed MapReduce reduce threads took "+seconds);
                }
        
	 
	 //Approach 4
	 
	 {
         final Map<String, Map<String, Integer>> outputApr4 = new ConcurrentHashMap<String, Map<String, Integer>>();

         // MAP:

         final CopyOnWriteArrayList<MappedItem> mappedItems = new CopyOnWriteArrayList<MappedItem>();


         Iterator<Map.Entry<String, String>> inputIter = inputConcurrent.entrySet().iterator();
         ExecutorService mapPool = Executors.newFixedThreadPool(poolSize);
         long startTime = System.nanoTime();
         while(inputIter.hasNext()) {
             Map.Entry<String, String> entry = inputIter.next();
             final String file = entry.getKey();
             final String contents = entry.getValue();

             Thread t = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     map(file, contents, mappedItems);
                 }
             });
             //Adding thread to threadpool
             mapPool.execute(t);
         }
         //Ensures all threads complete
         mapPool.shutdown();
        
         mapPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

         long endTime = System.nanoTime();

         long duration = (endTime - startTime);
         double seconds = (double)duration / 1000000000.0;
         System.out.println("Concurrent Distributed MapReduce map threads took "+seconds);
         // GROUP:

         Map<String, List<String>> groupedItems = new ConcurrentHashMap<String, List<String>>();

         Iterator<MappedItem> mappedIter = mappedItems.iterator();
         while(mappedIter.hasNext()) {
             MappedItem item = mappedIter.next();
             String word = item.getWord();
             String file = item.getFile();
             List<String> list = groupedItems.get(word);
             if (list == null) {
                 list = new LinkedList<String>();
                 groupedItems.put(word, list);
             }
             list.add(file);
         }

         // REDUCE:


         Iterator<Map.Entry<String, List<String>>> groupedIter = groupedItems.entrySet().iterator();
         ExecutorService reducePool = Executors.newFixedThreadPool(poolSize);
         while(groupedIter.hasNext()) {
             Map.Entry<String, List<String>> entry = groupedIter.next();
             final String word = entry.getKey();
             final List<String> list = entry.getValue();

             Thread t = new Thread(new Runnable() {
                 @Override
                 public void run() {
                     reduce(word, list, outputApr4);
                 }
             });
             reducePool.execute(t);
         }

         //Ensures all threads complete
         reducePool.shutdown();
         
         mapPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

         endTime = System.nanoTime();

         duration = (endTime - startTime);
          seconds = (double)duration / 1000000000.0;
         System.out.println("Approach 4 output "+outputApr4);
         System.out.println("Concurrent Distributed MapReduce reduce threads took "+seconds);

     }
 }
        
        public static void map(String file, String contents, List<MappedItem> mappedItems) {
                String[] words = contents.trim().split("\\s+");
                for(String word: words) {
                        mappedItems.add(new MappedItem(word, file));
                }
        }
        
        public static void map(String file, String contents, CopyOnWriteArrayList<MappedItem> mappedItems) {
            String[] words = contents.trim().split("\\s+");
            for(String word: words) {
                    mappedItems.add(new MappedItem(word, file));
            }
    }
        
        public static void reduce(String word, List<String> list, Map<String, Map<String, Integer>> output) {
                Map<String, Integer> reducedList = new HashMap<String, Integer>();
                for(String file: list) {
                        Integer occurrences = reducedList.get(file);
                        if (occurrences == null) {
                                reducedList.put(file, 1);
                        } else {
                                reducedList.put(file, occurrences.intValue() + 1);
                        }
                }
                output.put(word, reducedList);
        }
        
        
        public static interface MapCallback<E, V> {
                
                public void mapDone(E key, List<V> values);
        }
        
        public static void map(String file, String contents, MapCallback<String, MappedItem> callback) {
                String[] words = contents.trim().split("\\s+");
                List<MappedItem> results = new ArrayList<MappedItem>(words.length);
                for(String word: words) {
                        results.add(new MappedItem(word, file));
                }
                callback.mapDone(file, results);
        }
        
        public static interface ReduceCallback<E, K, V> {
                
                public void reduceDone(E e, Map<K,V> results);
        }
        
        public static void reduce(String word, List<String> list, ReduceCallback<String, String, Integer> callback) {
                
                Map<String, Integer> reducedList = new HashMap<String, Integer>();
                for(String file: list) {
                        Integer occurrences = reducedList.get(file);
                        if (occurrences == null) {
                                reducedList.put(file, 1);
                        } else {
                                reducedList.put(file, occurrences.intValue() + 1);
                        }
                }
                callback.reduceDone(word, reducedList);
        }
        
        private static class MappedItem { 
                
                private final String word;
                private final String file;
                
                public MappedItem(String word, String file) {
                        this.word = word;
                        this.file = file;
                }

                public String getWord() {
                        return word;
                }

                public String getFile() {
                        return file;
                }
                
                @Override
                public String toString() {
                        return "[\"" + word + "\",\"" + file + "\"]";
                }
        }
} 
