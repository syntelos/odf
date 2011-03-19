#!/bin/bash

for src in odfdom simple
do
    if [ -d ${src} ]
    then
	if cd ${src} && hg pull && hg update
	then
	    cd ..
	    echo ${src}
	else
	    echo "Error in 'cd ${src} && hg pull && hg update'."
	    exit 1
	fi
    else
	echo "Error, require 'init'."
	exit 1
    fi
done

echo "Updated upstream odfdom and simple.  Need 'copy' then 'ant'."
exit 0
