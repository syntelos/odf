#!/bin/bash

case $1 in

    in)
	for src in $(find src -type f -name '*.java~')
	do
	    tgt=$(echo $src | sed 's%~$%%')
	    dif="patches/${tgt}.diff"
	    difd=$(dirname ${dif})
	    if [ ! -d ${difd} ]
	    then
		mkdir -p ${difd}
	    fi
	    if ! diff -b ${tgt} ${src} > ${dif}
	    then
		wc -l ${dif}
	    else
		echo "Error in 'diff -b ${tgt} ${src} > ${dif}'."
		exit 1
	    fi
	done
	;;

    out)
	for dif in $(find patches -type f -name '*.diff')
	do
	    tgt=$(echo ${dif} | sed 's%patches/%%; s%\.diff$%%')

	    if patch -R ${tgt} -i ${dif}
	    then
		ls -l ${tgt}
	    else
		echo "Error in 'patch ${tgt} -i ${dif}'."
		exit 1
	    fi
	done
	;;
    *)
	cat<<EOF
Usage

  $0 (in|out)

Description

  in  -  Create patches from sources

  out -  Apply patches to sources

Notes

  All other patch management is manual.  

  One should delete all patches for a new update & copy.

EOF
	exit 1
	;;
esac

exit 0
