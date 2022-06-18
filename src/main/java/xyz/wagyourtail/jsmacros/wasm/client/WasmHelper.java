package xyz.wagyourtail.jsmacros.wasm.client;

import com.google.common.collect.ImmutableSet;
import io.github.kawamuray.wasmtime.*;
import xyz.wagyourtail.jsmacros.wasm.language.impl.WASMScriptContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.*;

public class WasmHelper {

    public static synchronized int pushObject(WASMScriptContext.WasmInstance in, Object o) {
        for (int i = 0; i < in.javaObjects.size(); i++) {
            if (in.javaObjects.get(i) == null) {
                in.javaObjects.set(i, o);
                return i;
            }
        }
        // else
        in.javaObjects.add(o);
        return in.javaObjects.size() - 1;
    }

    public static synchronized int readIntFromMemory(WASMScriptContext.WasmInstance in, int ptr) {
        return Integer.reverseBytes(in.getMemory().buffer(in.store).getInt(ptr));
    }

    public static synchronized long readLongFromMemory(WASMScriptContext.WasmInstance in, int ptr) {
        return Long.reverseBytes(in.getMemory().buffer(in.store).getLong(ptr));
    }

    public static synchronized float readFloatFromMemory(WASMScriptContext.WasmInstance in, int ptr) {
        return Float.intBitsToFloat(Integer.reverseBytes(in.getMemory().buffer(in.store).getInt(ptr)));
    }

    public static synchronized double readDoubleFromMemory(WASMScriptContext.WasmInstance in, int ptr) {
        return Double.longBitsToDouble(Long.reverseBytes(in.getMemory().buffer(in.store).getLong(ptr)));
    }

    public static synchronized void freeObject(WASMScriptContext.WasmInstance in, int jPtr) {
        in.javaObjects.set(jPtr, null);
    }

    public static boolean isNativeWasmType(Class<?> clz) {
        return clz == void.class || clz == int.class || clz == Integer.class || clz == long.class || clz == Long.class || clz == float.class || clz == Float.class || clz == double.class || clz == Double.class;
    }

    public static WasmValType<?> getNativeType(Class<?> clz) {
        if (clz == int.class || clz == Integer.class) {
            return WasmValType.I32;
        } else if (clz == long.class || clz == Long.class) {
            return WasmValType.I64;
        } else if (clz == float.class || clz == Float.class) {
            return WasmValType.F32;
        } else if (clz == double.class || clz == Double.class) {
            return WasmValType.F64;
        } else {
            throw new RuntimeException("Unsupported type: " + clz.getName());
        }
    }

    public static String fromWasmPtr(WASMScriptContext.WasmInstance in, int ptr) {
        StringBuilder sb = new StringBuilder();
        boolean not_null = true;
        ByteBuffer buff = in.getMemory().buffer(in.store);
        while (not_null) {
            not_null = buff.get(ptr) != 0;
            if (not_null) {
                sb.append((char) buff.get(ptr));
            }
            ptr+=1;
        }
        return sb.toString();
    }
    public static boolean canConvertToNativeType(Class<?> clz) {
        return clz == boolean.class || clz == Boolean.class || clz == byte.class || clz == Byte.class || clz == char.class || clz == Character.class || clz == short.class || clz == Short.class;
    }

    public static Number convertToNativeType(Object n) {
        if (isNativeWasmType(n.getClass())) {
            return (Number) n;
        }
        if (n instanceof Boolean) {
            return (Boolean) n ? 1 : 0;
        }
        if (n instanceof Character) {
            return (int) (Character) n;
        }
        if (n instanceof Byte) {
            return (int) (Byte) n;
        }
        if (n instanceof Short) {
            return (int) (Short) n;
        }
        throw new RuntimeException("Cannot convert " + n.getClass().getCanonicalName() + " to native type");
    }

    public static Object fromInt(Integer i, Class<?> c) {
        if (c == boolean.class || c == Boolean.class) {
            return i != 0;
        } else if (c == char.class || c == Character.class) {
            return (char) (int) i;
        } else if (c == byte.class || c == Byte.class) {
            return (byte) (int) i;
        } else if (c == short.class || c == Short.class) {
            return (short) (int) i;
        }
        throw new RuntimeException("Cannot convert " + c.getCanonicalName() + " to native type");
    }

    public static void registerLibrary(WASMScriptContext.WasmInstance in, String libraryName, Object libraryClass) {
        Map<String, List<Method>> methodsByName = new HashMap<>();
        for (Method method : libraryClass.getClass().getDeclaredMethods()) {
            if ((method.getModifiers() & Modifier.PUBLIC) != 0) {
                methodsByName.computeIfAbsent(method.getName(), k -> new ArrayList<>()).add(method);
            }
        }
        // wasmtime doesn't like duplicate function names, even if they are different signatures
        for (String key : ImmutableSet.copyOf(methodsByName.keySet())) {
            List<Method> methods = methodsByName.get(key);
            if (methods.size() > 1) {
                Map<String, List<Method>> methodsByArgs = new HashMap<>();
                for (Method method : methods) {
                    methodsByArgs.computeIfAbsent(key + method.getParameterCount(), k -> new ArrayList<>()).add(method);
                }
                // check if there are multiple methods with the same name and same number of arguments
                for (String key2 : ImmutableSet.copyOf(methodsByArgs.keySet())) {
                    List<Method> methods2 = methodsByArgs.get(key2);
                    if (methods2.size() > 1) {
                        Map<String, List<Method>> methodsNumbered = new HashMap<>();
                        int i = 0;
                        for (Method method : methods2) {
                            methodsNumbered.computeIfAbsent(key2 + "_" + (++i), k -> new ArrayList<>()).add(method);
                        }
                        methodsByArgs.remove(key2);
                        methodsByArgs.putAll(methodsNumbered);
                    }
                }
                methodsByName.remove(key);
                methodsByName.putAll(methodsByArgs);
            }
        }
        for (Map.Entry<String, List<Method>> stringListEntry : methodsByName.entrySet()) {
            registerMethod(in, libraryName, stringListEntry.getKey(), libraryClass, stringListEntry.getValue().get(0));
        }
    }

    public static void registerMethod(WASMScriptContext.WasmInstance in, String libraryName, String methName, Object methodClass, Method method) {
        Class<?> rv = method.getReturnType();
        Class<?>[] params = method.getParameterTypes();
        WasmValType[] paramTypes = new WasmValType[params.length];
        for (int i = 0; i < params.length; i++) {
            Class<?> param = params[i];
            if (isNativeWasmType(param)) {
                paramTypes[i] = getNativeType(param);
            } else if (canConvertToNativeType(param)) {
                paramTypes[i] = WasmValType.I32;
            } else {
                paramTypes[i] = WasmValType.I32;
            }
        }
        WasmValType rt;
        if (isNativeWasmType(rv)) {
            if (rv == void.class) {
                rt = null;
            } else {
                rt = getNativeType(rv);
            }
        } else if (canConvertToNativeType(rv)) {
            rt = WasmValType.I32;
        } else {
            rt = WasmValType.I32;
        }
        Val.Type[] retV;
        if (rt != null) {
            retV = new Val.Type[]{rt.type()};
        } else {
            retV = new Val.Type[]{};
        }
        Func f = new Func(in.store, new FuncType(Arrays.stream(paramTypes).map(WasmValType::type).toArray(Val.Type[]::new), retV), (caller, ps, results) -> {
            Object[] args = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                Class<?> param = params[i];
                if (isNativeWasmType(param)) {
                    args[i] = paramTypes[i].fromWasmVal(ps[i]);
                } else if (canConvertToNativeType(param)) {
                    args[i] = fromInt(WasmValType.I32.fromWasmVal(ps[i]), param);
                } else {
                    if (params[i] == String.class) {
                        args[i] = fromWasmPtr(in, WasmValType.I32.fromWasmVal(ps[i]));
                    } else {
                        args[i] = in.javaObjects.get(WasmValType.I32.fromWasmVal(ps[i]));
                    }
                }
            }
            Object result;
            try {
                result = method.invoke(methodClass, args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            if (isNativeWasmType(rv)) {
                if (rv != void.class) {
                    results[0] = rt.toWasmVal(result);
                }
            } else if (canConvertToNativeType(rv)) {
                results[0] = rt.toWasmVal(convertToNativeType(result));
            } else {
                results[0] = rt.toWasmVal(pushObject(in, result));
            }
            return Optional.empty();
        });
        in.linker.define(libraryName, methName, Extern.fromFunc(f));
    }
}
