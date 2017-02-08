#short-answer

Short Answer component

based on: http://www.adampease.org/professional/GlobalWordNet2016.pdf

Build
=====

Run "mvn package" and use the assembled jar (second fat jar) in your class path.

Demo
====

Run the Demo with the following (adapting the paths to your local structure):

java -Xmx7G -cp target/cobra-0.98.4-jar-with-dependencies.jar nlp.scripts.Demo
/home/user/CloudMinds/short-answer/short-answer-data/index
/home/user/CloudMinds/short-answer/short-answer-data/models question-classifier.pa770.ser
