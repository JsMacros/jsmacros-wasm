package xyz.wagyourtail.jsmacros.wasm.client;

import net.fabricmc.api.ModInitializer;
import org.apache.commons.io.IOUtils;
import org.wasmer.Instance;

import java.io.IOException;
import java.util.function.Consumer;

public class JsMacrosWASM implements ModInitializer {
    @Override
    public void onInitialize() {
//        try {
//            JsMacros.core.config.addOptions("jep", JEPConfig.class);
//        } catch (IllegalAccessException | InstantiationException e) {
//            throw new RuntimeException(e);
//        }
        Thread t = new Thread(JsMacrosWASM::loadWasm);
        t.start();
    }

    public static void loadWasm() {
        byte[] wasmBytes = new byte[0];
        try {
            wasmBytes = IOUtils.toByteArray(JsMacrosWASM.class.getResourceAsStream("/wasm/wasm_load.wasm"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Instance instance = new Instance(wasmBytes);
        instance.exports.getFunction("main").apply((Consumer<String>)JsMacrosWASM::print);
        instance.close();
    }

    public static void print(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) {
        Object[] test = new Object[][] {};
        Object[][] test2 = (Object[][]) test;
        System.out.println(test2.length);
        loadWasm();
    }

}
