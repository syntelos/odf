#!/bin/bash

echo hg clone https://hg.odftoolkit.org/hg/simple~code-base simple
if hg clone https://hg.odftoolkit.org/hg/simple~code-base simple
then
    echo hg clone https://hg.odftoolkit.org/hg/odfdom~developer odfdom
    if hg clone https://hg.odftoolkit.org/hg/odfdom~developer odfdom
    then
	exit 0
    else
	exit 1
    fi
else
    exit 1
fi
