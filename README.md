# College-Projects

## 2D Colour Game
The assignment given was to create our own 2D Game using HTML5 Canvas and JavaScript. The purpose of my game is to go through the pipe that is the same colour of the plane. You get one point for getting through a pipe and another point for hitting a box. Link to Game online [Colour Plane](http://ec2-54-229-109-138.eu-west-1.compute.amazonaws.com/Colour%20Plane%20Game/index.html?utm_source=Viber&utm_medium=Chat&utm_campaign=Private).


## Distributed MapReduce
The goal of this assignment was to implement and test some modifications for the Map Reduce Application. We had to do 4 different appraochs to process several large text files. The output should be a hashmap within a hashmap with a key for each word that's value is a hashmap. The inner hashmap's key is the file the word is in and the value is the number of occurences of the word eg. [when-> file1.txt=3, file3.txt=2].

#### The four approaches:
1. Brute Force.
2. MapReduce.
3. Distributed MapReduce using Callbacks.
4. Distributed MapReduce using Threadsafe CopyOnWriteArrayList and ConcurrentHashMap.

## Final Year Project
The aim of this project was the creation of a Smartphone based Road Surface Surveying ICT Platform aimed at aiding the Co. Council by notifying them of safety risks on the roads around the country. The platform consists of an app that uses the Smartphone’s built in GPS and accelerometer to track the user’s journey and vibrations that occurred over the trip. The data is uploaded to the backend where the analysis is performed, with results sent back for viewing on the phone or via a desktop browser. The application was designed, developed and deployed on both Android and iOS using the Apache Cordova framework. 

The main files in this project are the index.html and tracker.js in both the mobile app and the website. Unfortunately due to the time constraints of the final year project I was unable to finish the website user interface to the standard I would have liked. The data analyzing code is in the analyze.php file in the backend folder. 

#### Some of the processes involved in the analysis included:
* Handling the data coming in from the database and ensuring the order is correct.
* Virtual Reorientation of the phones axes to align with the car's axes.
* Creation of a pothole detection algorithm.

##Information Retrieval System
This was a group project where we had to develop an Information Retrieval System written in Java from scratch. Given a query, it computes the Cosine Similarity between the query and the given documents it returns a ranked list of all relevant documents relating to that query using a modern weighting scheme (TFIDF).

##Java RMI Assignment
Project consisted of a client-server model where a student (client) could download an assessment object from the server, complete the assessment and then upload is back to the server for correction.

##Machine Learning Assignment
Developed my own implementation of the Perceptron machine learning algorithm in Java to allow it to become a multi-class classifier. The program is fed in a set of classified training data upon which it uses the perceptron algorithm to 'learn' the patterns of these inputs and determine an optimal weighting scheme for the attributes. The algorithm had an average accuracy of 95.5%.
