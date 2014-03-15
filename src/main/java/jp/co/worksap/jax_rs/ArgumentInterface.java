package jp.co.worksap.jax_rs;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;

enum ArgumentInterface {
    /**
     * <p>Each function get some objects as argument.
     */
    SAME_TO_JAVA {
        @Override
        String generateArgument(List<String> argumentName) {
            return COMMA_JOINER.join(argumentName);
        }

        @Override
        String generatePathParam(String pathParam) {
            return pathParam;
        }

        @Override
        Function<String, String> createJsonPair() {
            return new Function<String, String>() {
                @Override
                public String apply(String input) {
                    return String.format("'%s':%s", input, input);
                }
            };
        }
    },

    /**
     * <p>Each function get only one object as argument.
     * Default.
     * See {@code src/test/expect/one_object_interface/simpleAPI.js} 
     */
    ONE_OBJECT {
        @Override
        String generateArgument(List<String> argumentName) {
            return "data";
        }

        @Override
        String generatePathParam(String pathParam) {
            return "data." + pathParam;
        }

        @Override
        Function<String, String> createJsonPair() {
            return new Function<String, String>() {
                @Override
                public String apply(String input) {
                    return String.format("'%s':data.%s", input, input);
                }
            };
        }
    };

    private static final Joiner COMMA_JOINER = Joiner.on(',');
    /**
     * <p>Generate argument list for JavaScript.
     *
     * @param argumentName list of argument name
     * @return generated list, you can use this String directly
     */
    abstract String generateArgument(List<String> argumentName);
    /**
     * <p>Generate object for &quot;data&quot; parameter of $.ajax method.
     *
     * @param argumentName list of argument name (excluding "path param")
     * @return generated object, you can use this String directly
     */
    String generateData(List<String> argumentName) {
        return "{" + COMMA_JOINER.join(
                Iterators.transform(argumentName.iterator(), createJsonPair()))
                + "}";
    }
    /**
     * <p>Generate name of path parameter.
     *
     * @param pathParam name of path parameter.
     * @return generated string, you need to wrap this String by {@code encodeURI} method.
     */
    abstract String generatePathParam(String pathParam);
    abstract Function<String, String> createJsonPair();
}
