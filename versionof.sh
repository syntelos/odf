#!/bin/bash

function usage {
    cat<<EOF>&2
Usage

 $0 dir

Description

 Extract version number from pom.xml in dir.

EOF
    exit 1
}
function version {
    egrep '<version>' ${pomf} | head -n 1 | sed 's%.*<version>%%; s%</version>.*%%; s%-SNAPSHOT%%'
}

if [ -d $1 ]
then
    srcd=$1
    pomf=${srcd}/pom.xml
    if [ -f ${pomf} ]
    then
	version ${pomf}
    else
	usage
    fi
else
    usage
fi
