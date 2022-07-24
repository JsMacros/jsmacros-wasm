package xyz.wagyourtail.jsmacros.wasmtime.language.impl;

import xyz.wagyourtail.jsmacros.client.JsMacros;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.config.ScriptTrigger;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext;
import xyz.wagyourtail.jsmacros.core.language.EventContainer;
import xyz.wagyourtail.jsmacros.wasmtime.WasmTimeExtension;
import xyz.wagyourtail.jsmacros.wasmtime.WasmHelper;

import java.io.File;
import java.nio.file.Files;

public class WasmTimeLanguageDefinition extends BaseLanguage<WasmTimeScriptContext.WasmInstance, WasmTimeScriptContext> {

    public WasmTimeLanguageDefinition(WasmTimeExtension extension, Core<?, ?> runner) {
        super(extension, runner);
    }


    @Override
    protected void exec(EventContainer<WasmTimeScriptContext> ctx, ScriptTrigger macro, BaseEvent event) throws Exception {
        byte[] b = Files.readAllBytes(ctx.getCtx().getFile().toPath());
        WasmTimeScriptContext.WasmInstance instance = new WasmTimeScriptContext.WasmInstance(b, ctx.getCtx().getFile().getName().endsWith(".wat"));
        ctx.getCtx().setContext(instance);

        retrieveLibs(ctx.getCtx()).forEach((k, v) -> WasmHelper.registerLibrary(instance, k, v));

        instance.runMain(event, ctx.getCtx().getFile(), ctx);
    }

    @Override
    protected void exec(EventContainer<WasmTimeScriptContext> ctx, String lang, String script, BaseEvent event) throws Exception {
        WasmTimeScriptContext.WasmInstance instance = new WasmTimeScriptContext.WasmInstance(script.getBytes(), lang.endsWith("wat"));
        ctx.getCtx().setContext(instance);

        retrieveLibs(ctx.getCtx()).forEach((k, v) -> WasmHelper.registerLibrary(instance, k, v));

        instance.runMain(event, ctx.getCtx().getFile(), ctx);
    }

    @Override
    public WasmTimeScriptContext createContext(BaseEvent event, File file) {
        return new WasmTimeScriptContext(event, file);
    }

}
