#Info About Mined Projects

######Input: contents of merged_report.db (an SQLite3 database)

######Output: *projectInfo.tsv*

##Program Summary
To aide the validation of the mining technique, information about the 1645 projects containing regexes that provided all the data for analysis is extracted from the database by this program.  The program queries the database and obtains the last sha seen (the current master at the time of mining) from the *sourceJSON* field for each source.

#####Columns of *projectInfo.tsv*:

- internal ID (useful for matching up patterns in other files to their project IDs)
- clone url (useful for cloning the project for study)
- last commit sha (necessary because reproducing the results would be sensitive to new commits)
- the GitHub repoID, which may be used in their API for whatever purpose.

This is not a result of the paper, but information about the sources of data for the paper was requested by reviewers, so this utility was created to make extracting that data easier.
