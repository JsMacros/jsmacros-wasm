package xyz.wagyourtail.jsmacros.wasm.language.impl;

import org.wasmer.Instance;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext;
import xyz.wagyourtail.jsmacros.core.language.BaseWrappedException;
import xyz.wagyourtail.jsmacros.core.language.EventContainer;

import java.io.File;

public class WASMLanguageDefinition extends BaseLanguage<Instance> {

    public WASMLanguageDefinition(String extension, Core<?, ?> runner) {
        super(extension, runner);
    }

    public void innerExec(byte[] bytes) {

    }

    @Override
    protected void exec(EventContainer<Instance> ctx, ScriptTrigger macro, BaseEvent event) throws Exception {

    }

    @Override
    protected void exec(EventContainer<Instance> ctx, String script, BaseEvent event) throws Exception {

    }

    @Override
    public BaseWrappedException<?> wrapException(Throwable ex) {
        return null;
    }

    @Override
    public BaseScriptContext<Instance> createContext(BaseEvent event, File file) {
        return null;
    }

}
