package pcd.ass02.asynch_programming;

import io.vertx.core.Future;
import pcd.ass02.asynch_programming.DependencyAnalyzerLib.*;

public class TestDependencyAnalyzerLib {
    public static void main(String[] args) throws Exception {
        DependencyAnalyzerLib deps = new DependencyAnalyzerLib();

        String basePath = "C:\\Users\\zacca\\Desktop\\assignment-02\\src\\main\\java\\pcd\\ass02\\asynch_programming\\";

        Future<ClassDepsReport> f1 = testGetClassDependencies(deps, basePath + "MyClass.java");
        Future<ClassDepsReport> f3 = testGetClassDependencies(deps, "C.java");

        Future<PackageDepsReport> f4 = testGetPackageDependencies(deps, basePath + "foopack");
        Future<PackageDepsReport> f5 = testGetPackageDependencies(deps, "foopack2");

        Future<ProjectDepsReport> f7 = testGetProjectDependencies(deps, ".");

        Future.join(f1, f3, f4, f5, f7)
                .onComplete(res -> {
                    log("All tests terminated. Closing Vert.x...");
                    deps.close();
                });
    }

    private static Future<ClassDepsReport> testGetClassDependencies(DependencyAnalyzerLib deps, final String filePath) {
        return deps.getClassDependencies(filePath)
                .onComplete(list -> {
                    if (list.succeeded()) {
                        log("dependencies for [" + filePath + "] \n ---> " + list.result() + "\n");
                    } else {
                        log("failure for [" + filePath + "] \n ---> " + list.cause() + "\n");
                    }
                });
    }

    private static Future<PackageDepsReport> testGetPackageDependencies(DependencyAnalyzerLib deps, final String packagePath) {
        return deps.getPackageDependencies(packagePath)
                .onComplete(list -> {
                    if (list.succeeded()) {
                        log("dependencies for [" + packagePath + "] \n ---> " + list.result() + "\n");
                    } else {
                        log("failure for [" + packagePath + "] \n ---> " + list.cause() + "\n");
                    }
                });
    }

    private static Future<ProjectDepsReport> testGetProjectDependencies(DependencyAnalyzerLib deps, final String projectPath) {
        return deps.getProjectDependencies(projectPath)
                .onComplete(list -> {
                    if (list.succeeded()) {
                        log("dependencies for [" + projectPath + "] \n ---> " + list.result() + "\n");
                    } else {
                        log("failure for [" + projectPath + "] \n ---> " + list.cause() + "\n");
                    }
                });
    }

    private static void log(String msg) {
        System.out.println("[" + System.currentTimeMillis() + "] [" + Thread.currentThread().getName() + "]: " + msg);
    }


}

