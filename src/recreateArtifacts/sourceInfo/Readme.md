#Info About Mined Projects

######Input: contents of merged_report.db (an SQLite3 database)

######Output: *projectInfo.tsv*

To aide the validation of the mining technique, various information about the 1645 projects containing regexes that provided all the data for analysis is extracted from the database in this package.

We provide the internal ID (useful for matching up patterns in other files to their projects), the clone url (useful for cloning the project for study), the last commit sha (necessary because reproducing the results would be sensitive to new commits), and the GitHub repoID, which may be used in their API for whatever purpose.

This is not a result of the paper, but information about the sources of data for the paper was requested by reviewers, so this utility was created to make extracting that data easier.
