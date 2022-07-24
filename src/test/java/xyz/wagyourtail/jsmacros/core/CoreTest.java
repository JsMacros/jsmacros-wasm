package xyz.wagyourtail.jsmacros.core;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import xyz.wagyourtail.jsmacros.core.event.impl.EventCustom;
import xyz.wagyourtail.jsmacros.core.language.EventContainer;
import xyz.wagyourtail.jsmacros.stubs.CoreInstanceCreator;
import xyz.wagyourtail.jsmacros.stubs.EventRegistryStub;
import xyz.wagyourtail.jsmacros.stubs.ProfileStub;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoreTest {
    @Language("wat")
    private final String TEST_SCRIPT = """
        (module
        (type $t0 (func (param i32 i32 i32)))
        (type $t1 (func (param i32 i32 i32 i32) (result i32)))
        (type $t2 (func (param i32) (result i32)))
        (type $t3 (func (param i32)))
        (type $t4 (func (param i32 i32)))
        (type $t5 (func (param i32 i32 i32) (result i32)))
        (import "Java" "jStringToC" (func $_ZN9untitled24Java10jStringToC17hf0f2fcaaa9064451E (type $t0)))
        (import "Java" "invokeMethod" (func $_ZN9untitled24Java12invokeMethod17h1f4a215b6eecee0bE (type $t1)))
        (import "JavaWrapper" "methodToJava" (func $_ZN9untitled211JavaWrapper12methodToJava17h2d65dd38f2c880a0E (type $t2)))
        (import "Java" "cStringToJ" (func $_ZN9untitled24Java10cStringToJ17h36a06f455d7a4809E (type $t2)))
        (import "Java" "free" (func $_ZN9untitled24Java4free17h7665edb5f0650874E (type $t3)))
        (import "JavaWrapper" "methodToJavaAsync" (func $_ZN9untitled211JavaWrapper17methodToJavaAsync17h7b6cd16589a8bd1bE (type $t2)))
        (func $exec (export "exec") (type $t4) (param $p0 i32) (param $p1 i32)
        (local $l2 i32)
        (global.set $__stack_pointer
          (local.tee $l2
            (i32.sub
              (global.get $__stack_pointer)
              (i32.const 272))))
        (drop
          (call $memset
            (i32.add
              (local.get $l2)
              (i32.const 8))
            (i32.const 0)
            (i32.const 256)))
        (call $_ZN9untitled24Java10jStringToC17hf0f2fcaaa9064451E
          (local.get $p1)
          (i32.add
            (local.get $l2)
            (i32.const 8))
          (i32.const 64))
        (i32.store offset=268
          (local.get $l2)
          (i32.const 1048576))
        (i32.store offset=264
          (local.get $l2)
          (i32.add
            (local.get $l2)
            (i32.const 8)))
        (drop
          (call $_ZN9untitled24Java12invokeMethod17h1f4a215b6eecee0bE
            (local.get $p0)
            (i32.const 1048589)
            (i32.add
              (local.get $l2)
              (i32.const 264))
            (i32.const 2)))
        (global.set $__stack_pointer
          (i32.add
            (local.get $l2)
            (i32.const 272))))
        (func $main (export "main") (type $t0) (param $p0 i32) (param $p1 i32) (param $p2 i32)
        (local $l3 i32) (local $l4 i32) (local $l5 i32)
        (global.set $__stack_pointer
          (local.tee $l3
            (i32.sub
              (global.get $__stack_pointer)
              (i32.const 16))))
        (i32.store offset=12
          (local.get $l3)
          (i32.const 1048576))
        (i32.store offset=8
          (local.get $l3)
          (i32.const 1048637))
        (drop
          (call $_ZN9untitled24Java12invokeMethod17h1f4a215b6eecee0bE
            (local.get $p0)
            (i32.const 1048589)
            (i32.add
              (local.get $l3)
              (i32.const 8))
            (i32.const 2)))
        (local.set $l4
          (call $_ZN9untitled211JavaWrapper12methodToJava17h2d65dd38f2c880a0E
            (i32.const 1048641)))
        (i32.store offset=12
          (local.get $l3)
          (local.tee $l5
            (call $_ZN9untitled24Java10cStringToJ17h36a06f455d7a4809E
              (i32.const 1048646))))
        (i32.store offset=8
          (local.get $l3)
          (local.get $p0))
        (drop
          (call $_ZN9untitled24Java12invokeMethod17h1f4a215b6eecee0bE
            (local.get $l4)
            (i32.const 1048650)
            (i32.add
              (local.get $l3)
              (i32.const 8))
            (i32.const 2)))
        (call $_ZN9untitled24Java4free17h7665edb5f0650874E
          (local.get $l5))
        (call $_ZN9untitled24Java4free17h7665edb5f0650874E
          (local.get $l4))
        (local.set $l4
          (call $_ZN9untitled211JavaWrapper17methodToJavaAsync17h7b6cd16589a8bd1bE
            (i32.const 1048641)))
        (i32.store offset=12
          (local.get $l3)
          (local.tee $l5
            (call $_ZN9untitled24Java10cStringToJ17h36a06f455d7a4809E
              (i32.const 1048695))))
        (i32.store offset=8
          (local.get $l3)
          (local.get $p0))
        (drop
          (call $_ZN9untitled24Java12invokeMethod17h1f4a215b6eecee0bE
            (local.get $l4)
            (i32.const 1048650)
            (i32.add
              (local.get $l3)
              (i32.const 8))
            (i32.const 2)))
        (call $_ZN9untitled24Java4free17h7665edb5f0650874E
          (local.get $l5))
        (call $_ZN9untitled24Java4free17h7665edb5f0650874E
          (local.get $l4))
        (global.set $__stack_pointer
          (i32.add
            (local.get $l3)
            (i32.const 16))))
        (func $memset (type $t5) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
        (call $_ZN17compiler_builtins3mem6memset17he8631275836e12a1E
          (local.get $p0)
          (local.get $p1)
          (local.get $p2)))
        (func $_ZN17compiler_builtins3mem6memset17he8631275836e12a1E (type $t5) (param $p0 i32) (param $p1 i32) (param $p2 i32) (result i32)
        (local $l3 i32) (local $l4 i32) (local $l5 i32)
        (block $B0
          (block $B1
            (br_if $B1
              (i32.gt_u
                (local.get $p2)
                (i32.const 15)))
            (local.set $l3
              (local.get $p0))
            (br $B0))
          (local.set $l5
            (i32.add
              (local.get $p0)
              (local.tee $l4
                (i32.and
                  (i32.sub
                    (i32.const 0)
                    (local.get $p0))
                  (i32.const 3)))))
          (block $B2
            (br_if $B2
              (i32.eqz
                (local.get $l4)))
            (local.set $l3
              (local.get $p0))
            (loop $L3
              (i32.store8
                (local.get $l3)
                (local.get $p1))
              (br_if $L3
                (i32.lt_u
                  (local.tee $l3
                    (i32.add
                      (local.get $l3)
                      (i32.const 1)))
                  (local.get $l5)))))
          (local.set $l3
            (i32.add
              (local.get $l5)
              (local.tee $p2
                (i32.and
                  (local.tee $l4
                    (i32.sub
                      (local.get $p2)
                      (local.get $l4)))
                  (i32.const -4)))))
          (block $B4
            (br_if $B4
              (i32.lt_s
                (local.get $p2)
                (i32.const 1)))
            (local.set $p2
              (i32.mul
                (i32.and
                  (local.get $p1)
                  (i32.const 255))
                (i32.const 16843009)))
            (loop $L5
              (i32.store
                (local.get $l5)
                (local.get $p2))
              (br_if $L5
                (i32.lt_u
                  (local.tee $l5
                    (i32.add
                      (local.get $l5)
                      (i32.const 4)))
                  (local.get $l3)))))
          (local.set $p2
            (i32.and
              (local.get $l4)
              (i32.const 3))))
        (block $B6
          (br_if $B6
            (i32.eqz
              (local.get $p2)))
          (local.set $l5
            (i32.add
              (local.get $l3)
              (local.get $p2)))
          (loop $L7
            (i32.store8
              (local.get $l3)
              (local.get $p1))
            (br_if $L7
              (i32.lt_u
                (local.tee $l3
                  (i32.add
                    (local.get $l3)
                    (i32.const 1)))
                (local.get $l5)))))
        (local.get $p0))
        (table $T0 1 1 funcref)
        (memory $memory (export "memory") 17)
        (global $__stack_pointer (mut i32) (i32.const 1048576))
        (global $__data_end (export "__data_end") i32 (i32.const 1048699))
        (global $__heap_base (export "__heap_base") i32 (i32.const 1048704))
        (data $.rodata (i32.const 1048576) "Hello World!\\00putString(Ljava/lang/String;Ljava/lang/String;)Zrp1\\00exec\\00rp2\\00accept(Ljava/lang/Object;Ljava/lang/Object;)Vrp3\\00"))
        """;
    
    @Test
    public void test() throws InterruptedException {
        Core<ProfileStub, EventRegistryStub> core = CoreInstanceCreator.createCore();
        EventCustom event = new EventCustom("test");
        EventContainer<?> ev = core.exec("wat", TEST_SCRIPT, null, event, null, null);
        ev.awaitLock(() -> {});
        Thread.sleep(100);
        assertEquals("{rp1=Hello World!, rp3=Hello World!, rp2=Hello World!}", event.getUnderlyingMap().toString());
    }

}
