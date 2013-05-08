package se.miman.forge.easyprj.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;

import se.miman.forge.easyprj.dto.MavenProjectId;

/**
 * This is a helper class for project organized according to the Nazgul project philosophy.
 * Info about this philosophy can be found here:
 * 
 * https://bytebucket.org/lennartj/nazgul_tools/wiki/index.html
 *  
 * @author Mikael Thorman
 */
public class NazgulPrjUtil {

	/**
	 * Finds the correct parent project and sets this in this project pom file
	 * @param pom
	 */
	static public void setParentProject(Model pom, Project project) {

		Resource<?> parentFolder = findParentProject(project);
		Resource<?> pomFile = parentFolder.getChild("pom.xml");
		InputStream inputStream = pomFile.getResourceInputStream();
		String groupId = null;
		String artifactId = null;
		String version = null;
		try {
			String myString = IOUtils.toString(inputStream, "UTF-8");
			
			// We don't want to get the values for the parent project, but the actual project.
			int parentEndIndex = myString.indexOf("</parent>");
			
			int startIndex = myString.indexOf("<groupId>", parentEndIndex);
			if (startIndex > 0) {
				startIndex += "<groupId>".length();
				int endIndex = myString.indexOf("</groupId>");
				groupId = myString.substring(startIndex, endIndex);
				System.out.println("Group id was found: " + groupId);
			} else {
				System.out.println("The group id was not found");
				return;
			}
			startIndex = myString.indexOf("<artifactId>", parentEndIndex);
			if (startIndex > 0) {
				startIndex += "<artifactId>".length();
				int endIndex = myString.indexOf("</artifactId>");
				artifactId = myString.substring(startIndex, endIndex);
				System.out.println("artifactId was found: " + artifactId);
			} else {
				System.out.println("The artifactId was not found");
				return;
			}
			startIndex = myString.indexOf("<version>", parentEndIndex);
			if (startIndex > 0) {
				startIndex += "<version>".length();
				int endIndex = myString.indexOf("</version>");
				version = myString.substring(startIndex, endIndex);
				System.out.println("version was found: " + version);
			} else {
				System.out.println("The version was not found");
				return;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		if (pom.getParent() == null) {
			pom.setParent(new Parent());
		}
		pom.getParent().setGroupId(groupId);
		pom.getParent().setArtifactId(artifactId);
		pom.getParent().setVersion(version);
	}
	
	/**
	 * Finds the root project file path (based on the presence of a poms subdirectory precense)
	 * @param prjPath The file path to the current project
	 * @return	The file path to the root
	 */
	static private Resource<?> findParentProject(Project project) {
		Resource<?> parent = project.getProjectRoot().getParent();
		
		while (parent != null) {
			File f = new File(parent.getFullyQualifiedName());
			File[] matchingFiles = f.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.startsWith("poms");
			    }
			});
			if (matchingFiles.length > 0) {
				System.out.println("poms subdir found for prj: " + parent.getFullyQualifiedName());
				return parent.getChild("poms").getChild(parent.getName() + "-parent");
			}
			
			parent = parent.getParent();
		}
		
		return null;	// Parent prj not found
	}

	/**
	 * Retrives the groupId/artifactId & version for the parent project (according to the Nazgul philosophy).
	 * 
	 * That is the parent prj under the poms directiry.
	 * @param pom	The Forge POM object
	 * @param project	The project we are originating the request from
	 * @return	The parent project id's
	 */
	static public MavenProjectId getParentProjectId(Model pom, Project project) {
		MavenProjectId reply = new MavenProjectId();
		Resource<?> parentFolder = findParentProject(project);
		Resource<?> pomFile = parentFolder.getChild("pom.xml");
		InputStream inputStream = pomFile.getResourceInputStream();
		try {
			String myString = IOUtils.toString(inputStream, "UTF-8");
			
			// We don't want to get the values for the parent project, but the actual project.
			int parentEndIndex = myString.indexOf("</parent>");
			
			int startIndex = myString.indexOf("<groupId>", parentEndIndex);
			if (startIndex > 0) {
				startIndex += "<groupId>".length();
				int endIndex = myString.indexOf("</groupId>", startIndex);
				reply.setGroupId(myString.substring(startIndex, endIndex));
				System.out.println("Group id was found: " + reply.getGroupId());
			} else {
				System.out.println("The group id was not found");
				return null;
			}
			startIndex = myString.indexOf("<artifactId>", parentEndIndex);
			if (startIndex > 0) {
				startIndex += "<artifactId>".length();
				int endIndex = myString.indexOf("</artifactId>", startIndex);
				reply.setArtifactId(myString.substring(startIndex, endIndex));
				System.out.println("artifactId was found: " + reply.getArtifactId());
			} else {
				System.out.println("The artifactId was not found");
				return null;
			}
			startIndex = myString.indexOf("<version>", parentEndIndex);
			if (startIndex > 0) {
				startIndex += "<version>".length();
				int endIndex = myString.indexOf("</version>", startIndex);
				reply.setVersion(myString.substring(startIndex, endIndex));
				System.out.println("version was found: " + reply.getVersion());
			} else {
				System.out.println("The version was not found");
				return null;
			}
			return reply;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Finds the groupId of the root project
	 * @param project The forge current project
	 * @return	The groupId of the root project
	 */
	static private Resource<?> findRootProject(Project project) {
		Resource<?> parent = project.getProjectRoot().getParent();
		
		while (parent != null) {
			File f = new File(parent.getFullyQualifiedName());
			File[] matchingFiles = f.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.startsWith("poms");
			    }
			});
			if (matchingFiles.length > 0) {
				System.out.println("poms subdir found for prj: " + parent.getFullyQualifiedName());
				return parent;
			}
			
			parent = parent.getParent();
		}
		
		return null;	// Parent prj not found
	}

	/**
	 * Retrieves the groupId for the root project (according to the Nazgul philosophy).
	 * 
	 * That is the parent prj under the poms directory.
	 * @param pom	The Forge POM object
	 * @param project	The project we are originating the request from
	 * @return	The root project group id
	 */
	static public String getRootProjectGroupId(Model pom, Project project) {
		Resource<?> parentFolder = findRootProject(project);
		Resource<?> pomFile = parentFolder.getChild("pom.xml");
		InputStream inputStream = pomFile.getResourceInputStream();
		try {
			String myString = IOUtils.toString(inputStream, "UTF-8");
			
			// We don't want to get the values for the parent project, but the actual project.
			int parentEndIndex = myString.indexOf("</parent>");
			
			int startIndex = myString.indexOf("<groupId>", parentEndIndex);
			if (startIndex > 0) {
				startIndex += "<groupId>".length();
				int endIndex = myString.indexOf("</groupId>", startIndex);
				return myString.substring(startIndex, endIndex);
			} else {
				System.out.println("The group id was not found");
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Goes though the root projects children & their children for a project with the given artifactId
	 * @param project
	 * @param artifactId
	 * @param projectLocator
	 * @return
	 */
	static public Project findProjectWithArtifactId(Project project, String artifactId, ProjectFactory prjFactory) {
		DirectoryResource rootDir = findRootDirectory(project, prjFactory);
		Project prj = findSubProjectWithArtifactId(artifactId, prjFactory, rootDir);
		
		return prj;
	}

	/**
	 * Goes though all children & their children for a project with the given artifactId
	 * @param artifactId
	 * @param projectLocator
	 * @param rootDir
	 * @return
	 */
	private static Project findSubProjectWithArtifactId(String artifactId,
			ProjectFactory prjFactory, DirectoryResource rootDir) {
		for (Resource<?> child : rootDir.listResources()) {
			if (DirectoryResource.class.isInstance(child)) {
				if (prjFactory.containsProject((DirectoryResource)child)) {
					@SuppressWarnings("unchecked")
					Project prj = prjFactory.createProject((DirectoryResource)child);
					MavenCoreFacet mvnFacet = prj.getFacet(MavenCoreFacet.class);
					Model pom = mvnFacet.getPOM();
					if (artifactId.compareToIgnoreCase(pom.getArtifactId()) == 0) {
						// This is the project with the given artifactId
						return prj;
					} else {
						// Search all sub-folders
						Project p = findSubProjectWithArtifactId(artifactId,
								prjFactory, (DirectoryResource)child);
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
	 * Traverses up from the given project until it finds the root directory (the one with a poms sub-folder)
	 * @param project
	 * @return
	 */
	static public DirectoryResource findRootDirectory(Project project, ProjectFactory prjFactory) {
		DirectoryResource currentDir = project.getProjectRoot();
		
		while(currentDir != null) {
			DirectoryResource pomsDir = ForgeProjectUtil.findSubFolderWithName("poms", prjFactory, currentDir);
			if (pomsDir != null) {
				return currentDir;			
			} else {
				// See if the current folders parent folder is the root
				currentDir = (DirectoryResource)currentDir.getParent();	
			}
		}
		return null;	// Root was not found	
	}
}
