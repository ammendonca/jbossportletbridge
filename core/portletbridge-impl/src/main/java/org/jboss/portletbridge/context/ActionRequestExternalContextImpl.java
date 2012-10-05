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
package org.jboss.portletbridge.context;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.StateAwareResponse;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;
import javax.portlet.faces.Bridge;

/**
 * @author asmirnov
 *
 */
public class ActionRequestExternalContextImpl extends PortletExternalContextImpl {

    /**
     * @param context
     * @param request
     * @param response
     */
    public ActionRequestExternalContextImpl(PortletContext context, ActionRequest request, ActionResponse response) {
        super(context, request, response);
    }

    // ============================================================
    // public methods
    @Override
    public PortletContext getContext() {
        return (PortletContext) super.getContext();
    }

    public ActionRequest getActionRequest() {
        return (ActionRequest) super.getRequest();
    }

    public ActionResponse getActionResponse() {
        return (ActionResponse) super.getResponse();
    }

    /**
     * @param url
     * @return
     */
    @Override
    protected String createActionUrl(PortalActionURL actionUrl, boolean escape) {
        String viewIdFromUrl = bridgeContext.getFacesViewIdFromPath(actionUrl.getPath());
        actionUrl.setParameter(bridgeContext.getBridgeConfig().getViewIdRenderParameterName(), viewIdFromUrl);

        StateAwareResponse stateResponse = (StateAwareResponse) getResponse();

        for (Entry<String, String[]> parameter : actionUrl.getParameters().entrySet()) {
            String key = parameter.getKey();
            String[] value = parameter.getValue();

            if (key.equals(Bridge.PORTLET_MODE_PARAMETER)) {
                if (null != value) {
                    PortletMode mode = new PortletMode(value[0]);
                    try {
                        stateResponse.setPortletMode(mode);
                    } catch (PortletModeException e) {
                        // only valid modes supported.
                    }
                }
            } else if (key.equals(Bridge.PORTLET_WINDOWSTATE_PARAMETER)) {
                if (null != value) {
                    WindowState state = new WindowState(value[0]);
                    try {
                        stateResponse.setWindowState(state);
                    } catch (WindowStateException e) {
                        // only valid window states supported.
                    }
                }
            } else if (key.equals(Bridge.PORTLET_SECURE_PARAMETER)) {
                // ignore
            } else {
                stateResponse.setRenderParameter(key, value);
            }
        }
        return escapeUrl(escape, actionUrl.toString());
    }

    @Override
    protected String createPartialActionUrl(PortalActionURL portalUrl) {
        return createActionUrl(portalUrl, isStrictEscaped(portalUrl.toString()));
    }

    @Override
    protected String createRenderUrl(PortalActionURL portalUrl, boolean escape, Map<String, List<String>> parameters) {
        return ACTION_URL_DO_NOTHITG;
    }

    @Override
    protected String createResourceUrl(PortalActionURL portalUrl, boolean escape) {
        return RESOURCE_URL_DO_NOTHITG;
    }

    public void redirect(String url) throws IOException {
        if (null == url || url.length() < 0) {
            throw new NullPointerException("Path to redirect is null");
        }

        PortalActionURL actionURL = new PortalActionURL(url);
        Map<String, String[]> urlParams = null;
        if (null != encodedActionUrlParameters) {
            urlParams = encodedActionUrlParameters.get(url);
        }

        if (null != urlParams) {
            PortalUrlQueryString queryString = new PortalUrlQueryString(null);
            queryString.setParameters(urlParams);

            Map<String, String[]> publicParamMap = getActionRequest().getPublicParameterMap();
            if (null != publicParamMap && !publicParamMap.isEmpty()) {
                for (Map.Entry<String, String[]> entry : publicParamMap.entrySet()) {
                    String key = entry.getKey();

                    if (!queryString.hasParameter(key)) {
                        for (String param : entry.getValue()) {
                            queryString.addParameter(key, param);
                        }
                    }
                }
            }

            bridgeContext.setRenderRedirectQueryString(queryString.toString());
        } else if (url.startsWith("#") || (!actionURL.isInContext(getRequestContextPath()))
            || "true".equalsIgnoreCase(actionURL.getParameter(Bridge.DIRECT_LINK))) {
            getPortletFlash().doLastPhaseActions(FacesContext.getCurrentInstance(), true);
            getActionResponse().sendRedirect(url);
        } else {
            redirect(encodeActionURL(url));
        }
        FacesContext context = FacesContext.getCurrentInstance();
        getPortletFlash().doLastPhaseActions(context, true);
        context.responseComplete();
    }

    // ============================================================
    // non-public methods

    @Override
    public String getRequestContentType() {
        return getActionRequest().getContentType();
    }

    @Override
    public int getRequestContentLength() {
        return getActionRequest().getContentLength();
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        try {
            getActionRequest().setCharacterEncoding(encoding);
        } catch (IllegalStateException e) {
            // No op
        }
    }

    @Override
    public String getRequestCharacterEncoding() {
        return getActionRequest().getCharacterEncoding();
    }

    protected String getRequestHeader(String name) {
        if ("CONTENT-TYPE".equalsIgnoreCase(name)) {
            if (null == contentType) {
                constructContentType();
            }
            return contentType;
        }
        if ("CONTENT-LENGTH".equalsIgnoreCase(name)) {
            if (null == contentLength) {
                constructContentLength();
            }
            return contentLength;
        }

        return super.getRequestHeader(name);
    }

    protected Enumeration<String> getRequestHeaderNames() {
        List<String> names = new ArrayList<String>();
        Enumeration<String> propNames = super.getRequestHeaderNames();
        while (propNames.hasMoreElements()) {
            String name = (String) propNames.nextElement();
            names.add(name);
        }
        names.add("CONTENT-TYPE");
        names.add("CONTENT-LENGTH");
        return Collections.enumeration(names);
    }

    protected String[] getRequestHeaderValues(String name) {
        if ("CONTENT-TYPE".equalsIgnoreCase(name)) {
            if (null == contentType) {
                constructContentType();
            }
            return new String[] { contentType };
        }
        if ("CONTENT-LENGTH".equalsIgnoreCase(name)) {
            if (null == contentLength) {
                constructContentLength();
            }
            return new String[] { contentLength };
        }

        return super.getRequestHeaderValues(name);
    }

    @Override
    public void addResponseHeader(String name, String value) {
        if ("X-Portlet-Mode".equals(name)) {
            try {
                getActionResponse().setPortletMode(new PortletMode(value));
            } catch (PortletModeException e) {
                throw new RuntimeException("Cant set portlet mode '" + value + "'", e);
            }
        } else if ("X-Window-State".equals(name)) {
            try {
                getActionResponse().setWindowState(new WindowState(value));
            } catch (WindowStateException e) {
                throw new RuntimeException("Cant set window state '" + value + "'", e);
            }
        } else {
            super.addResponseHeader(name, value);
        }
    }

    @Override
    public void setResponseHeader(String name, String value) {
        if ("X-Portlet-Mode".equals(name)) {
            try {
                getActionResponse().setPortletMode(new PortletMode(value));
            } catch (PortletModeException e) {
                throw new RuntimeException("Cant set portlet mode '" + value + "'", e);
            }
        } else if ("X-Window-State".equals(name)) {
            try {
                getActionResponse().setWindowState(new WindowState(value));
            } catch (WindowStateException e) {
                throw new RuntimeException("Cant set window state '" + value + "'", e);
            }
        } else {
            super.setResponseHeader(name, value);
        }
    }

    @Override
    public boolean isResponseCommitted() {
        return true;
    }

    @Override
    public void responseSendError(int statusCode, String message) throws IOException {
        // TODO - set error code for render phase.
    }

    @Override
    public int getResponseBufferSize() {
        return 0;
    }

    @Override
    public OutputStream getResponseOutputStream() throws IOException {
        return null;
    }

    @Override
    public Writer getResponseOutputWriter() throws IOException {
        return null;
    }

    @Override
    public void responseFlushBuffer() throws IOException {
        getPortletFlash().doLastPhaseActions(FacesContext.getCurrentInstance(), true);
    }

    @Override
    public void responseReset() {

    }

    @Override
    public void setResponseBufferSize(int size) {

    }

    @Override
    public void setResponseContentLength(int length) {

    }

    @Override
    public void setResponseContentType(String contentType) {

    }

}
