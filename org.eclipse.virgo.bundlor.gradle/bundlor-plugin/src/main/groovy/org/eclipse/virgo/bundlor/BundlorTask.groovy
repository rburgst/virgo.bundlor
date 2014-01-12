package org.eclipse.virgo.bundlor

import org.eclipse.jdt.internal.compiler.batch.FileFinder;
import org.gradle.api.DefaultTask
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * The task that launches the bundlor tool via ant.
 * 
 * @author Rainer Burgstaller
 *
 */
class BundlorTask extends DefaultTask {
	@Input @Optional Boolean failOnWarnings = true
	@Input @Optional String bundleName = null
	@Input @Optional String bundleVersion = project.version
	@Input @Optional String bundleVendor = 'SpringSource'
	@Input @Optional String bundleSymbolicName = null
	@Input @Optional String bundleManifestVersion = '2'
	@Input @Optional def importTemplate = []
	@Input @Optional def exportTemplate = []
	@Input @Optional def excludedImports = []
	@Input @Optional def excludedExports = []
	@Input @Optional String manifestTemplateContent = null
	@InputFile @Optional File templatePath = null
	@Input @Optional expansions = []
	@OutputDirectory File outputDir = new File("${project.buildDir}/bundlor")
	@OutputFile def manifest = new File("${outputDir}/META-INF/MANIFEST.MF")
	
	BundlorTask() {
		group = 'Build'
		description = 'Generates an OSGi-compatibile MANIFEST.MF file.'
		enabled = true
	}

	@TaskAction
	void start() {
		if (bundleName == null)
			bundleName = project.description

		project.ant.taskdef(
			resource: 'org/eclipse/virgo/bundlor/ant/antlib.xml',
			classpath: project.configurations.bundlorconf.asPath)

		// the bundlor ant task writes directly to standard out
		// redirect it to INFO level logging, which gradle will
		// deal with gracefully
		logging.captureStandardOutput(LogLevel.INFO)

		// the ant task will throw unless this dir exists
		if (!outputDir.isDirectory())
			outputDir.mkdir()


		logger.info(expansions.toString())
		Properties properties = new Properties()

		if (expansions) {
			expansions.each { key, value ->
				properties[key.toString()] = value.toString()
			}
		}

		File outProps = File.createTempFile("props", ".properties");
		properties.store(outProps.newWriter(), null);

		logger.info("output property file ${outProps.absolutePath}")
		// execute the ant task, and write out the manifest file
		def manifestTemplatePath
		if (templatePath != null) {
			logger.info("Using explicit bundlor manifest template: from ${templatePath}")
			manifestTemplateContent = templatePath.text
			manifestTemplatePath = templatePath.absolutePath
		} else if (manifestTemplateContent == null) {
			assert bundleSymbolicName != null
			assert bundleVendor != null
			assert bundleName != null

			manifestTemplateContent = """\
			Bundle-Vendor: ${bundleVendor}
			Bundle-Version: ${bundleVersion}
			Bundle-Name: ${bundleName}
			Bundle-ManifestVersion: ${bundleManifestVersion}
			Bundle-SymbolicName: ${bundleSymbolicName}
			""".stripIndent()
			manifestTemplateContent += generateTemplateDirective(importTemplate,  "Import-Template")
			manifestTemplateContent += generateTemplateDirective(exportTemplate,  "Export-Template")
			manifestTemplateContent += generateTemplateDirective(excludedExports, "Excluded-Exports")
			manifestTemplateContent += generateTemplateDirective(excludedImports, "Excluded-Imports")
			
			logger.info('Using generated bundlor manifest template:')
			File outTemplate = File.createTempFile("template", ".mf")
			outTemplate.text = manifestTemplateContent
			manifestTemplatePath = outTemplate.absolutePath
		}
		
		logger.info('-------------------------------------------------')
		logger.info(manifestTemplateContent)
		logger.info('-------------------------------------------------')
		project.ant.bundlor(
				enabled: enabled,
				inputPath: project.sourceSets.main.output.classesDir,
				outputPath: outputDir,
				bundleVersion: bundleVersion,
				failOnWarnings: failOnWarnings,
				propertiesPath: outProps.absolutePath,
				manifestTemplatePath: manifestTemplatePath,
				)
	}
	
    def generateTemplateDirective(list, name) {
        def template = ""
        if (!list.isEmpty()) {
            template += "${name}: "
            list.each { entry ->
                template += "\n " + entry
                if (entry != list.last()) {
                    template += ','
                }
            }
            template += "\n"
        }
        return template
    }
}
