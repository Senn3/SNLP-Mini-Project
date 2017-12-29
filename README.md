# SNLP-Mini-Project

###How to create the corpus
1.	Download the latest wikipedia dump. This is a bz2 archive, which contains a xml -file with the latest stand of the articles of wikipedia.
	https://dumps.wikimedia.org/enwiki/latest/enwiki-latest-pages-articles.xml.bz2

	
2.	Extract the bz2 archive
	
	
3.	Extract the plain text from the xml-file:

	python WikiExtractor/WikiExtractor.py -q -o "Wikipedia Corpus" enwiki-latest-pages-articles.xml
	
	This can take up to a few hours.
	
	If this error appears: "AttributeError: module 'fileinput' has no attribute 'hook_compressed_encoded'"
	Follow this link: https://www.bountysource.com/issues/51517999-another-unicodedecodeerror
	And add the new method hook_compressed_encoded to the file $PYTHON_DIR/Lib/fileinput.py
	
	
4.	Separated the wiki_** file into individual .txt-files, where each file contains exactly 1 article.
 
	java -jar WikiExtractor/WikipediaExtractor.jar "Wikipedia Corpus" "Wikipedia Corpus Cutted"
	
	This can take up to a few hours again.
	
	