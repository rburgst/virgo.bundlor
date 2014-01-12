/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package org.eclipse.virgo.bundlor

import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.PropertySet;
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.logging.LogLevel

/**
 * Contribute a 'bundlor' task capable of creating an OSGi manifest. Task is tied
 * to the lifecycle by having the 'jar' task depend on 'bundlor'.
 *
 * @author Chris Beams
 * @author Luke Taylor
 * @see http://www.springsource.org/bundlor (out of date)
 * @see http://static.springsource.org/s2-bundlor/1.0.x/user-guide/html/ch04s02.html (out of date)
 * @see https://www.eclipse.org/virgo/documentation/bundlor-documentation-1.1.2.RELEASE/docs/user-guide/html/index.html
 */
public class BundlorPlugin implements Plugin<Project> {
	
    public void apply(Project project) {

        project.configurations { bundlorconf }

	    project.repositories.maven( {name = 'bundlor'; url = 'http://build.eclipse.org/rt/virgo/maven/bundles/release'})
        project.repositories.maven( {name = 'springsource'; url = 'http://repository.springsource.com/maven/bundles/external'})
        project.dependencies {
             bundlorconf 'org.eclipse.virgo.bundlor:org.eclipse.virgo.bundlor.ant:1.1.2.RELEASE',
                         'org.eclipse.virgo.bundlor:org.eclipse.virgo.bundlor:1.1.2.RELEASE',
                         'org.eclipse.virgo.bundlor:org.eclipse.virgo.bundlor.blint:1.1.2.RELEASE'
        }

		BundlorTask bTask = project.tasks.create("bundlor", BundlorTask.class)
		
		bTask.dependsOn project.compileJava
		bTask.inputs.files project.sourceSets.main.runtimeClasspath

		project.tasks.jar {
			dependsOn 'bundlor'
			inputs.files bTask.manifest
			manifest.from bTask.manifest
		}
    }
}
