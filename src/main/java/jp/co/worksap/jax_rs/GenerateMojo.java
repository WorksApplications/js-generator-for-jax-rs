package jp.co.worksap.jax_rs;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import com.google.common.collect.Lists;

/**
 * @goal generate
 * @phase process-classes
 * @author Kengo TODA
 */
public final class GenerateMojo extends AbstractMojo {
    /**
     * Package which target controllers belong.
     *
     * @parameter
     * @required
     */
    protected String packageName;
    /**
     * Place where generated scripts will be stored.
     *
     * @parameter
     * @required
     */
    protected File outputDirectory;
    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;
    /**
     * @parameter default-value="application-data"
     * @required
     */
    private String metaTagName;
    /**
     * @parameter default-value="context-path"
     * @required
     */
    private String dataNameToGetContextPath;
    /**
     * @parameter default-value="SAME_TO_JAVA"
     * @required
     */
    private ArgumentInterface argumentInterface;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        outputDirectory.mkdirs();
        deleteChildren(outputDirectory, getLog());
        String[] controllerNames = listControllerNames();
        try {
            URLClassLoader newLoader = createClassLoader();
            Class<?>[] controllerClasses = loadClasses(newLoader, controllerNames);
            Class<?> generatorClass = newLoader.loadClass(ApiScriptGenerator.class.getName());
            Constructor<?> constructor = generatorClass.getConstructor(Class[].class);
            Object generator = constructor.newInstance(new Object[]{controllerClasses});
            Method execute = generatorClass.getMethod("execute", File.class, String.class, String.class);
            execute.invoke(generator, outputDirectory, metaTagName, dataNameToGetContextPath, argumentInterface);
        } catch (IOException e) {
            throw new MojoExecutionException("IOException occurs", e);
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException(
                    "DependencyResolutionRequiredException occurs", e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException("ClassNotFoundException occurs", e);
        } catch (SecurityException e) {
            throw new MojoExecutionException("SecurityException occurs", e);
        } catch (NoSuchMethodException e) {
            throw new MojoExecutionException("NoSuchMethodException occurs", e);
        } catch (InstantiationException e) {
            throw new MojoExecutionException("InstantiationException occurs", e);
        } catch (IllegalAccessException e) {
            throw new MojoExecutionException("IllegalAccessException occurs", e);
        } catch (InvocationTargetException e) {
            throw new MojoExecutionException("InvocationTargetException occurs", e);
        }
    }

    private Class<?>[] loadClasses(URLClassLoader newLoader, String[] controllerNames)
            throws MalformedURLException,
            DependencyResolutionRequiredException, ClassNotFoundException {
        List<Class<?>> result = Lists.newArrayList();
        for (String name : controllerNames) {
            result.add(newLoader.loadClass(name));
        }
        Thread.currentThread().setContextClassLoader(newLoader);
        return result.toArray(new Class<?>[result.size()]);
    }

    @SuppressWarnings("unchecked")
    private URLClassLoader createClassLoader()
            throws DependencyResolutionRequiredException, MalformedURLException {
        List<String> classpathElements = project.getCompileClasspathElements();
        for (Object o : project.getDependencyArtifacts()) {
            Artifact a = (Artifact) o;
            classpathElements.add(a.getFile().getAbsolutePath());
        }
        URL[] runtimeUrls = new URL[classpathElements.size()];
        for (int i = 0; i < classpathElements.size(); i++) {
            String element = classpathElements.get(i);
            runtimeUrls[i] = new File(element).toURI().toURL();
        }
        URLClassLoader newLoader = new URLClassLoader(runtimeUrls, Thread
                .currentThread().getContextClassLoader());
        return newLoader;
    }

    private String[] listControllerNames() {
        File controllerDir = searchControllerDirectory();
        List<String> result = Lists.newArrayList();
        if (!controllerDir.exists() || !controllerDir.isDirectory()) {
            getLog().warn("There is no file in controller directory.");
            return new String[0];
        }
        for (File file : controllerDir.listFiles()) {
            if (!file.isFile() || !file.getName().endsWith(".java")) {
                continue;
            }
            result.add(packageName + '.' + dropExtension(file.getName()));
        }
        return result.toArray(new String[result.size()]);
    }

    private File searchControllerDirectory() {
        Log logger = getLog();
        File baseDir = project.getBasedir();
        logger.info(
                "Base directory of project is:" + baseDir.getAbsolutePath());

        File sourceDir = new File(baseDir, "src/main/java");
        File controllerDir = new File(sourceDir, packageName.replace('.',
                File.separatorChar));
        logger.info(
                "Controller directory is:" + controllerDir.getAbsolutePath());
        return controllerDir;
    }

    String dropExtension(String name) {
        int index = name.indexOf('.');
        return name.substring(0, index);
    }

    private void deleteChildren(File directory, Log log) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                deleteChildren(file, log);
            }
            if (!file.delete()) {
                log.warn(
                    "Failed to remove old JavaScript API component. File name is: " +
                    file.getAbsolutePath());
            }
        }
    }

}
