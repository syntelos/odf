#!/bin/bash
#
# Run through the first build
#

function backout {
    if rm -f $(find patches -type f )
    then
	if rm -rf src/org src/odf
	then
	    return 0
	else
	    return 1
	fi
    else
	return 1
    fi
}

if [ -d src/org ]||[ -d src/odf ]
then
    cat<<EOF
Wrong state to run this script.  Looking for 'update'?
EOF
    exit 1

elif ./init.sh 
then
    if ./copy.sh
    then
	if ./patch.sh out
	then
	    if ant
	    then
		echo ok
		exit 0
	    else
		if backout
		then
		    cat<<EOF
Patches failed to build, backed out.  Please run this script again.
If you're really unhappy, delete the world and start over.
EOF
		else
		    cat<<EOF
Patches failed.  Backing out failed.  Don't know this state.
If you're really unhappy, delete the world and start over.
EOF
		fi
		exit 1
	    fi
	else
	    if backout
	    then
		cat<<EOF
Patches failed, backed out.  Please run this script again.
If you're really unhappy, delete the world and start over.
EOF
	    else
		cat<<EOF
Patches failed.  Backing out failed.  Don't know this state.
If you're really unhappy, delete the world and start over.
EOF
	    fi
	    exit 1
	fi
    else
	cat<<EOF
Copy failed.  Don't know this state.  Doing nothing.
If you're really unhappy, delete the world and start over.
EOF
	exit 1
    fi
else
    cat<<EOF
Checkout failed.  Need to install mercurial?
EOF
    exit 1
fi
