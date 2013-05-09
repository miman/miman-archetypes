package se.miman.forge.easyprj.util;

import org.apache.maven.model.Model;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;

import se.miman.forge.easyprj.dto.MavenProjectId;

/**
 * This is a helper class for project organized according to the Nazgul project
 * philosophy. Info about this philosophy can be found here:
 * 
 * https://bytebucket.org/lennartj/nazgul_tools/wiki/index.html
 * 
 * @author Mikael Thorman
 */
public class NazgulPrjUtil {

	/**
	 * Retrieves the groupId/artifactId & version for the parent project
	 * (according to the Nazgul philosophy).
	 * 
	 * s * That is the parent prj under the poms directory.
	 * 
	 * @param pom
	 *            The Forge POM object
	 * @param project
	 *            The project we are originating the request from
	 * @return The parent project id's
	 */
	static public MavenProjectId getParentProjectId(Project project,
			ProjectFactory prjFactory) {
		MavenProjectId reply = new MavenProjectId();

		Project parentPrj = findParentProject(project, prjFactory);
		if (parentPrj == null) {
			return null;
		}

		MavenCoreFacet maprentPrjMvnFacet = parentPrj
				.getFacet(MavenCoreFacet.class);
		Model parentPom = maprentPrjMvnFacet.getPOM();

		reply.setGroupId(parentPom.getGroupId());
		reply.setArtifactId(parentPom.getArtifactId());
		reply.setVersion(parentPom.getVersion());
		System.out.println("Parent maven id found: " + reply);

		return reply;
	}

	/**
	 * Traverses up from the given project until it finds the root directory
	 * (the one with a poms sub-folder)
	 * 
	 * @param project
	 * @return
	 */
	static public Project findRootProject(Project project,
			ProjectFactory prjFactory) {
		
		DirectoryResource rootDir = findRootDirectory(project, prjFactory);
		if (rootDir == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		Project prj = prjFactory.createProject(rootDir);
		return prj;
	}

	/**
	 * Goes though the root projects children & their children for the parent
	 * 
	 * @param project
	 *            The project we are starting our search from
	 * @param projectLocator
	 *            The project locator
	 * @return The parent project (or null if not found)
	 */
	@SuppressWarnings("unchecked")
	static public Project findParentProject(Project project,
			ProjectFactory prjFactory) {
		DirectoryResource rootDir = findRootDirectory(project, prjFactory);
		// Now find the parent project under the poms subdirectory
		for (Resource<?> child : rootDir.listResources()) {
			if ("poms".equals(child.getName())) {
				System.out.println("poms subdir found for prj: "
						+ child.getFullyQualifiedName());
				if (DirectoryResource.class.isInstance(child)) {
					Resource<?> parentDir = ((DirectoryResource) child)
							.getChild(child.getParent().getName() + "-parent");
					return prjFactory
							.createProject((DirectoryResource) parentDir);
				}
			}
		}

		return null;
	}

	/**
	 * Goes though the root projects children & their children for a project
	 * with the given artifactId
	 * 
	 * @param project
	 *            The project we are starting our search from
	 * @param artifactId
	 * @param projectLocator
	 *            The project locator
	 * @return The project with the given artifact id (or null if not found)
	 */
	static public Project findProjectWithArtifactId(Project project,
			String artifactId, ProjectFactory prjFactory) {
		DirectoryResource rootDir = findRootDirectory(project, prjFactory);
		Project prj = findSubProjectWithArtifactId(artifactId, prjFactory,
				rootDir);

		return prj;
	}

	/**
	 * Goes though all children & their children for a project with the given
	 * artifactId
	 * 
	 * @param artifactId
	 * @param projectLocator
	 * @param rootDir
	 * @return
	 */
	private static Project findSubProjectWithArtifactId(String artifactId,
			ProjectFactory prjFactory, DirectoryResource rootDir) {
		for (Resource<?> child : rootDir.listResources()) {
			if (DirectoryResource.class.isInstance(child)) {
				if (prjFactory.containsProject((DirectoryResource) child)) {
					@SuppressWarnings("unchecked")
					Project prj = prjFactory
							.createProject((DirectoryResource) child);
					MavenCoreFacet mvnFacet = prj
							.getFacet(MavenCoreFacet.class);
					Model pom = mvnFacet.getPOM();
					if (artifactId.compareToIgnoreCase(pom.getArtifactId()) == 0) {
						// This is the project with the given artifactId
						return prj;
					} else {
						// Search all sub-folders
						Project p = findSubProjectWithArtifactId(artifactId,
								prjFactory, (DirectoryResource) child);
						if (p != null) {
							return p;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Traverses up from the given project until it finds the root directory
	 * (the one with a poms sub-folder)
	 * 
	 * @param project
	 * @return
	 */
	static private DirectoryResource findRootDirectory(Project project,
			ProjectFactory prjFactory) {
		DirectoryResource currentDir = project.getProjectRoot();

		while (currentDir != null) {
			DirectoryResource pomsDir = ForgeProjectUtil.findSubFolderWithName(
					"poms", prjFactory, currentDir);
			if (pomsDir != null) {
				return currentDir;
			} else {
				// See if the current folders parent folder is the root
				currentDir = (DirectoryResource) currentDir.getParent();
			}
		}
		return null; // Root was not found
	}
}
