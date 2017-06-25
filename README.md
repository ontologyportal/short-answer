#short-answer

Short Answer component

based on: http://www.adampease.org/professional/GlobalWordNet2016.pdf

Build
=====

Run "mvn package" and use the assembled jar (second fat jar) in your class path.

Demo
====

Run the Demo with the following (adapting the paths to your local structure):

java -Xmx7G -cp target/cobra-0.98.4-jar-with-dependencies.jar nlp.scripts.Demo  /
   /home/user/data/short-answer/short-answer-data/index /
   /home/user/data/short-answer/short-answer-data/models /
   question-classifier.pa770.ser /
   questions.txt


This simple demo go overs the questions supplied in the questions.txt (optional, otherwise it uses a static small set of questions),
which contains a simple question per line, i.e. "What is the estimated population of Egypt?"

See below how to obtains the needed data files, or how to train the models and create the needed supporting text files by your self.

Index
=====

The candidate sentences which potentially contain answers to potential questions, aka the Knowledge Base, resides in a lucene index.
This index is created by the IndexSentences script that can be run like this:

java -Xmx7G -cp target/cobra-0.98.4-jar-with-dependencies.jar nlp.scripts.IndexScripts [corpus-path] [index-path]

where corpus-path is a path to a directory which contains text files with sentences to be indexed into the knowledge base,
and the index-path is path to a non existing directory which will contain the lucene index and be used as the knowledge base.

A sample index can be found in models dir in the repository.

Models
=====

There are a few static models used and one trained model. The static models, which are used for featurization mainly, are:
brown-clusters, word vectors, and gazetteers. They all can be found in the models dir in the repository.

The trained model is the questions classifier, which uses all those features.

NOTE: glove.6B.50d.txt.gz is not included beacause of the size and should be downloaded from: http://nlp.stanford.edu/projects/glove/


Question Classifier
===================

A pre-trained classifier is located inside the models dir in the repo, but its name has to be supplied to the demo script separately.
(just the name of the file to be used inside the models dir). During traning multiple versions of the classifier exist inside the models dir,
and so the demo asks for the particular verstion to be used.

In order to train the classifier you need to use the QuestionClassifierTrainingScript:

java -Xmx7G -cp target/cobra-0.98.4-jar-with-dependencies.jar nlp.scripts.TrainQuestionClassifier [models-output-path] [questions-train-test-set]

The models output path is the path where all the versions of the classifier will be written to. The dataset in the questions-train-test-set dir should reside
within to folders, train and test, inside each files should be formatted in the following way:

DESC:manner How did serfdom develop in and then leave Russia ?
ENTY:cremat What films featured the character Popeye Doyle ?
DESC:manner How can I find a list of celebrities ' real names ?
ENTY:animal What fowl grabs the spotlight after the Chinese Year of the Monkey ?
ABBR:exp What is the full form of .com ?

in according to: http://cogcomp.cs.illinois.edu/Data/QA/QC/

Testing the classifier
======================

We were unable to reproduce the exact results cited in the paper for classification of the questions: http://www.adampease.org/professional/GlobalWordNet2016.pdf,
but non the less we came close. We report 83% on fine, and 89% on gross.

The classifier can be tested using the TestQuestionsClassifier script:

java -Xmx7G -cp target/cobra-0.98.4-jar-with-dependencies.jar nlp.scripts.TestQuestionClassifier [models-path] [classifier-name] [questions-data-path] [type=gross/fine]

This outputs the accuracy on the test set inside questions-data-path dir. The models-path and the classifier-name are as in previous sections. Type is just a string "gross" or "fine"
to indicate how to test the classifier.

Alternative Ant-based Build
======================
cd ~
echo "export SIGMA_SRC=~/workspace/sigmakee" >> .bashrc
echo "export CORPORA=~/corpora" >> .bashrc
source .bashrc
cd ~/workspace/
git clone https://github.com/ontologyportal/sigmanlp
wget 'http://nlp.stanford.edu/software/stanford-corenlp-full-2015-12-09.zip'
unzip stanford-corenlp-full-2015-12-09.zip
rm stanford-corenlp-full-2015-12-09.zip
cd ~/Programs/stanford-corenlp-full-2015-12-09/
unzip stanford-corenlp-3.6.0-models.jar
cp ~/Programs/stanford-corenlp-full-2015-12-09/stanford-corenlp-3.6.0.jar ~/workspace/short-answer/lib
cp ~/Programs/stanford-corenlp-full-2015-12-09/stanford-corenlp-3.6.0-models.jar ~/workspace/short-answer/lib
cd ~/workspace/short-answer/models
wget 'http://nlp.stanford.edu/data/glove.6B.zip'
unzip glove.6B.zip
java -Xmx9G -cp ~/workspace/short-answer/build/classes:~/workspace/short-answer/build/lib/* nlp.scripts.Demo -t
