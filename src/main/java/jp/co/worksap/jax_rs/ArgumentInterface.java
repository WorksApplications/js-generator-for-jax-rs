package jp.co.worksap.jax_rs;

enum ArgumentInterface {
    /**
     * <p>Each function get some objects as argument.
     * Default.
     */
    SAME_TO_JAVA,
    /**
     * <p>Each function get only one object as argument.
     * See {@code src/test/expect/one_object_interface/simpleAPI.js} 
     */
    ONE_OBJECT
}
