package se.miman.forge.easyprj;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.ShellMessages;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.SetupCommand;

import se.miman.forge.easyprj.facet.NazgulReactorPrjFacet;

/**
 * Modifies a project to a Nazgul reactor project (has the 'nazgul-tools-reactor' as a parent in the pom).
 */
@Alias("nazgul-reactor-prj")
@Help("A plugin that helps to build a Nazgul reactor project")
@RequiresProject
public class NazgulReactorPrjPlugin implements Plugin
{
	@Inject
	private Event<InstallFacets> event;

	@Inject
	private Project project;

   @SetupCommand
   @Command(value = "setup", help = "Convert project to a Nazgul reactor project")
   public void setup(PipeOut out) {

		if (!project.hasFacet(NazgulReactorPrjFacet.class))
	           event.fire(new InstallFacets(NazgulReactorPrjFacet.class));
	       else
	           ShellMessages.info(out, "Project already an Nazgul reactor project.");
   }
}
