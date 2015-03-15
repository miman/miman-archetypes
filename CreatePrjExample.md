

# Introduction #

This page descibes an example of how to create a project with these artifacts.


# Creating projects #
Here we create an example project called miman-test-prj, which will contain one camel route logging a text every 20 secs.

This route will be run in a spring based web project.

## Create main project ##
Run the following maven command:
```
mvn archetype:generate -DarchetypeGroupId=se.miman.archetypes -DarchetypeArtifactId=miman-minimal-main-prj-archetype -DarchetypeVersion=1.0.0  -DgroupId=se.miman.test -Dpackage=se.miman.test -DartifactId=miman-test-prj -Dversion=1.0.0-SNAPSHOT
```

## Create web project ##
Change to the project root directory
```
cd miman-test-prj
```

Run the following maven command:
```
mvn archetype:generate -DarchetypeGroupId=se.miman.archetypes.camel -DarchetypeArtifactId=camel-war-archetype -DarchetypeVersion=1.0.1  -DgroupId=se.miman.test.web -Dpackage=se.miman.test.web -DartifactId=miman-test-prj-web -Dversion=1.0.0-SNAPSHOT -DparentGroupId=se.miman.test.poms.parent -DparentArtifactId=miman-test-prj-parent -DparentVersion=1.0.0-SNAPSHOT
```

## Create Camel route project ##
Run the following maven command:
```
mvn archetype:generate -DarchetypeGroupId=se.miman.archetypes.camel -DarchetypeArtifactId=camel-route-minimal-archetype -DarchetypeVersion=1.0.1  -DgroupId=se.miman.test.routes -Dpackage=se.miman.test.routes -DartifactId=miman-test-prj-route1 -Dversion=1.0.0-SNAPSHOT -DparentGroupId=se.miman.test.poms.parent -DparentArtifactId=miman-test-prj-parent -DparentVersion=1.0.0-SNAPSHOT
```

# Configuring the projects #

## Add route project to web project ##
For the web project to be aware of the camel route it needs to import the camel route project.

Add the following to the pom file of the miman-test-prj-web project.

```
    <dependency>
      <groupId>se.miman.test.routes</groupId>
      <artifactId>miman-test-prj-route1</artifactId>
      <version>1.0.0-SNAPSHOT</version>
    </dependency>
```

# Run the project #
Now run the project on a tomcat in eclipse or run the following command
```
mvn tomcat:run
```

You should now see the following line repeating after the web server has started:
```
[mel-1) thread #0 - timer://foo] route1      INFO  >>> TestRoute
```