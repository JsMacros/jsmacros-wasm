package xyz.wagyourtail.jsmacros.wasm.language.impl;

import org.wasmer.Instance;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext;

import java.io.File;

public class WASMScriptContext extends BaseScriptContext<Instance> {
    public WASMScriptContext(BaseEvent event, File file) {
        super(event, file);
    }

    @Override
    public synchronized void closeContext() {
        super.closeContext();
        getContext().close();
    }

}
