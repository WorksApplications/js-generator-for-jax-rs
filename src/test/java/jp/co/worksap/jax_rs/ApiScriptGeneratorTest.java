package jp.co.worksap.jax_rs;

import static jp.co.worksap.jax_rs.ArgumentInterface.ONE_OBJECT;
import static jp.co.worksap.jax_rs.ArgumentInterface.SAME_TO_JAVA;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jp.co.worksap.jax_rs.sample.SimpleAPI;
import jp.co.worksap.jax_rs.sample.SimpleImplementation;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class ApiScriptGeneratorTest {
    @Test
    public void testDefault() throws IOException {
        File expect = new File("src/test/expect/same_to_java/simpleAPI.js");
        File actual = new File("target/actual/same_to_java/simpleAPI.js");
        Files.createParentDirs(actual);
        new ApiScriptGenerator(SimpleAPI.class).execute(actual.getParentFile(), "app-data", "context-path", SAME_TO_JAVA);

        List<String> expectedCode = Files.readLines(expect, Charsets.UTF_8);
        List<String> actualCode = Files.readLines(actual, Charsets.UTF_8);
        if (!expectedCode.equals(actualCode)) {
            System.err.println("expected:");
            System.err.println(Files.toString(expect, Charsets.UTF_8));
            System.err.println("actual:");
            System.err.println(Files.toString(actual, Charsets.UTF_8));
            fail();
        }
    }

    @Test
    public void testOneObjectStyle() throws IOException {
        File expect = new File("src/test/expect/one_object_interface/simpleAPI.js");
        File actual = new File("target/actual/one_object_interface/simpleAPI.js");
        Files.createParentDirs(actual);
        new ApiScriptGenerator(SimpleAPI.class).execute(actual.getParentFile(), "app-data", "context-path", ONE_OBJECT);

        List<String> expectedCode = Files.readLines(expect, Charsets.UTF_8);
        List<String> actualCode = Files.readLines(actual, Charsets.UTF_8);
        if (!expectedCode.equals(actualCode)) {
            System.err.println("expected:");
            System.err.println(Files.toString(expect, Charsets.UTF_8));
            System.err.println("actual:");
            System.err.println(Files.toString(actual, Charsets.UTF_8));
            fail();
        }
    }

    /**
     * <p>We do not support inheritance of annotations applied to interface types.</p>
     * @see https://github.com/WorksApplications/js-generator-for-jax-rs/issues/4
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGenerateFromImplementation() throws IOException {
        File actual = new File("target/actual/simpleImplementation.js");
        Files.createParentDirs(actual);
        new ApiScriptGenerator(SimpleImplementation.class).execute(actual.getParentFile(), "app-data", "context-path");
    }

    @Test
    public void testEscapeTagName() {
        ApiScriptGenerator generator = new ApiScriptGenerator(SimpleAPI.class);
        assertThat(generator.makeScriptToGetBaseURL("foo-bar", ""), is(equalTo(scriptFor("foo-bar"))));
        assertThat(generator.makeScriptToGetBaseURL("<some'attr>", ""), is(equalTo(scriptFor("&lt;some&#x27;attr&gt;"))));
        assertThat(generator.makeScriptToGetBaseURL("another\"attr", ""), is(equalTo(scriptFor("another&#x22;attr"))));
    }

    private String scriptFor(String escapedTagName) {
        return String.format("  var baseURL = $('meta[name=\"%s\"]').data('') + '/resources';\n", escapedTagName);
    }
}
