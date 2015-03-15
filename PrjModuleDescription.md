

# Introduction #

This page describes the purpose of all modules in this project.


# Modules #

## nazgulprj-archetype ##
This is an archetype for creating a base project according to the structure defined in the nazgul-tools & nazgul-core project.

This project can be found here:

https://bitbucket.org/lennartj/nazgul_tools

https://bitbucket.org/lennartj/nazgul_core

A description of the project structure and features of this can be found here:

https://bytebucket.org/lennartj/nazgul_tools/wiki/index.html

For a more detailed description on how this archetype works & how to use it, run mvn site on the project.

## camel-war-archetype ##
This is an archetype for creating a sub-project which is a web project that will run any camel routes in its path.

The purpose is to create one project like this and then a number of camel routes projects with the archetype described below which contains the actual routes.

## camel-rest-war-archetype ##
This is an archetype for creating a sub-project which is a web project that will run any camel routes in its path with support for REST routes based on restlets.

The purpose is to create one project like this and then a number of camel routes projects with the archetype described below which contains the actual routes.

## camel-route-minimal-archetype ##
This is an archetype for creating a sub-project which contains one or more camel routes and any calsses needed by these routes (transformations...). This is then run by the web project created with camel-war-archetype.