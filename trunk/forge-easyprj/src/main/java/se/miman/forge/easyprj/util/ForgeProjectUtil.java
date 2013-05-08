package se.miman.forge.easyprj.util;

import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;

/**
 *	Utility functions for Forge projects
 * 
 * @author Mikael Thorman
 */
public class ForgeProjectUtil {

	/**
	 * Goes though all children & their children for a project with the given name.
	 * It goes through all subfolders recursivly looking for the folder name.
	 * @param name	The folder name to search for
	 * @param prjFactory	Forge prj factory
	 * @param dir	the dir to search from.
	 * @return
	 */
	public static DirectoryResource findSubFolderWithNameRecursive(String name,
			ProjectFactory prjFactory, DirectoryResource dir) {
		for (Resource<?> child : dir.listResources()) {
			if (DirectoryResource.class.isInstance(child)) {
				DirectoryResource subDir = (DirectoryResource)child;
				if (name.equalsIgnoreCase(child.getName())) {
					return subDir;
				} else {
					DirectoryResource subSubDir = findSubFolderWithNameRecursive(name, prjFactory, subDir);
					if (subSubDir != null) {
						return subSubDir;
					}
					// else continue searching in this folder
				}
			} 
		}
		return null;	// Not found
	}

	/**
	 * Goes though all children & their children for a project with the given name.
	 * @param name	The folder name to search for
	 * @param prjFactory	Forge prj factory
	 * @param dir	the dir to search from.
	 * @return
	 */
	public static DirectoryResource findSubFolderWithName(String name,
			ProjectFactory prjFactory, DirectoryResource dir) {
		for (Resource<?> child : dir.listResources()) {
			if (DirectoryResource.class.isInstance(child)) {
				DirectoryResource subDir = (DirectoryResource)child;
				if (name.equalsIgnoreCase(child.getName())) {
					return subDir;
				}
			} 
		}
		return null;	// Not found
	}
}
