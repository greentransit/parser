#!/bin/bash
echo ">> Parsing...";
CLASS=$(cat "parser_class")
java -Xms4096m -Xmx24576m -Dfile.encoding=UTF-8 \
-classpath \
bin:\
lib/commons-lang3-3.1.jar \
$CLASS;
echo ">> Parsing... DONE";
