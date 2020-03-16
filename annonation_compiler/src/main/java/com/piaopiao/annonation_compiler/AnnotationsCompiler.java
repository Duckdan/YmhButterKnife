package com.piaopiao.annonation_compiler;

import com.google.auto.service.AutoService;
import com.piaopiao.annotations.BindView;
import com.piaopiao.annotations.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * 用来生成衍生代码
 */
@AutoService(Processor.class)
//允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({Constants.BIND_VIEW, Constants.ON_CLICK})
//指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AnnotationsCompiler extends AbstractProcessor {

    //1.日志打印工具类
    private Messager messager;

    //3.需要一个用来生成文件的对象
    Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        javaPoet(roundEnvironment);
//        writerJava(roundEnvironment);
        return true;

    }

    /**
     * 手写java类
     * @param roundEnv
     */
    private void writerJava(RoundEnvironment roundEnv) {
        //类    TypeElement
        //方法  ExecutableElement
        //属性  VariableElement
        //需要开始进行分类
        Map<String, List<Element>> mapElement = new HashMap<>();

        //字段
        Set<? extends Element> elementsAnnotatedWithField = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element element : elementsAnnotatedWithField) {
            VariableElement variableElement = (VariableElement) element;
            //得到activity名字(全类名)
            String activityName = variableElement.getEnclosingElement().getSimpleName().toString();
            List<Element> variableElements = mapElement.get(activityName);
            if (variableElements == null) {
                variableElements = new ArrayList();
                mapElement.put(activityName, variableElements);
            }
            variableElements.add(variableElement);
        }

        //方法
        Set<? extends Element> elementsAnnotatedWithMethod = roundEnv.getElementsAnnotatedWith(OnClick.class);
        for (Element element : elementsAnnotatedWithMethod) {
            ExecutableElement executableElement = (ExecutableElement) element;
            //得到activity名字(全类名)
            String activityName = executableElement.getEnclosingElement().getSimpleName().toString();
            //找到方法元素
            List<Element> executableElements = mapElement.get(activityName);
            if (executableElements == null) {
                executableElements = new ArrayList();
                mapElement.put(activityName, executableElements);
            }
            executableElements.add(executableElement);
        }

        if (mapElement.size() > 0) {
            Writer writer = null;
            Iterator<String> iterator = mapElement.keySet().iterator();
            while (iterator.hasNext()) {
                //开始生成对应的文件
                String activityName = iterator.next();
                List<Element> elements = mapElement.get(activityName);
                //得到包名
                TypeElement enclosingElment = (TypeElement) elements.get(0).getEnclosingElement();
                String packageName = processingEnv.getElementUtils().getPackageOf(enclosingElment).toString();
                //写入文件
                try {
                    JavaFileObject sourceFile = filer.createSourceFile(packageName + "." + activityName + "_ViewBinding");
                    writer = sourceFile.openWriter();
                    //package com.example.butterknife_framework_demo;
                    writer.write("package " + packageName + ";\n");
                    //import com.example.butterknife_framework_demo.IBinder;
                    writer.write("import " + packageName + ".IBinder;\n");
                    //public class MainActivity_ViewBinding implements IBinder<com.example.butterknife_framework_demo.MainActivity> {
                    writer.write("public class " + activityName + "_ViewBinding implements IBinder<" + packageName + "." + activityName + ">{\n");
                    //@Override
                    writer.write("@Override\n");
                    //public void bind(com.example.butterknife_framework_demo.MainActivity target) {
                    writer.write("public void bind(final " + packageName + "." + activityName + " target){\n");
                    //target.textView = (android.widget.TextView) target.findViewById(2131165359);
                    for (Element element : elements) {
                        if (element instanceof VariableElement) {
                            //得到名字
                            String variableName = element.getSimpleName().toString();
                            //得到ID
                            int id = element.getAnnotation(BindView.class).value();
                            //得到类型
                            TypeMirror typeMirror = element.asType();
                            //target.textView = (android.widget.TextView) target.findViewById(2131165359);
                            writer.write("target." + variableName + "=(" + typeMirror + ")target.findViewById(" + id + ");\n");
                        } else if (element instanceof ExecutableElement) {//方法
                            //方法名
                            String executableName = element.getSimpleName().toString();
                            int[] viewIds = element.getAnnotation(OnClick.class).value();
                            for (int id : viewIds) {
                                writer.write("target.findViewById(" + id + ").setOnClickListener( new android.view.View.OnClickListener(){\n");
                                //        void onClick(View v);
                                writer.write("public void onClick(android.view.View view){\n");
                                writer.write("target." + executableName + "(view);");
                                writer.write("\n}\n});");
                            }
                        }

                    }
                    writer.write("\n}\n}");

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }

    /**
     * 利用javaPoet生成java类
     * @param roundEnv
     */
    private void javaPoet(RoundEnvironment roundEnv) {
        //类    TypeElement
        //方法  ExecutableElement
        //属性  VariableElement
        //需要开始进行分类
        Map<String, List<Element>> mapElement = new HashMap<>();

        //字段
        Set<? extends Element> elementsAnnotatedWithField = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element element : elementsAnnotatedWithField) {
            VariableElement variableElement = (VariableElement) element;
            //得到activity名字(全类名)
            String activityName = variableElement.getEnclosingElement().getSimpleName().toString();
            List<Element> variableElements = mapElement.get(activityName);
            if (variableElements == null) {
                variableElements = new ArrayList();
                mapElement.put(activityName, variableElements);
            }
            variableElements.add(variableElement);
        }

        //方法
        Set<? extends Element> elementsAnnotatedWithMethod = roundEnv.getElementsAnnotatedWith(OnClick.class);
        for (Element element : elementsAnnotatedWithMethod) {
            ExecutableElement executableElement = (ExecutableElement) element;
            //得到activity名字(类名)
            String activityName = executableElement.getEnclosingElement().getSimpleName().toString();
            //找到方法元素
            List<Element> executableElements = mapElement.get(activityName);
            if (executableElements == null) {
                executableElements = new ArrayList();
                mapElement.put(activityName, executableElements);
            }
            executableElements.add(executableElement);
        }

        //writerClass(mapElement);
        if (mapElement.size() > 0) {
            Iterator<String> iterator = mapElement.keySet().iterator();
            while (iterator.hasNext()) {
                //开始生成对应的文件
                String activityName = iterator.next();
                List<Element> elements = mapElement.get(activityName);
                //得到包名
                TypeElement enclosingElment = (TypeElement) elements.get(0).getEnclosingElement();
                String packageName = processingEnv.getElementUtils().getPackageOf(enclosingElment).toString();


                MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("bind")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ClassName.get(enclosingElment), "target", Modifier.FINAL)
                        .addAnnotation(Override.class)
                        .returns(void.class);
                for (Element element : elements) {
                    if (element instanceof VariableElement) {
                        //得到名字
                        String variableName = element.getSimpleName().toString();
                        //得到ID
                        int id = element.getAnnotation(BindView.class).value();
                        //得到类型
                        TypeMirror typeMirror = element.asType();

                        methodSpecBuilder.addStatement("target.$N=($T)target.findViewById($L)", variableName, typeMirror, id);
                    } else if (element instanceof ExecutableElement) {//方法
                        //方法名
                        String executableName = element.getSimpleName().toString();
                        int[] viewIds = element.getAnnotation(OnClick.class).value();
                        for (int id : viewIds) {
                            // 匿名内部类实现的接口
                            ClassName viewClick = ClassName.get("android.view.View", "OnClickListener");

                            // 重写的方法
                            MethodSpec clickMethod = MethodSpec.methodBuilder("onClick")
                                    .addModifiers(Modifier.PUBLIC)
                                    .addAnnotation(Override.class)
                                    .addParameter(ClassName.get("android.view", "View"), "view")
                                    .addStatement("target.$N(view)", executableName)
                                    .build();
                            // 创建匿名内部类
                            TypeSpec typeClick = TypeSpec.anonymousClassBuilder("")
                                    .addSuperinterface(viewClick)
                                    .addMethod(clickMethod)
                                    .build();

                            methodSpecBuilder.addStatement("target.findViewById($L).setOnClickListener($L)", id, typeClick);

                        }

                    }
                }
                MethodSpec methodSpec = methodSpecBuilder.build();

                ClassName iBinder = ClassName.get(packageName, "IBinder");

                ClassName activityClassName = ClassName.get(packageName, activityName);

                TypeSpec typeSpec = TypeSpec
                        .classBuilder(activityName + "_ViewBinding")
                        .addSuperinterface(ParameterizedTypeName.get(iBinder, activityClassName))
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(methodSpec).build();

                JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

                try {
                    javaFile.writeTo(filer);
                } catch (Exception e) {
                    e.printStackTrace();
                    messager.printMessage(Diagnostic.Kind.NOTE, "生成文件出现异常：：" + e.getMessage());
                }
            }
        }
    }
}

