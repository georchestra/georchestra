# Setting up OpenLDAP and a basic LDAP tree

## install the required packages

        sudo apt-get install slapd ldap-utils git-core

## sample data import

 * getting the data, where XX stands for the geOrchestra version you're using (eg: ```14.06``` for stable or ```master``` for unstable)
 
            git clone -b XX git://github.com/georchestra/LDAP.git
	
 * inserting the data: follow the instructions in https://github.com/georchestra/LDAP/blob/master/README.md

 * check everything is OK:
 
            ldapsearch -x -bdc=georchestra,dc=org | less