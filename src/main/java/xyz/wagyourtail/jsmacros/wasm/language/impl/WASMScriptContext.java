package xyz.wagyourtail.jsmacros.wasm.language.impl;

import io.github.kawamuray.wasmtime.Module;
import io.github.kawamuray.wasmtime.*;
import io.github.kawamuray.wasmtime.wasi.WasiCtx;
import io.github.kawamuray.wasmtime.wasi.WasiCtxBuilder;
import xyz.wagyourtail.jsmacros.core.event.BaseEvent;
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext;
import xyz.wagyourtail.jsmacros.wasm.client.WasmHelper;

import java.io.Closeable;
import java.io.File;
import java.util.*;

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

        public LinkedList<Integer> freeObjects = new LinkedList<>();


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

        public void runMain(Object... args) {
            linker.module(store, "", module);
            memory = linker.get(store, "", "memory").get().memory();
            synchronized (this) {
                main = linker.get(store, "", "main").get().func();
            }
            Val[] vals = new Val[args.length];
            for (int i = 0; i < args.length; i++) {
                vals[i] = Val.fromI32(WasmHelper.pushObject(this, args[i]));
            }
            try {
                main.call(store, vals);
            } finally {
                synchronized (this) {
                    main.close();
                    main = null;
                }
                for (int i = 0; i < args.length; i++) {
                    WasmHelper.freeObject(this, vals[i].i32());
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
