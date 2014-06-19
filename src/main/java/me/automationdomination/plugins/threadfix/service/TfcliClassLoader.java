package me.automationdomination.plugins.threadfix.service;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * Created by xolian on 6/19/14.
 */
public class TfcliClassLoader extends URLClassLoader {

        private static TfcliClassLoader _instance;
        private ClassLoader currentThreadLoader;

        private static Properties prop = new Properties();

        public TfcliClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
            currentThreadLoader = null;
        }

        public static void reset() {
            _instance = null;
        }

        /** A special hack for Hudson
         * <p>
         * When Hudson loads Fortify360Plugin, it use a special ClassLoader, let's call it PluginClassLoader.
         * But when a Hudson job runs, it will run as a normal worker thread, and this worker thread is not PluginClassLoader.
         * Therefore, it won't not found the fortifyclient.jar which is stored in Fortify360plugin as a resources.
         * <p>
         * Therefore, the solution is when we need to run FortifyClient, we will need to change the worker thread ClassLoader
         * to *this* ClassLoader (which is the PluginClassLoader)
         * </p>
         *
         */
        public void bindCurrentThread() {
            if ( null != currentThreadLoader ) {
                throw new IllegalStateException(this.getClass().getName() + ".bindCurrentThread(), rebinding is not allowed. Probably previously binded without unbinding. currentThreadLoader is " + currentThreadLoader);
            }
            currentThreadLoader =  Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this);
        }

        /** A special hack for Hudson
         * <p>
         * When Hudson loads Fortify360Plugin, it use a special ClassLoader, let's call it PluginClassLoader.
         * But when a Hudson job runs, it will run as a normal worker thread, and this worker thread is not PluginClassLoader.
         * Therefore, it won't not found the fortifyclient.jar which is stored in Fortify360plugin as a resources.
         * <p>
         * Therefore, the solution is when we need to run FortifyClient, we will need to change the worker thread ClassLoader
         * to *this* ClassLoader (which is the PluginClassLoader)
         * </p>
         *
         */
        public void unbindCurrentThread() {
            if ( null != currentThreadLoader ) {
                Thread.currentThread().setContextClassLoader(currentThreadLoader);
                currentThreadLoader = null;
            }
        }

        @Override
        protected Class<?> findClass(String className) throws ClassNotFoundException {
            try {
                return super.findClass(className);
            } catch ( ClassNotFoundException e ) {
                if ( null != currentThreadLoader ) {
                    return currentThreadLoader.loadClass(className);
                } else {
                    throw e;
                }
            }
        }

        @Override
        public URL getResource(String name) {
            URL url = findResource(name);
            if ( null == url ) {
                if ( null != currentThreadLoader ) {
                    url = currentThreadLoader.getResource(name);
                }
            }
            return url;
        }
       /*
        public static String findWSClientPath() {
            File[] list = PathUtils.locateBaesnameInPath("tfcli.jar");
            if ( null != list && list.length > 0 ) {
                File f = list[0];
                // e.g. f is "C:\\Program Files\\Fortify Software\\Fortify 360 v2.6.5\\bin\\fortifyclient.bat"
                // we need to change this to "C:\\Program Files\\Fortify Software\\Fortify 360 v2.6.5\\Core\\lib
                File core = new File(f.getParentFile().getParentFile(), "Core");
                File lib = new File(core, "lib");
                File tfcli = findFileByJarBasename(lib, "tfcli");
                if ( null != tfcli ) return lib.toString();
            }
            return null;
        }
        */
        /** Find the file by using the jar basename
         * @param path e.g. a library path
         * @param basename e.g. tfcli
         * @return the jar file with the given basename
         */
        public static File findFileByJarBasename(File path, String basename) {
            if ( !path.exists() || !path.isDirectory() ) return null;

            // find by exact tfcli.jar
            File exactFile = new File(path, basename + ".jar");
            if ( exactFile.exists() ) return exactFile;;
            return null;
        }
}
