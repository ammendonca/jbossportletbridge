/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.portletbridge.test.facelet;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.portal.api.PortalTest;
import org.jboss.arquillian.portal.api.PortalURL;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.portletbridge.test.TestDeployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@RunWith(Arquillian.class)
@PortalTest
public class FaceletCompositionDefineTest {

    @Deployment()
    public static WebArchive createDeployment() {
        WebArchive wa = TestDeployment.createDeployment()
                .addAsWebResource("pages/facelet/compositionDefine.xhtml", "home.xhtml")
                .addAsWebResource("pages/facelet/commonContent.xhtml", "commonContent.xhtml")
                .addAsWebResource("pages/facelet/commonFooter.xhtml", "commonFooter.xhtml")
                .addAsWebResource("pages/facelet/commonHeader.xhtml", "commonHeader.xhtml")
                .addAsWebResource("pages/facelet/commonLayout.xhtml", "commonLayout.xhtml");
        TestDeployment.addWebXml(wa);
        TestDeployment.addPortletXml(wa);
        return wa;
    }

    @FindBy(xpath = "//h1[contains(@id,'header')]")
    private WebElement header;

    @FindBy(xpath = "//h1[contains(@id,'content')]")
    private WebElement content;

    @FindBy(xpath = "//h1[contains(@id,'footer')]")
    private WebElement footer;

    protected static final String headerContent = "This is default header";
    protected static final String contentContent = "This is defined content";
    protected static final String footerContent = "This is defined footer";

    @ArquillianResource
    @PortalURL
    URL portalURL;
    @Drone
    WebDriver driver;

    @Test
    @RunAsClient
    public void testFaceletCompositionDefine() throws Exception {
        driver.get(portalURL.toString());

        assertTrue("Check that page contains header element.", Graphene.element(header).isVisible().apply(driver));
        assertTrue("Check that page contains content element.", Graphene.element(content).isVisible().apply(driver));
        assertTrue("Check that page contains footer element.", Graphene.element(footer).isVisible().apply(driver));

        assertTrue("Header should contain: " + headerContent,
                Graphene.element(header).textEquals(headerContent).apply(driver));
        assertTrue("Content should contain: " + contentContent,
                Graphene.element(content).textEquals(contentContent).apply(driver));
        assertTrue("Footer should contain: " + footerContent,
                Graphene.element(footer).textEquals(footerContent).apply(driver));
    }
}
