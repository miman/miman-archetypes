package se.miman.forge.easyprj.util;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaType;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.project.facets.WebResourceFacet;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.java.JavaResource;

/**
 *	Utility functions for working with velocity files. 
 */
public class VelocityUtil {
	private static final String UTF_8 = "UTF-8";
	
	public String capitalize(String orgString) {
		return StringUtils.capitalize(orgString);
		
	}
	
	public String uncapitalize(String orgString) {
		return StringUtils.uncapitalize(orgString);
	}

	/**
	 * Creates an Velocity template
	 * @param parameter	The map of parameters you want to have in the template, use null if you want to create an empty template. 
	 * @return A velocity template.
	 */
	static public VelocityContext createVelocityContext(
			Map<String, Object> parameter) {
		if (parameter == null) {
			parameter = new HashMap<String, Object>();
		}
		VelocityContext velocityContext = new VelocityContext(parameter);
		return velocityContext;
	}
	
	/**
	 * Stores the template file as the target file after running a velocity merge on the template file.
	 * @param templateFilePath	The template file to store after replacing all velocity placeholders
	 * @param velocityContext	The velocity placeholder mappings
	 * @param targetFilePath	The target file path + name
	 * @return	The target file
	 */
	static public FileResource<?> createResourceAbsolute(String templateFilePath, VelocityContext velocityContext, String targetFilePath, 
												Project project, VelocityEngine velocityEngine) {
		ResourceFacet resources = project.getFacet(ResourceFacet.class);

		StringWriter stringWriter = new StringWriter();
		velocityEngine.mergeTemplate(templateFilePath, UTF_8,
				velocityContext, stringWriter);

		System.out.println("Storing file in: " + targetFilePath);
		FileResource<?> createdResource = resources.createResource(
				stringWriter.toString().toCharArray(),
				targetFilePath);
		return createdResource;
	}

	/**
	 * Stores the template file as the target file after running a velocity merge on the template file.
	 * @param templateFilePath	The template file to store after replacing all velocity placeholders
	 * @param velocityContext	The velocity placeholder mappings
	 * @param targetFilePath	The target file path + name
	 * @return	The target file
	 */
	static public FileResource<?> createWebResourceAbsolute(String templateFilePath, VelocityContext velocityContext, String targetFilePath, 
												Project project, VelocityEngine velocityEngine) {
		WebResourceFacet resources = project.getFacet(WebResourceFacet.class);

		StringWriter stringWriter = new StringWriter();
		velocityEngine.mergeTemplate(templateFilePath, UTF_8,
				velocityContext, stringWriter);

		System.out.println("Storing web file in: " + targetFilePath);
		FileResource<?> createdResource = resources.createWebResource(
				stringWriter.toString().toCharArray(),
				targetFilePath);
		return createdResource;
	}

	/**
	 * Stores the template file as the target file after running a velocity merge on the template file.
	 * The correct file path is retrieved from the Java class.
	 * @param templateFilePath	The template file to store after replacing all velocity placeholders
	 * @param velocityContext	The velocity placeholder mappings
	 * @return	The target file
	 */
	static public JavaResource createJavaSource(String template, VelocityContext velocityContext, 
			Project project, VelocityEngine velocityEngine) {
		JavaSourceFacet java = project.getFacet(JavaSourceFacet.class);

		StringWriter stringWriter = new StringWriter();
		velocityEngine.mergeTemplate(template, UTF_8, velocityContext,
				stringWriter);

		JavaType<?> serviceClass = JavaParser.parse(JavaType.class,
				stringWriter.toString());
		try {
			JavaResource saveJavaSource = java.saveJavaSource(serviceClass);
			return saveJavaSource;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
