set -x
if [ -d "$1" ]; then
    cd $1
    svn revert -R .
    svn st | awk '{print $2}' | xargs rm -rf
fi

echo "done clean"
