##Recreating the corpus from the database
####(and proving it equal to the version loaded from *fullCorpus.tsv*)

######**Input**: *merged_report.db* (an SQLite3 database)

######**Output**: *fullCorpus.tsv* and three files tracking error patterns (for accounting) located in ```patternTracking``` folder within the ```output``` folder.

##Program summary

This program should run for about a minute or two, printing lots of error messages from the PCRE parser, and then saying 'true' when the corpus created from the original database is equal to the one loaded from the text file.  This proves that the corpus loaded from the test file (which is easier and faster to load) is equivalent to the original corpus.

####Pattern tracking
A side effect of loading the corpus from the database is that the errors produced can be tracked, and so the three types of errors:

1. alien features (no error message from PCRE)
2. unicode (error messages like: no viable alternative at input 'u')
3. other errors (all other error messages, like: missing CloseParen at '|')

are tracked by printing out the patterns that caused the errors to the files in the `patternTracking' directory.  These error patterns were mentioned in the paper when describing the filtering process.
