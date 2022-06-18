package xyz.wagyourtail.jsmacros.wasm.client;

import net.fabricmc.api.ModInitializer;
import org.apache.commons.io.IOUtils;
import xyz.wagyourtail.jsmacros.client.JsMacros;
import xyz.wagyourtail.jsmacros.wasm.language.impl.WASMLanguageDefinition;
import xyz.wagyourtail.jsmacros.wasm.language.impl.WASMScriptContext;
import xyz.wagyourtail.jsmacros.wasm.library.impl.FJava;

import java.io.IOException;

public class JsMacrosWASM implements ModInitializer {
    @Override
    public void onInitialize() {
//        try {
//            JsMacros.core.config.addOptions("jep", JEPConfig.class);
//        } catch (IllegalAccessException | InstantiationException e) {
//            throw new RuntimeException(e);
//        }

        JsMacros.core.libraryRegistry.addLibrary(FJava.class);
        JsMacros.core.addLanguage(WASMLanguageDefinition.WASM);
        JsMacros.core.addLanguage(WASMLanguageDefinition.WAT);

        Thread t = new Thread(JsMacrosWASM::loadWasm);
        t.start();
    }

    public static void loadWasm() {
        byte[] wasmBytes;
        try {
            wasmBytes = IOUtils.toByteArray(JsMacrosWASM.class.getResourceAsStream("/wasm/wasm_load.wasm"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (WASMScriptContext.WasmInstance i = new WASMScriptContext.WasmInstance(wasmBytes, false)) {
            WasmHelper.registerMethod(i, "env", "print", null, JsMacrosWASM.class.getMethod("print", String.class));
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
