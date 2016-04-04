Artifacts prepared for ISSTA-16-AE-6

A brief tutorial of how to generate the main table in the paper that lists the frequency of observed features in the corpus.

DATA SOURCE
The data source used to present data in the paper is provided, but the tool should work with any data set matching the schema.
The project builds the corpus using the following select statment:
"select pattern, uniqueSourceID from RegexCitationMerged where (flags=0 or flags like 'arg%' or flags=128 or flags='re.DEBUG') and pattern!='arg1';"

So a corpus can be build from any database conforming to the following schema:
Table1:(USED) uniqueSourceID: int, repoID: int, sourceJSON: text, fileHash: char(44), filePath: text, pattern: text, flags: int, regexFunction: int

uniqueSourceID is an int unique to a source project (USED)
repoID is the ID assigned by GitHub to a repo (not used)
sourceJSON contains data about the clone_url, etc (not used)
the fileHash is the SHA_224 hash of the original source file (not used)
the filePath is the path of the file relative to the repo root folder. (not used)
the pattern is the string used to define the regex (USED)
Flags 0,2,4,8,16,32,64 and 128 map to their internal Python values, as indicated in this source code. (USED: all utilizations ignored except those using flags: 0, like 'arg%', 128, 're.DEBUG')
regexFunction maps 0,1,2,3,4,5,6 to the 7 functions of Python's re module, as indicated in this source code. (not used)


Table2:(not used) nFiles: int, frequency: int

nFiles counts the number of files a project has (not used)
frequency indicates the number of times a project having nFiles was observed, (not used)
[note these special values: 
nFiles=-1 (not used): number of files observed by sourcer (tested for Python content)
nFiles=-2 (not used): an error counter for refreshing a repository]

THIS REPO IS SIMPLIFIED
The version presented here is simplified, so that the source code is more clear - in the original and more complex version, the code automatically generated several minor tables, and gathered various statistics.  
The source code for these functions is available on the original repository (https://github.com/softwarekitty/tour_de_source).
This version will accept a database as input and produce the main feature counting table as output, which should generalize to any source of valid patterns using a subset of PCRE features.
So flags and functions will also be ignored so that this version of the tool can generalize.


ENVIRONMENT SETUP
Use the source to create a Java Project in Eclipse (use Java 1.7)
Add the five included jar files to the build path
In the `C' file, change the `artifactPath' file to the root of this cloned repo.
Check the output folder - there should be no content.
Run the Main file. (Some error noise from the PCRE parser is normal - ignore this unless the program stops)
Check the output folder - a latex table should be present displaying the feature frequencies for the studied feature set.
