/**
 * 
 */
package se.miman.forge.easyprj.facet;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

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
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrintWriter;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.RequiresFacet;

import se.miman.forge.easyprj.dto.MavenProjectId;
import se.miman.forge.easyprj.util.NazgulPrjUtil;
import se.miman.forge.easyprj.util.VelocityUtil;

/**
 * This is the Facet class for Camel war projects & artifacts 
 * @author Mikael Thorman
 */
@Alias("camelwebfacet")
@RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
	DependencyFacet.class })
public class CamelWebPrjFacet extends BaseFacet {

	@Inject
	ProjectFactory prjFactory;

   @Inject
   private ShellPrintWriter writer;

	/**
	 * The velocity engine used to replace data in the supplied templates with the correct info.
	 */
	private final VelocityEngine velocityEngine;
	
	// TODO there must be a better way to transfer data from the plugin to the Facet, but I don'y know it now.
	public static String camelBasePackage = "";
	public static String prjDescription = "Web project for Camel routes";

	public CamelWebPrjFacet() {
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
		return true;
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
		MavenProjectId prjId = NazgulPrjUtil.getParentProjectId(project, prjFactory);
		mergePomFileWithTemplate(prjId, pom, prjDescription);
		// While we are replacing the pom, we do not manually modify & store it 
		// here (it will then overwrite the one we are replacing it with)
		
		if (camelBasePackage == null) {
			Project rootPrj = NazgulPrjUtil.findRootProject(project, prjFactory);
			if (rootPrj == null) {
				// The root project cannot be found
				writer.println(ShellColor.RED, "Error - Couldn't find any root project above this project !");
				return;
			}
			final MavenCoreFacet rootPrjMvnFacet = rootPrj.getFacet(MavenCoreFacet.class);
			Model rootPrjPom = rootPrjMvnFacet.getPOM();

			camelBasePackage = rootPrjPom.getGroupId().trim().replace("-", "").replace("_", "");
		}
		createApplicationContext(camelBasePackage);
		createWebXml();
		createLog4jProperties();
		createReadMeTxt();
	}


	/**
	 * Merges the pom temaplate for this project with the needed data
	 * @param pom	The parent pom
	 * @param parentPrjName	The name of the parent folder
	 */
	private void mergePomFileWithTemplate(MavenProjectId prjId, Model pom, String prjDesc) {
		String parentPomUri = "/template-files/camel/web/pom.xml";
		
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
	 * Creates a applicationContext.xml file based on the template located 
	 * at src/main/resources/template-files/camel/web/main/webapp/WEB-INF/applicationContext.xml
	 * @param parentPrjName	The name of the parent folder
	 */
	private void createApplicationContext(String camelBasePackage) {
		String parentPomUri = "/template-files/camel/web/main/webapp/WEB-INF/applicationContext.xml";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		velocityPlaceholderMap.put("camelBasePackage", camelBasePackage);
		
	    // Replace the current pom with the copied/merged
		String targetUri = "WEB-INF/applicationContext.xml";
		VelocityContext velocityContext = VelocityUtil.createVelocityContext(velocityPlaceholderMap);
		VelocityUtil.createWebResourceAbsolute(parentPomUri, velocityContext, targetUri, project, velocityEngine);
	}

	/**
	 * Creates a web.xml file based on the template located 
	 * at src/main/resources/template-files/camel/web/main/webapp/WEB-INF/web.xml
	 */
	private void createWebXml() {
		String parentPomUri = "/template-files/camel/web/main/webapp/WEB-INF/web.xml";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		
	    // Replace the current pom with the copied/merged
		String targetUri = "WEB-INF/web.xml";
		VelocityContext velocityContext = VelocityUtil.createVelocityContext(velocityPlaceholderMap);
		VelocityUtil.createWebResourceAbsolute(parentPomUri, velocityContext, targetUri, project, velocityEngine);
	}

	/**
	 * Creates a log4j config file based on the template located 
	 * at src/main/resources/template-files/camel/web/main/resources/log4j.properties
	 */
	private void createLog4jProperties() {
		String parentPomUri = "/template-files/camel/web/main/resources/log4j.properties";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		
	    // Replace the current pom with the copied/merged
		String targetUri = "log4j.properties";
		VelocityContext velocityContext = VelocityUtil.createVelocityContext(velocityPlaceholderMap);
		VelocityUtil.createResourceAbsolute(parentPomUri, velocityContext, targetUri, project, velocityEngine);
	}

	/**
	 * Creates a read-me.txt file based on the template located 
	 * at src/main/resources/template-files/camel/web/read-me.txt
	 */
	private void createReadMeTxt() {
		String parentPomUri = "/template-files/camel/web/read-me.txt";
		
		Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
		
	    // Replace the current pom with the copied/merged
		String targetUri = "../../../read-me.txt";
		VelocityContext velocityContext = VelocityUtil.createVelocityContext(velocityPlaceholderMap);
		VelocityUtil.createResourceAbsolute(parentPomUri, velocityContext, targetUri, project, velocityEngine);
	}
}
