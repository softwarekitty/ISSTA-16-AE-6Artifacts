import sys, os

# dangerous!  erases lots of stuff!
# expects to be run in the directory containing AllRows
# only run if you really want to empty all the row directory contents

# cite: http://stackoverflow.com/questions/2212643/python-recursive-folder-read
for root, subdirs, files in os.walk(os.path.join(sys.path[0],"allRows"):
    for subdir in subdirs:
        for filename in files:
            os.remove(os.path.join(root, filename))
