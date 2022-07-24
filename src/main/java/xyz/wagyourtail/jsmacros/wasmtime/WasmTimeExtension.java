package xyz.wagyourtail.jsmacros.wasmtime;

import com.google.common.collect.Sets;
import io.github.kawamuray.wasmtime.Func;
import io.github.kawamuray.wasmtime.Val;
import org.apache.commons.io.IOUtils;
import org.slf4j.helpers.NOPLogger;
import sun.misc.Unsafe;
import xyz.wagyourtail.jsmacros.core.Core;
import xyz.wagyourtail.jsmacros.core.extensions.Extension;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseWrappedException;
import xyz.wagyourtail.jsmacros.core.library.BaseLibrary;
import xyz.wagyourtail.jsmacros.wasmtime.language.impl.WasmTimeLanguageDefinition;
import xyz.wagyourtail.jsmacros.wasmtime.language.impl.WasmTimeScriptContext;
import xyz.wagyourtail.jsmacros.wasmtime.library.impl.FJava;
import xyz.wagyourtail.jsmacros.wasmtime.library.impl.FWrapper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

public class WasmTimeExtension implements Extension {
    WasmTimeLanguageDefinition languageDefinition;

    @Override
    public void init() {
//        try {
//            Core.getInstance().config.addOptions("wasm", WASMConfig.class);
//        } catch (IllegalAccessException | InstantiationException e) {
//            throw new RuntimeException(e);
//        }

        Thread t = new Thread(WasmTimeExtension::loadWasm);
        t.start();

        try {
            new Func(null, null, null);
        } catch (Throwable tx) {

        }

        try {
            Field f = Func.class.getDeclaredField("log");
            Field f2 = Unsafe.class.getDeclaredField("theUnsafe");
            f2.setAccessible(true);
            Unsafe unsafe = (Unsafe) f2.get(null);
            unsafe.putOrderedObject(unsafe.staticFieldBase(f), unsafe.staticFieldOffset(f), NOPLogger.NOP_LOGGER);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getLanguageImplName() {
        return "wasmtime";
    }

    @Override
    public ExtMatch extensionMatch(File file) {
        if (file.getName().endsWith(".wasm") ||
            file.getName().endsWith(".wasi") ||
            file.getName().endsWith(".wat")
        ) {
            if (file.getName().contains(getLanguageImplName())) {
                return ExtMatch.MATCH_WITH_NAME;
            } else {
                return ExtMatch.MATCH;
            }
        }
        return ExtMatch.NOT_MATCH;
    }

    @Override
    public String defaultFileExtension() {
        return "wasm";
    }

    @Override
    public BaseLanguage<?, ?> getLanguage(Core<?, ?> core) {
        if (languageDefinition == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(WasmTimeExtension.class.getClassLoader());
            languageDefinition = new WasmTimeLanguageDefinition(this, core);
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        return languageDefinition;
    }

    @Override
    public Set<Class<? extends BaseLibrary>> getLibraries() {
        return Sets.newHashSet(FWrapper.class, FJava.class);
    }

    @Override
    public BaseWrappedException<?> wrapException(Throwable throwable) {
        //TODO, figure out what the hell to do here...
        return null;
    }

    @Override
    public boolean isGuestObject(Object o) {
        return o instanceof Val;
    }

    public static void loadWasm() {
        byte[] wasmBytes;
        try {
            wasmBytes = IOUtils.toByteArray(WasmTimeExtension.class.getResourceAsStream("/wasm/wasm_load.wasm"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (WasmTimeScriptContext.WasmInstance i = new WasmTimeScriptContext.WasmInstance(wasmBytes, false)) {
            WasmHelper.registerMethod(i, "env", "print", null, WasmTimeExtension.class.getMethod("print", String.class));
            i.runMain();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void print(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) {
        loadWasm();
    }

}
