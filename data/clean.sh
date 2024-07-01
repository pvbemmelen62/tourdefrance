#!/bin/sh

set -x

#starters-html-only.html
#starters-tidy.html


for i in \
starters-tidy.html \
cyclistsIndex.html \
teamNamesIndex.html \
teams.js \
teams.xml \
teamsIndex.html \
teamsJerseys.html \
teams.json ; do
  rm -f $i
done


