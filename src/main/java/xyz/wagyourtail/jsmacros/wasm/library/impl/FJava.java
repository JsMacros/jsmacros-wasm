package xyz.wagyourtail.jsmacros.wasm.library.impl;

import io.github.kawamuray.wasmtime.WasmValType;
import xyz.wagyourtail.jsmacros.core.classes.WrappedClassInstance;
import xyz.wagyourtail.jsmacros.core.language.BaseLanguage;
import xyz.wagyourtail.jsmacros.core.language.BaseScriptContext;
import xyz.wagyourtail.jsmacros.core.library.Library;
import xyz.wagyourtail.jsmacros.core.library.PerExecLanguageLibrary;
import xyz.wagyourtail.jsmacros.wasm.client.WasmHelper;
import xyz.wagyourtail.jsmacros.wasm.language.impl.WASMLanguageDefinition;
import xyz.wagyourtail.jsmacros.wasm.language.impl.WASMScriptContext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Library(value = "Java", languages = { WASMLanguageDefinition.class })
public class FJava extends PerExecLanguageLibrary<WASMScriptContext.WasmInstance> {

    public FJava(BaseScriptContext<WASMScriptContext.WasmInstance> context, Class<? extends BaseLanguage<WASMScriptContext.WasmInstance>> language) {
        super(context, language);
    }

    /**
     * writes a c-string to the wasm array ptr provided, <br>
     * make sure maxLength is correct for a null-terminated string
     * @param jPtr
     * @param arr
     * @param maxLength
     */
    public void convertJavaString(int jPtr, int arr, int maxLength) {
        ByteBuffer buff = ctx.getContext().getMemory().buffer(ctx.getContext().store);
        byte[] s = ctx.getContext().javaObjects.get(jPtr).toString().getBytes(StandardCharsets.UTF_8);
        if (s.length >= maxLength) {
            throw new RuntimeException("String is too long");
        }
        int i;
        for (i = 0; i < s.length; i++) {
            buff.put(arr + i, s[i]);
        }
        // write null terminator
        buff.put(arr + i, (byte) 0);
    }

    public int toJString(int ptr) {
        String s = WasmHelper.fromWasmPtr(ctx.getContext(), ptr);
        return WasmHelper.pushObject(ctx.getContext(), s);
    }

    public int convertJavaPrimitiveInt(int jPtr) {
        return (int) ctx.getContext().javaObjects.get(jPtr);
    }

    public long convertJavaPrimitiveLong(int jPtr) {
        return (long) ctx.getContext().javaObjects.get(jPtr);
    }

    public float convertJavaPrimitiveFloat(int jPtr) {
        return (float) ctx.getContext().javaObjects.get(jPtr);
    }

    public double convertJavaPrimitiveDouble(int jPtr) {
        return (double) ctx.getContext().javaObjects.get(jPtr);
    }

    /**
     *
     * @param jPtr
     * @return jPtr of a string
     */
    public int getJavaType(int jPtr) {
        return WasmHelper.pushObject(ctx.getContext(), ctx.getContext().javaObjects.get(jPtr).getClass().getCanonicalName());
    }

    /**
     *
     * @param jPtr
     * @param methodSig
     * @param argPtrs *i32[] pointer to each arg (or jPtr)
     * @param argCount
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    public int invokeJavaMethod(int jPtr, String methodSig, int argPtrs, int argCount) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object methodClass = ctx.getContext().javaObjects.get(jPtr);
        MethodSigParts ms = mapMethodSig(methodSig);
        Method method = methodClass.getClass().getMethod(ms.name, ms.params);

        Class<?>[] params = method.getParameterTypes();
        WasmValType[] paramTypes = new WasmValType[params.length];
        for (int i = 0; i < params.length; i++) {
            Class<?> param = params[i];
            if (WasmHelper.isNativeWasmType(param)) {
                paramTypes[i] = WasmHelper.getNativeType(param);
            } else if (WasmHelper.canConvertToNativeType(param)) {
                paramTypes[i] = WasmValType.I32;
            } else {
                paramTypes[i] = WasmValType.I32;
            }
        }

        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Class<?> param = params[i];
            int ptr = WasmHelper.readIntFromMemory(ctx.getContext(), argPtrs + i * 4);
            if (WasmHelper.isNativeWasmType(param)) {
                if (param == int.class || param == Integer.class) {
                    args[i] = WasmHelper.readIntFromMemory(ctx.getContext(), ptr);
                } else if (param == float.class || param == Float.class) {
                    args[i] = WasmHelper.readFloatFromMemory(ctx.getContext(), ptr);
                } else if (param == double.class || param == Double.class) {
                    args[i] = WasmHelper.readDoubleFromMemory(ctx.getContext(), ptr);
                } else if (param == long.class || param == Long.class) {
                    args[i] = WasmHelper.readLongFromMemory(ctx.getContext(), ptr);
                }
            } else if (WasmHelper.canConvertToNativeType(param)) {
                args[i] = WasmHelper.fromInt(WasmHelper.readIntFromMemory(ctx.getContext(), ptr), param);
            } else {
                if (params[i] == String.class) {
                    args[i] = WasmHelper.fromWasmPtr(ctx.getContext(), ptr);
                } else {
                    args[i] = ctx.getContext().javaObjects.get(ptr);
                }
            }
        }
        Object result = method.invoke(methodClass, args);
        if (method.getReturnType() != void.class) {
            return WasmHelper.pushObject(ctx.getContext(), result);
        }
        return -1;
    }

    /**
     *
     * @param jPtr
     */
    public void freeJavaObject(int jPtr) {
        WasmHelper.freeObject(ctx.getContext(), jPtr);
    }


    private final Pattern sigPart = Pattern.compile("[ZBCSIJFDV]|L(.+?);");

    private Class<?> getPrimitive(char c) {
        switch (c) {
            case 'Z':
                return boolean.class;
            case 'B':
                return byte.class;
            case 'C':
                return char.class;
            case 'S':
                return short.class;
            case 'I':
                return int.class;
            case 'J':
                return long.class;
            case 'F':
                return float.class;
            case 'D':
                return double.class;
            case 'V':
                return void.class;
            default:
                throw new NullPointerException("Unknown Primitive: " + c);
        }
    }

    protected Class<?> getClass(String className) throws ClassNotFoundException, IOException {
        return Class.forName(className.replace("/", "."));
    }

    private MethodSigParts mapMethodSig(String methodSig) throws ClassNotFoundException, IOException {
        String[] parts = methodSig.split("[()]", 3);
        List<Class<?>> params = new ArrayList<>();
        Matcher m = sigPart.matcher(parts[1]);
        while (m.find()) {
            String clazz = m.group(1);
            if (clazz == null) {
                params.add(getPrimitive(m.group().charAt(0)));
            } else {
                params.add(getClass(clazz));
            }
        }
        Class<?> retval;
        Matcher r = sigPart.matcher(parts[2]);
        if (r.find()) {
            String clazz = r.group(1);
            if (clazz == null) {
                retval = getPrimitive(r.group().charAt(0));
            } else {
                retval = getClass(clazz);
            }
        } else {
            throw new IllegalArgumentException("Signature return value invalid.");
        }
        return new MethodSigParts(parts[0], params.toArray(new Class[0]), retval);
    }

    public static class MethodSigParts {
        public final String name;
        public final Class<?>[] params;
        public final Class<?> returnType;

        MethodSigParts(String name, Class<?>[] params, Class<?> returnType) {
            this.name = name;
            this.params = params;
            this.returnType = returnType;
        }
    }
}
