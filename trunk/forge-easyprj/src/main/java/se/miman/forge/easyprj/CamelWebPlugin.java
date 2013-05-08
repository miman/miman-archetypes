package se.miman.forge.easyprj;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;

import se.miman.forge.easyprj.facet.CamelWebPrjFacet;

/**
 * Modifies a project to be a Spring based WAR project that can run Apache Camel routes.
 * @author Mikael Thorman
 */
@Alias("camelweb")
@Help("A plugin that helps to build Apache Camel Web artifacts")
@RequiresProject
public class CamelWebPlugin implements Plugin
{
	@Inject
	private Event<InstallFacets> event;

	@Inject
	private Project project;

   /**
    * The setup command converts a project to a war project that can run Apache Camel routes.
    * @param camelBasePackage	The package where the camel routes can be found (used to know where to scan for Camel annotated classes)
    * @param prjDescription	The decription of the project (used in the project pom file.
    * @param out	Used to write info back to the user.
    */
   @SetupCommand
   @Command(value = "setup", help = "Convert the project to a Spring based WAR project that can run Apache Camel routes")
   public void setup(@Option(name="camelBasePackage", shortName="cbp") String camelBasePackage, 
		   			@Option(name="prjDescription", shortName="pd") String prjDescription, 
		   			PipeOut out) {

//	   if (camelBasePackage == null) {
//		   ShellMessages.info(out, "You must supply a camelBasePackage!");
//		   return;
//	   }
	   CamelWebPrjFacet.camelBasePackage = camelBasePackage;
	   CamelWebPrjFacet.prjDescription = prjDescription;
		if (!project.hasFacet(CamelWebPrjFacet.class))
	           event.fire(new InstallFacets(CamelWebPrjFacet.class));
	       else
	           ShellMessages.info(out, "Project already an Camel project.");
	   
   }
}
