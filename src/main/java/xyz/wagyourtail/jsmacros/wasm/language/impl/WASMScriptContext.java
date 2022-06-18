package xyz.wagyourtail.jsmacros.wasm.language.impl;

import io.github.kawamuray.wasmtime.Module;
import io.github.kawamuray.wasmtime.*;
import io.github.kawamuray.wasmtime.wasi.WasiCtx;
import io.github.kawamuray.wasmtime.wasi.WasiCtxBuilder;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext;

import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WASMScriptContext extends BaseScriptContext<WASMScriptContext.WasmInstance> {


    public WASMScriptContext(BaseEvent event, File file) {
        super(event, file);
    }

    @Override
    public synchronized void closeContext() {
        super.closeContext();
        getContext().close();
    }

    @Override
    protected void finalize() {
        closeContext();
    }

    public static class WasmInstance implements AutoCloseable {
        public final WasiCtx wasiCtx;
        public final Linker linker;
        private Memory memory;
        public final Store<Void> store;
        public final Module module;


        private Func main;
        protected List<Func> exports = new ArrayList<>();
        public List<Object> javaObjects = new ArrayList<>();


        public WasmInstance(byte[] bytes, boolean watBytes) {
            wasiCtx = new WasiCtxBuilder().inheritStdout().inheritStderr().build();
            store = Store.withoutData(wasiCtx);
            linker = new Linker(store.engine());
            if (watBytes) {
                module = new Module(store.engine(), bytes);
            } else {
                module = Module.fromBinary(store.engine(), bytes);
            }
            WasiCtx.addToLinker(linker);
        }

        public Memory getMemory() {
            return memory;
        }

        public void runMain() {
            linker.module(store, "", module);
            memory = linker.get(store, "", "memory").get().memory();
            synchronized (this) {
                main = linker.get(store, "", "main").get().func();
            }
            try {
                WasmFunctions.Consumer0 main_func = WasmFunctions.consumer(store, main);
                main_func.accept();
            } finally {
                synchronized (this) {
                    main.close();
                    main = null;
                }
            }
        }

        @Override
        public void close() {
            for (Func export : exports) {
                export.close();
            }
            synchronized (this) {
                if (main != null) {
                    main.close();
                }
            }
            store.close();
            memory.close();
            linker.close();
            wasiCtx.close();
            for (Object javaObject : javaObjects) {
                if (javaObject instanceof AutoCloseable) {
                    try {
                        ((AutoCloseable) javaObject).close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (javaObject instanceof Closeable) {
                    try {
                        ((Closeable) javaObject).close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

}
