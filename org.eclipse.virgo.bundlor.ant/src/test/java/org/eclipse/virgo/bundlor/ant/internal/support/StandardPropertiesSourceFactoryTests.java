/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.bundlor.ant.internal.support;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.PropertySet;
import org.junit.Test;

import org.eclipse.virgo.bundlor.support.properties.PropertiesSource;

public class StandardPropertiesSourceFactoryTests {

    private final StandardPropertiesSourceFactory factory = new StandardPropertiesSourceFactory();

    @Test
    public void create() {
        List<PropertiesSource> properties = this.factory.create("src/test/resources/test.properties", Collections.<PropertySet> emptyList(),
            Collections.<Property> emptyList());
        assertEquals(4, properties.size());
    }

    @Test
    public void propertiesPathNull() {
        List<PropertiesSource> properties = this.factory.create(null, Collections.<PropertySet> emptyList(), Collections.<Property> emptyList());
        assertEquals(3, properties.size());
    }
}
