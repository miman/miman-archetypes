/**
 * 
 */
package se.miman.forge.easyprj.facet;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import se.miman.forge.easyprj.dto.MavenProjectId;
import se.miman.forge.easyprj.util.NazgulPrjUtil;

/**
 *	Makes sure the reactor project has the 'nazgul-tools-reactor' as a parent project and that the version is correct.  
 * @author Mikael Thorman
 */
@Alias("nazgul-reactor-facet")
@RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
	DependencyFacet.class })
public class NazgulReactorPrjFacet extends BaseFacet {

	public NazgulReactorPrjFacet() {
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
		if (!"se.jguru.nazgul.tools".equals(pom.getParent().getGroupId())) {
			return false;
		}
		if (!"nazgul-tools-reactor".equals(pom.getParent().getArtifactId())) {
			return false;
		}
		return true;
	}

	/**
	 * Set the project parent to Nazgul project.
	 * Changes the project to a pom project.
	 */
	private void installNazgulConfiguration() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		// Change the parent to Nazgul project
		MavenProjectId prjId = NazgulPrjUtil.getParentProjectId(pom, project);
		
		if (pom.getParent() == null) {
			pom.setParent(new Parent());
		}
		pom.getParent().setGroupId("se.jguru.nazgul.tools");
		pom.getParent().setArtifactId("nazgul-tools-reactor");
		pom.getParent().setVersion(prjId.getVersion());
		
		pom.setPackaging("pom");
		
		mvnFacet.setPOM(pom);
	}
}
