#/bin/bash
if [ "$#" -ne 1 ]; then
	echo "Usage: $0 <image>"
	exit 0
fi

convert $1 -resize x400 ../../src/main/res/drawable/$1
