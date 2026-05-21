package com.example.special.utils;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;

public class DynamicClassLoader extends ClassLoader {

    // 把字节码 → 定义成 Class
    public Class<?> defineClassFromBytes(String className, byte[] classBytes) {
        return super.defineClass(className, classBytes, 0, classBytes.length);
    }

    // 源码 → 内存编译 → byte[]
    public static byte[] compileToBytes(String fullClassName, String sourceCode) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // 内存源码
        SimpleJavaFileObject source = new SimpleJavaFileObject(
                URI.create("string:///" + fullClassName.replace('.', '/') + ".java"),
                JavaFileObject.Kind.SOURCE
        ) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return sourceCode;
            }
        };

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // 内存class输出
        SimpleJavaFileObject classFile = new SimpleJavaFileObject(
                URI.create("string:///" + fullClassName.replace('.', '/') + ".class"),
                JavaFileObject.Kind.CLASS
        ) {
            @Override
            public OutputStream openOutputStream() {
                return outputStream;
            }
        };

        try (StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(null, null, null)) {
            JavaFileManager fileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(standardFileManager) {
                @Override
                public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
                    return classFile;
                }
            };

            // ==================== 关键：关闭注解处理器，干掉警告 ====================
            JavaCompiler.CompilationTask task = compiler.getTask(
                    null, fileManager, null,
                    Collections.singletonList("-proc:none"),  // 这里！关闭 Lombok 等处理器
                    null,
                    Collections.singletonList(source)
            );

            boolean success = task.call();
            if (!success) {
                throw new RuntimeException("编译失败");
            }

            return outputStream.toByteArray();
        }
    }

    // 测试
    public static void main(String[] args) throws Exception {
        String className = "com.test.DynamicDemo";
        String code = "package com.test;\n" +
                "public class DynamicDemo {\n" +
                "    public String hello() {\n" +
                "        return \"我是运行时编译的类！\";\n" +
                "    }\n" +
                "}";
        try {
            byte[] bytes = compileToBytes(className, code);
            DynamicClassLoader loader = new DynamicClassLoader();
            Class<?> clazz = loader.defineClassFromBytes(className, bytes);

            Object obj = clazz.getDeclaredConstructor().newInstance();
            Method hello = clazz.getMethod("hello");
            System.out.println(hello.invoke(obj));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }


    }
}
