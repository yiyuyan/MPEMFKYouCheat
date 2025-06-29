package net.shuyanmc.mpem.mpemfkyoucheat;

import it.unimi.dsi.fastutil.Pair;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {
    /***
     * @param jar jar文件
     * @return jar内的类|类中的字符串
     */
    public static Pair<List<String>, List<Pair<String, List<String>>>> getClassInfo(JarFile jar) throws IOException {
        List<String> classes = new ArrayList<>();
        List<Pair<String, List<String>>> stringPairs = new ArrayList<>();
        Enumeration<JarEntry> entryEnumeration = jar.entries();
        while (entryEnumeration.hasMoreElements()) {
            JarEntry entry = entryEnumeration.nextElement();
            if (entry.getName().endsWith(".class")) {
                String clazzS = entry.getName().replace("/", ".").replace(".class", "");
                classes.add(clazzS);
                byte[] bytes = IOUtils.toByteArray(jar.getInputStream(entry));
                ClassReader reader = new ClassReader(bytes);
                List<String> strings = new ArrayList<>();
                reader.accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                     String signature, String[] exceptions) {
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override
                            public void visitLdcInsn(Object value) {
                                if (value instanceof String) {
                                    strings.add((String) value);
                                }
                            }

                            @Override
                            public void visitParameter(String name, int access) {
                                if (name != null && !name.isEmpty()) strings.add(name);
                                super.visitParameter(name, access);
                            }
                        };
                    }

                    @Override
                    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                        strings.add(name);
                        return super.visitField(access, name, descriptor, signature, value);
                    }
                }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                if (!strings.isEmpty()) {
                    stringPairs.add(
                            Pair.of(
                                    clazzS,
                                    strings
                            )
                    );
                }
            }
        }
        return Pair.of(classes, stringPairs);
    }
}
