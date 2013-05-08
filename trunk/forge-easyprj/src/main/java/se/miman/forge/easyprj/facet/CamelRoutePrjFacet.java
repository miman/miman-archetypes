/**
 * 
 */
package se.miman.forge.easyprj.facet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.project.facets.DependencyFacet;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import se.miman.forge.easyprj.dto.MavenProjectId;
import se.miman.forge.easyprj.util.NazgulPrjUtil;
import se.miman.forge.easyprj.util.VelocityUtil;

/**
 * This is the Facet class for Camel jar projects & artifacts 
 * @author Mikael Thorman
 */
@Alias("camelroutefacet")
@RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
	DependencyFacet.class })
public class CamelRoutePrjFacet extends BaseFacet {

	/**
	 * The velocity engine used to replace data in the supplied templates with the correct info.
	 */
	private final VelocityEngine velocityEngine;
	
	@Inject
	ProjectFactory prjFactory;
	
	// TODO there must be a better way to transfer data from the plugin to the Facet, but I don'y know it now.
	public static String prjDescription = "JAR project for Camel routes";
	/**
	 * If this parameter is set, the pom file for the project with this artifact id will be updated with a dependency to this project.
	 */
	public static String warProjectArtifactId = null;

	public CamelRoutePrjFacet() {
		super();
		velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER,
				"classpath");
		velocityEngine.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		velocityEngine.setProperty(
				RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
				"org.apache.velocity.runtime.log.JdkLogChute");

	}
	
	/* (non-Javadoc)
	 * @see org.jboss.forge.project.Facet#install()
	 */
	@Override
	public boolean install() {
		configureProject();
		createTestRoute();
		updateWarProjectDependencies(warProjectArtifactId);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.jboss.forge.project.Facet#isInstalled()
	 */
	@Override
	public boolean isInstalled() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		// We verify that we have a parent and that it ends with the default parent suffixes
		if (pom.getParent() == null) {
			return false;
		}
		if (!pom.getParent().getGroupId().endsWith("poms.parent")) {
			return false;
		}
		if (!pom.getParent().getArtifactId().endsWith("-parent")) {
			return false;
		}
		
		List<Dependency> deps = pom.getDependencies();
		boolean dependenciesOk = false;
		for (Dependency dependency : deps) {
			if (dependency.getGroupId().equals("org.apache.camel") && dependency.getArtifactId().equals("camel-core")) {
				dependenciesOk = true;
			}
		}
		
		return dependenciesOk;
	}

	// Helper functions ****************************************
	/**
	 * Set the project parent to the main parent project.
	 * Changes the project to a pom project.
	 * Add the poms module
	 */
	private void configureProject() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();

		// Change the POM parent to parent project
		MavenProjectId prjId = NazgulPrjUtil.getParentProjectId(pom, project);
		mergePomFileWithTemplate(prjId, pom, prjDescription);
		// While we are replacing the pom, we do not manually modify & store it 
		// here (it will then overwrite the one we are replacing it with)
	}


	/**
	 * Merges the pom temaplate for this project with the needed data
	 * @param pom	The parent pom
	 * @param parentPrjName	The name of the parent folder
	 */
	private void mergePomFileWithTemplate(MavenProjectId prjId, Model pom, String prjDesc) {
		String parentPomUri = "/template-files/camel/route/pom.xml";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		velocityPlaceholderMap.put("groupId", pom.getGroupId());
		velocityPlaceholderMap.put("artifactIdReplace", pom.getArtifactId());
		velocityPlaceholderMap.put("version", pom.getVersion());
		velocityPlaceholderMap.put("parentGroupId", prjId.getGroupId());
		velocityPlaceholderMap.put("parentArtifactId", prjId.getArtifactId());
		velocityPlaceholderMap.put("parentVersion", prjId.getVersion());
		velocityPlaceholderMap.put("prjDescription", prjDesc);
		
	    // Replace the current pom with the copied/merged
		String targetUri = "../../../pom.xml";
		VelocityContext velocityContext = VelocityUtil.createVelocityContext(velocityPlaceholderMap);
		VelocityUtil.createResourceAbsolute(parentPomUri, velocityContext, targetUri, project, velocityEngine);
	}

	/**
	 * Creates a TestRoute.java file based on the template located 
	 * at src/main/resources/template-files/camel/route/src/main/java/TestRoute.java
	 */
	private void createTestRoute() {
		final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
		Model pom = mvnFacet.getPOM();
		String parentPomUri = "/template-files/camel/route/src/main/java/TestRoute.java";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		velocityPlaceholderMap.put("package", pom.getGroupId() + ".routes");
		
	    // Replace the current pom with the copied/merged
		VelocityContext velocityContext = VelocityUtil.createVelocityContext(velocityPlaceholderMap);
		VelocityUtil.createJavaSource(parentPomUri, velocityContext, project, velocityEngine);
	}

	/**
	 * The Maven project with the given artifactId will be updated with dependencies to this project.
	 * @param warProjectArtifactId
	 */
	private void updateWarProjectDependencies(String warProjectArtifactId) {
		if (warProjectArtifactId == null) {
			// No project supplied -> do nothing
			return;
		}

		Project prj = NazgulPrjUtil.findProjectWithArtifactId(project, warProjectArtifactId, prjFactory);
		if (prj != null) {
			final MavenCoreFacet mvnFacet = prj.getFacet(MavenCoreFacet.class);
			Model pom = mvnFacet.getPOM();
			final MavenCoreFacet thisPrjMvnFacet = project.getFacet(MavenCoreFacet.class);
			Model thisPrjPom = thisPrjMvnFacet.getPOM();
			
			Dependency dependency = new Dependency();
			dependency.setGroupId(thisPrjPom.getGroupId());
			dependency.setArtifactId(thisPrjPom.getArtifactId());
			dependency.setVersion(thisPrjPom.getVersion());
			
			pom.addDependency(dependency);
			mvnFacet.setPOM(pom);
		}
		
	}
}
