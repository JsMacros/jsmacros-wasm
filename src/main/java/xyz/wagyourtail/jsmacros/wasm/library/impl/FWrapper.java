package xyz.wagyourtail.jsmacros.wasm.library.impl;

import io.github.kawamuray.wasmtime.Func;
import io.github.kawamuray.wasmtime.Val;
import io.github.kawamuray.wasmtime.WasmValType;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.MethodWrapper;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext;
import xyz.wagyourtail.jsmacros.core.library.IFWrapper;
import xyz.wagyourtail.jsmacros.core.library.Library;
import xyz.wagyourtail.jsmacros.core.library.PerExecLanguageLibrary;
import xyz.wagyourtail.jsmacros.wasm.client.WasmHelper;
import xyz.wagyourtail.jsmacros.wasm.language.impl.WASMLanguageDefinition;
import xyz.wagyourtail.jsmacros.wasm.language.impl.WASMScriptContext;

import java.util.HashSet;
import java.util.Set;

@Library(value = "JavaWrapper", languages = WASMLanguageDefinition.class)
public class FWrapper  extends PerExecLanguageLibrary<WASMScriptContext.WasmInstance> implements IFWrapper<String> {
    public FWrapper(BaseScriptContext<WASMScriptContext.WasmInstance> context, Class<? extends BaseLanguage<WASMScriptContext.WasmInstance>> language) {
        super(context, language);
    }

    @Override
    public <A, B, R> MethodWrapper<A, B, R, ?> methodToJava(String c) {
        return new WasmMethodWrapper<>(c, false, ctx);
    }

    @Override
    public <A, B, R> MethodWrapper<A, B, R, ?> methodToJavaAsync(String c) {
        return new WasmMethodWrapper<>(c, true, ctx);
    }

    @Override
    public void stop() {
        ctx.getContext().close();
    }

    public class WasmMethodWrapper<T, U, R> extends MethodWrapper<T, U, R, BaseScriptContext<WASMScriptContext.WasmInstance>> {
        private final String fn;
        private final boolean await;

        private WasmMethodWrapper(String fn, boolean await, BaseScriptContext<WASMScriptContext.WasmInstance> ctx) {
            super(ctx);
            this.fn = fn;
            this.await = await;
            try (Func fn2 = ctx.getContext().linker.get(ctx.getContext().store, "", fn).get().func()) {
                //pass
            }
        }

        private void innerAccept(Object... args) {

            if (await) {
                internalApply(args);
                return;
            }

            Thread th = new Thread(() -> {
                ctx.bindThread(Thread.currentThread());
                try {
                    try (Func fn2 = ctx.getContext().linker.get(ctx.getContext().store, "", fn).get().func()) {
                        Set<Integer> toFree = new HashSet<>();
                        Val[] params = new Val[args.length];
                        try {
                            for (int i = 0; i < args.length; i++) {
                                if (WasmHelper.isNativeWasmType(args[i].getClass())) {
                                    params[i] = WasmHelper.getNativeType(args[i].getClass()).toWasmVal(args[i]);
                                } else if (WasmHelper.canConvertToNativeType(args[i].getClass())) {
                                    params[i] = WasmValType.I32.toWasmVal(WasmHelper.convertToNativeType(args[i]));
                                } else {
                                    params[i] = WasmValType.I32.toWasmVal(WasmHelper.pushObject(ctx.getContext(), args[i]));
                                    toFree.add(i);
                                }
                            }
                            fn2.call(ctx.getContext().store, params);
                        } finally {
                            for (int i : toFree) {
                                WasmHelper.freeObject(ctx.getContext(), params[i].i32());
                            }
                        }
                    }
                } catch (Throwable ex) {
                    Core.getInstance().profile.logError(ex);
                } finally {
                    ctx.releaseBoundEventIfPresent(Thread.currentThread());
                    ctx.unbindThread(Thread.currentThread());
                    Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread());
                }

            });
            th.start();
        }

        private Object internalApply(Object... args) {
            try {
                ctx.bindThread(Thread.currentThread());
                try (Func fn2 = ctx.getContext().linker.get(ctx.getContext().store, "", fn).get().func()) {
                    Set<Integer> toFree = new HashSet<>();
                    Val[] params = new Val[args.length];
                    try {
                        for (int i = 0; i < args.length; i++) {
                            if (WasmHelper.isNativeWasmType(args[i].getClass())) {
                                params[i] = WasmHelper.getNativeType(args[i].getClass()).toWasmVal(args[i]);
                            } else if (WasmHelper.canConvertToNativeType(args[i].getClass())) {
                                params[i] = WasmValType.I32.toWasmVal(WasmHelper.convertToNativeType(args[i]));
                            } else {
                                params[i] = WasmValType.I32.toWasmVal(WasmHelper.pushObject(ctx.getContext(), args[i]));
                            }
                        }
                        Val[] rv = fn2.call(ctx.getContext().store, params);
                        if (rv.length == 0) {
                            return null;
                        }
                        return ctx.getContext().javaObjects.get(rv[0].i32());
                    } finally {
                        for (int i : toFree) {
                            WasmHelper.freeObject(ctx.getContext(), params[i].i32());
                        }
                    }
                }
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            } finally {
                ctx.releaseBoundEventIfPresent(Thread.currentThread());
                ctx.unbindThread(Thread.currentThread());
                Core.getInstance().profile.joinedThreadStack.remove(Thread.currentThread());
            }
        }

        @Override
        public void accept(T t) {
            innerAccept(t);
        }

        @Override
        public void accept(T t, U u) {
            innerAccept(t, u);
        }

        @Override
        public R apply(T t) {
            return (R) internalApply(t);
        }

        @Override
        public R apply(T t, U u) {
            return (R) internalApply(t, u);
        }

        @Override
        public boolean test(T t) {
            return (boolean) internalApply(t);
        }

        @Override
        public boolean test(T t, U u) {
            return (boolean) internalApply(t, u);
        }

        @Override
        public void run() {
            innerAccept();
        }

        @Override
        public int compare(T t, T t1) {
            return (int) internalApply(t, t1);
        }

        @Override
        public R get() {
            return (R) internalApply();
        }

    }

}
