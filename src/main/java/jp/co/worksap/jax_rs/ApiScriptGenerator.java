package jp.co.worksap.jax_rs;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.UriBuilder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

public class ApiScriptGenerator {
    private static final Joiner COMMA_JOINER = Joiner.on(',');
    private final Class<?>[] controllers;

    public ApiScriptGenerator(Class<?>... controllers) {
        this.controllers = controllers;
    }

    public void execute(File outputDir, String metaTagName, String dataNameToGetContextPath, ArgumentInterface argumentInterface) throws IOException {
        Preconditions.checkNotNull(outputDir);
        Preconditions.checkArgument(outputDir.isDirectory());
        for (Class<?> clazz : controllers) {
            File js = new File(outputDir, createModuleNameOf(clazz) + ".js");
            write(clazz, js, metaTagName, dataNameToGetContextPath);
        }
    }

    private void write(Class<?> clazz, File js, String metaTagName, String dataNameToGetContextPath) throws IOException {
        Function<String, String> createJsonPair = new Function<String, String>() {
            @Override
            public String apply(String input) {
                return String.format("'%s':%s", input, input);
            }
        };

        BufferedWriter writer = Files.newWriter(js, Charsets.UTF_8);
        try {
            writer.write("// auto generated by ");
            writer.write(getClass().getName());
            writer.write("\ndefine(");
            writer.write("['jquery', 'exports'], function($, exports) {\n  'use strict';\n");
            String scriptToGetBaseURL = makeScriptToGetBaseURL(metaTagName, dataNameToGetContextPath);
            writer.write(scriptToGetBaseURL);

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.getDeclaringClass().equals(clazz)) {
                    continue;
                }
                writeMethod(clazz, createJsonPair, writer, method);
            }
            writer.write("});");
        } finally {
            writer.close();
        }
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    @VisibleForTesting
    String makeScriptToGetBaseURL(String metaTagName,
            String dataNameToGetContextPath) {
        checkNotNull(metaTagName);
        checkNotNull(dataNameToGetContextPath);

        String scriptToGetBaseURL = String.format("  var baseURL = $('meta[name=\"%s\"]').data('%s') + '/resources';\n",
                escape(metaTagName),
                escape(dataNameToGetContextPath));

        return scriptToGetBaseURL;
    }

    /**
     * We have to escape double-quotation in attribute value.
     * @see https://www.owasp.org/index.php/XSS_%28Cross_Site_Scripting%29_Prevention_Cheat_Sheet
     */
    @Nonnull
    private String escape(@Nonnull String htmlAttributeValue) {
        return htmlAttributeValue
                .replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                .replaceAll("\"", "&#x22;").replaceAll("'", "&#x27;").replaceAll("/", "&#x2F;");
    }

    private List<String> writeMethod(Class<?> clazz,
            Function<String, String> createJsonPair, BufferedWriter writer,
            Method method) throws IOException {
        List<String> argumentName = getArgumentName(method);

        writer.write("  exports.");
        writer.write(method.getName());
        writer.write(" = function (");

        writer.write(COMMA_JOINER.join(argumentName));
        writer.write(") {\n");
        writer.write("    return $.ajax({\n        cache: false,\n        url: baseURL + ");
        writer.write(getPathOf(clazz, method));
        writer.write(",\n        type: '");
        writer.write(getTypeOf(method));
        writer.write("',\n        data: {");
        writer.write(COMMA_JOINER.join(
                Iterators.transform(getFormOrQueryParamName(method).iterator(),
                        createJsonPair)));
        writer.write("}\n    }).promise();\n  };\n");
        return argumentName;
    }

    private String createModuleNameOf(Class<?> clazz) {
        StringBuilder result = new StringBuilder(clazz.getSimpleName());
        result.setCharAt(0, Character.toLowerCase(result.charAt(0)));
        return result.toString();
    }

    private String getPathOf(Class<?> clazz, Method method) {
        UriBuilder builder = UriBuilder.fromResource(clazz).path(method);
        Map<String, String> pathParams = Maps.newHashMap();
        for (String pathParam : getPathParamName(method)) {
            pathParams.put(pathParam, "' + encodeURI(" + pathParam + ") + '");
        }
        String path = "\'" + builder.buildFromMap(pathParams).getPath();
        if (path.endsWith(" + \'")) {
            path = path.substring(0, path.length() - " + \'".length());
        } else {
            path += '\'';
        }
        return path;
    }

    private List<String> getArgumentName(Method method) {
        List<String> params = getPathParamName(method);
        List<String> formParams = getFormOrQueryParamName(method);
        params.addAll(formParams);
        return params;
    }

    private List<String> getFormOrQueryParamName(Method method) {
        List<String> argumentName = Lists.newArrayList();
        for (Annotation[] perMethod : method.getParameterAnnotations()) {
            for (Annotation annotation : perMethod) {
                if (annotation instanceof FormParam) {
                    FormParam param = (FormParam) annotation;
                    argumentName.add(param.value());
                } else if (annotation instanceof QueryParam) {
                    QueryParam param = (QueryParam) annotation;
                    argumentName.add(param.value());
                }
            }
        }
        return argumentName;
    }

    private List<String> getPathParamName(Method method) {
        List<String> argumentName = Lists.newArrayList();
        for (Annotation[] perMethod : method.getParameterAnnotations()) {
            for (Annotation annotation : perMethod) {
                if (annotation instanceof PathParam) {
                    PathParam param = (PathParam) annotation;
                    argumentName.add(param.value());
                }
            }
        }
        return argumentName;
    }

    private String getTypeOf(Method method) {
        if (method.getAnnotation(GET.class) != null) {
            return "get";
        } else if (method.getAnnotation(POST.class) != null) {
            return "post";
        } else if (method.getAnnotation(DELETE.class) != null) {
            return "delete";
        } else if (method.getAnnotation(PUT.class) != null) {
            return "put";
        }
        throw new IllegalArgumentException(method.getName()
                + " has no annotation like @GET");
    }
}
