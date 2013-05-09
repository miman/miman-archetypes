/**
 * 
 */
package se.miman.forge.easyprj.facet;

import javax.inject.Inject;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import se.miman.forge.easyprj.util.NazgulPrjUtil;

/**
 *	Makes sure the reactor project has the 'miman-root' as a parent project and that the version is correct.  
 * @author Mikael Thorman
 */
@Alias("miman-reactor-facet")
@RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
	DependencyFacet.class })
public class MimanReactorPrjFacet extends BaseFacet {

	@Inject
	ProjectFactory prjFactory;

   @Inject
   private ShellPrintWriter writer;

	public MimanReactorPrjFacet() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.jboss.forge.project.Facet#install()
	 */
	@Override
	public boolean install() {
		installNazgulConfiguration();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.jboss.forge.project.Facet#isInstalled()
	 */
	@Override
	public boolean isInstalled() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		// Change the parent to Nazgul project
		if (pom.getParent() == null) {
			return false;
		}
		if (!MimanPrjFacet.PARENT_GROUP_ID.equals(pom.getParent().getGroupId())) {
			return false;
		}
		if (!MimanPrjFacet.PARENT_ARTIFACT_ID.equals(pom.getParent().getArtifactId())) {
			return false;
		}
		return true;
	}

	/**
	 * Set the project parent to MiMan project.
	 * Changes the project to a pom project.
	 */
	private void installNazgulConfiguration() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		// Change the parent to Nazgul project
		Project parentPrj = NazgulPrjUtil.findParentProject(project, prjFactory);
		MavenCoreFacet maprentPrjMvnFacet = parentPrj.getFacet(MavenCoreFacet.class);
		Model parentPom = maprentPrjMvnFacet.getPOM();
		
		if (parentPom.getParent() == null) {
			// The parent pom doesn't have any parent pom in its turn
			writer.println(ShellColor.RED, "Error - The parent pom.xml file doesn't have any parent!");
			writer.println("The parent pom is located here: " + parentPrj.getProjectRoot().getFullyQualifiedName());
			return;
		}
		
		if (pom.getParent() == null) {
			pom.setParent(new Parent());
		}
		pom.getParent().setGroupId(parentPom.getParent().getGroupId());
		pom.getParent().setArtifactId(parentPom.getParent().getArtifactId());
		pom.getParent().setVersion(parentPom.getParent().getVersion());
		
		pom.setPackaging("pom");
		
		mvnFacet.setPOM(pom);
	}
}
