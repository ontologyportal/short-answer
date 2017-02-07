#short-answer

Short Answer component

based on: http://www.adampease.org/professional/GlobalWordNet2016.pdf

Build
=====

Run "maven package" and use the assembled jar (second fat jar) in your class path.

Demo
====

Run the Demo by:

java -Xmx14G -cp [your path to the fat jar]/cobra-0.98.4-jar-with-dependencies.jar nlp.scripts.Demo index-dir models-dir question-classifier.pa770.ser

