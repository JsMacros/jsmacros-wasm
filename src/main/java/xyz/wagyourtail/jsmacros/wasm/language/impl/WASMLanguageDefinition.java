package xyz.wagyourtail.jsmacros.wasm.language.impl;

import xyz.wagyourtail.jsmacros.client.JsMacros;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext;
import xyz.wagyourtail.jsmacros.core.language.BaseWrappedException;
import xyz.wagyourtail.jsmacros.core.language.EventContainer;
import xyz.wagyourtail.jsmacros.wasm.client.WasmHelper;

import java.io.File;
import java.nio.file.Files;

public class WASMLanguageDefinition extends BaseLanguage<WASMScriptContext.WasmInstance> {
    public static final WASMLanguageDefinition WASM = new WASMLanguageDefinition(".wasm", JsMacros.core);
    public static final WASMLanguageDefinition WAT = new WASMLanguageDefinition(".wat", JsMacros.core);

    protected WASMLanguageDefinition(String extension, Core<?, ?> runner) {
        super(extension, runner);
    }


    @Override
    protected void exec(EventContainer<WASMScriptContext.WasmInstance> ctx, ScriptTrigger macro, BaseEvent event) throws Exception {
        byte[] b = Files.readAllBytes(ctx.getCtx().getFile().toPath());
        WASMScriptContext.WasmInstance instance = new WASMScriptContext.WasmInstance(b, ctx.getCtx().getFile().getName().endsWith(".wat"));
        ctx.getCtx().setContext(instance);

        retrieveLibs(ctx.getCtx()).forEach((k, v) -> WasmHelper.registerLibrary(instance, k, v));

        instance.runMain(event, ctx.getCtx().getFile(), ctx);
    }

    @Override
    protected void exec(EventContainer<WASMScriptContext.WasmInstance> ctx, String script, BaseEvent event) throws Exception {
        WASMScriptContext.WasmInstance instance = new WASMScriptContext.WasmInstance(script.getBytes(), true);
        ctx.getCtx().setContext(instance);

        retrieveLibs(ctx.getCtx()).forEach((k, v) -> WasmHelper.registerLibrary(instance, k, v));

        instance.runMain(event, ctx.getCtx().getFile(), ctx);
    }

    @Override
    public BaseWrappedException<?> wrapException(Throwable ex) {
        //TODO, figure out what the hell to do here...
        return null;
    }

    @Override
    public BaseScriptContext<WASMScriptContext.WasmInstance> createContext(BaseEvent event, File file) {
        return new WASMScriptContext(event, file);
    }

}
