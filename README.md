# SNLP-Mini-Project

###How to execute the program
1.	Download the latest wikipedia dump. This is a bz2 archive, which contains a xml -file with the latest stand of the articles of wikipedia.
	https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2

	
2.	Extract the bz2 archive
	
	
3.	Extract the plain text from the xml-file:

	python WikiExtractor/WikiExtractor.py -q -o "Wikipedia Corpus" enwiki-latest-pages-articles.xml
	
	This can take up to a few hours.
	
	If this error appears: "AttributeError: module 'fileinput' has no attribute 'hook_compressed_encoded'"
	Follow this link: https://www.bountysource.com/issues/51517999-another-unicodedecodeerror
	And add the new method hook_compressed_encoded to the file $PYTHON_DIR/Lib/fileinput.py
	

4.	Start the main method of the class "TextAnalyzer" with the following paramters:
	<The corpus directory> <The directory of the output> <The minimum number of lines an article needs to be considered>
	This class analyzes the corpus. The first step for this is to extract all articles from the original corpus.
	Afterwards it checks whether an article contains all the nouns (or the corresponding synonyms) of one of the facts. 
	In the case that all nouns or synonyms are part of an article, it will be copied to a folder, which is named 
	after the fact id. This program results in a folder with 1301 (the number of facts) subfolders, where each folder
    can contain wikipedia articles according to the number of matches between the nouns and synonyms of the statement and 
	the content of the articles.
	

5.	The last step is starting the main method of the class "FactChecker". This class is processing the statements and assigns 
	a truth value to them. Every statement, respectively fact, is processed on its own. The nouns and verbs are extracted from 
	a statement. Afterwards it is checked whether any of those have synonyms. When that is done, it is checked if any of the text files, 
	which have been declared to be related to the statement beforehand, contain any lines, which contain all of the previously extracted 
	words from the statement. If a matching line was found '1.0' is assigned to the current statement. If no matching line was found or if 
	there are no related texts for the statement, the statement is assigned '-1.0'. Afterwards the result will be saved in a file named "result.ttl".
	


	
	