#!/bin/bash

function copyf {
    if cp -p ${src} ${tgt}
    then
	echo ${tgt}
	return 0
    else
	echo "Error in 'cp -p ${src} ${tgt}'."
	return 1
    fi
}
function rewritef {

    if cat ${src} | sed 's%org\.odftoolkit\.simple%odf%g' > ${tgt}
    then
	touch -r ${src} ${tgt}
	echo ${tgt}
	return 0
    else
	echo "Error in 'cat ${src} | sed 's%org\\.odftoolkit\\.simple%odf%g' > ${tgt}'."
	return 1
    fi
}

#
# Copy odfdom to src
#
for src in $(find odfdom -type f -name '*.java' | egrep -v '/test/')
do
    tgt=$(echo $src | sed 's%.*/src/main/java/%src/%')
    if [ -z "$(echo $tgt | egrep '^src/' )" ]
    then
	echo "Error unrecognized file $src"
	exit 1
    else
	tgtd=$(dirname $tgt)
	if [ ! -d ${tgtd} ]
	then
	    mkdir -p ${tgtd}
	    if ! copyf ${src} ${tgt}
	    then
		exit 1
	    fi
	elif [ ! -f ${tgt} ]||[ ${src} -nt ${tgt} ]
	then
	    if ! copyf ${src} ${tgt}
	    then
		exit 1
	    fi
	fi
    fi
done
#
# Rewrite simple to src
#
for src in $(find simple -type f -name '*.java' | egrep -v '/test/')
do
    tgt=$(echo $src | sed 's%.*/src/main/java/org/odftoolkit/simple/%src/odf/%')
    if [ -z "$(echo $tgt | egrep '^src/' )" ]
    then
	echo "Error unrecognized file $src"
	exit 1
    else
	tgtd=$(dirname $tgt)
	if [ ! -d ${tgtd} ]
	then
	    mkdir -p ${tgtd}
	    if ! rewritef ${src} ${tgt}
	    then
		exit 1
	    fi
	elif [ ! -f ${tgt} ]||[ ${src} -nt ${tgt} ]
	then
	    if ! rewritef ${src} ${tgt}
	    then
		exit 1
	    fi
	fi
    fi
done
#
# Update version information from odfdom
#
if version=$(./versionof.sh odfdom)
then
    vmajor=$(echo $version | awk -F. '{print $1}')
    vminor=$(echo $version | awk -F. '{print $2}')
    vbuild=$(echo $version | awk -F. '{print $3}')
    if [ -n "${vmajor}" ]&&[ -n "${vminor}" ]
    then
	if [ -z "${vbuild}" ]
	then
	    vbuild=0
	fi
	cat<<EOF>build.version
version.major=${vmajor}
version.minor=${vminor}
version.build=${vbuild}
EOF
	echo "${vmajor}.${vminor}.${vbuild}"
    else
	cat<<EOF>&2
Error, version information not found for 'odfdom'.
See './versionof.sh odfdom'.
EOF
	exit 1
    fi
else
    cat<<EOF>&2
Error in 'version=\$(./versionof.sh odfdom)'.
EOF
exit  1
fi
echo "Copy completed.  Need 'patch'?"
exit 0
