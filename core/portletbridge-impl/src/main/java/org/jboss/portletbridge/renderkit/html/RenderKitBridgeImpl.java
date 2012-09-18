package org.jboss.portletbridge.renderkit.html;

import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitWrapper;
import javax.faces.render.Renderer;

import org.jboss.portletbridge.renderkit.richfaces.FileUploadRendererRichFacesImpl;

public class RenderKitBridgeImpl extends RenderKitWrapper {

    private RenderKit wrapped;

    public RenderKitBridgeImpl(RenderKit wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Renderer getRenderer(String family, String rendererType) {

        Renderer renderer = super.getRenderer(family, rendererType);

        if ("org.richfaces.FileUpload".equals(family) && "org.richfaces.FileUploadRenderer".equals(rendererType)) {
            renderer = new FileUploadRendererRichFacesImpl(renderer);
        }

        return renderer;
    }

    @Override
    public RenderKit getWrapped() {
        return wrapped;
    }

}
