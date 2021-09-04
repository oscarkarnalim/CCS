# Code Clarity Suggester

**Code Clarity Suggester** \(CCS\) aims to educate computing students about code clarity in programming. It scans Java/Python program code and highlights any identifier names or comments whose clarity needs to be improved. For convenience, the suggestion is mapped to an interactive HTML file as shown below. Further details will be published in [a paper](#) published in 2021 IEEE Frontiers in Education (FIE) Conference. This is a work-in-progress and more code clarity aspects will be considered in its later version.

<p align="center">
<img width="80%" src="https://github.com/oscarkarnalim/ccs/blob/main/code_clarity_sample_layout.png?raw=true">
</p>

## CCS suggestion types
1. Suggestion to update identifier names that are too short.
2. Suggestion to update identifier names that are not meaningful.
3. Suggestion to update identifier names that are incorrectly written.
4. Suggestion to update identifier names that use inconsistent word transition compared to
   other names. The transition is either capitalisation ('thisIsIdent') or underscore
   ('this_is_ident').
5. Suggestion to update comments that are too short.
6. Suggestion to update comments that are not meaningful.
7. Suggestion to update comments that are incorrectly written.
8. Suggestion to add an explaining comment for each syntax block.

## Minimum command
```
-path <program_path> -proglang <programming_language>
```
<program_path> is the complete path of targeted program code file. 
<programming_language> refers to the programming language used in the program code. Values: 'java' (for Java) or 'py' (for Python). 
The result can be seen in 'out.html'. 

## Additional arguments
### Treating library identifiers as keywords
```
-akpath <additional_keyword_path>
```
It is applicable when some identifiers need to be recognised as keywords. This typically happens when the program uses third-party libraries. <additional_keywords_path> refers to a file containing additional keywords with newline as the delimiter. Keywords with more than one token should be written by embedding spaces between the tokens. For example, 'System.out.print' should be written as 'System . out . print'.

### Human language
```
-humanlang <human_language>
```
This changes the human language used while delivering the suggestions. <human_language> values: 'en' for both British and American English (default), 'id' for Indonesian, and 'iden' for Indonesian and English.

### Reindex
```
-reindex
```
This forces CCS to reindex word database for spell checking. Please use it only when the human language is changed as the process is quite time consuming.

### Set the output as a text, not a HTML
```
-outtext
```
This alters the resulted output to a standard text file ('out.txt'). This might be useful when CCS is integrated to larger system.

### Remove suggestions about identifier names that are too short
```
-excludesi
```

### Remove suggestions about identifier names that are not meaningful
```
-excludemi
```

### Remove suggestions about identifier names that are incorrectly written
```
-excludeii
```

### Remove suggestions about identifier names that use inconsistent word transition
```
-excludeici
```

### Remove suggestions about comments that are too short
```
-excludesc
```

### Remove suggestions about comments that are not meaningful
```
-excludemc
```

### Remove suggestions about comments that are incorrectly written
```
-excludeic
```

### Remove suggestions about adding an explaining comment per syntax block
```
-excludebc
```

## Acknowledgments
This tool uses [ANTLR](https://www.antlr.org/) to tokenise given programs, [Google Prettify](https://github.com/google/code-prettify) to display source code, [Tartarus'code](https://tartarus.org/martin/PorterStemmer/java.txt) to stem words, and [Apache Lucene](https://lucene.apache.org/) to identify stop words and detect incorrectly written words. In terms of dictionaries for spelling correction, they are taken from three websites: [American](https://github.com/dwyl/english-words), [British](https://www.curlewcommunications.uk/wordlist.html), and [Indonesian](http://indodic.com/SpellCheckInstall.html).




