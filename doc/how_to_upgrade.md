
configuration
=============

Do whatever updates you want in the master branch, and regularly merge the upstream changes:

    git checkout master
    git fetch upstream
    git merge upstream/master

Note: merge upstream/master into your config if you're using geOrchestra master, or upstream/14.06 if you're using geOrchestra stable.