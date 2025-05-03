package pcd.ass02;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

import pcd.ass02.DependencyAnalyzerLib.*;

public class TestDependencyAnalyzerLib {
    public static void main(String[] args) throws Exception {
        testGetClassDependencies("C:\\Users\\zacca\\Desktop\\MAGISTRALE INFORMATICA\\PRIMO ANNO\\SECONDO SEMESTRE\\Programmazione Concorrente e Distribuita\\Assignments\\Assignment#02\\assignment-02\\src\\main\\java\\pcd\\ass02\\MyClass.java");
        testGetClassDependencies("C:\\Users\\zacca\\Desktop\\MAGISTRALE INFORMATICA\\PRIMO ANNO\\SECONDO SEMESTRE\\Programmazione Concorrente e Distribuita\\Assignments\\Assignment#02\\assignment-02\\src\\main\\java\\pcd\\ass02\\ValeYellow.java");
        testGetClassDependencies("");

        testGetPackageDependencies("C:\\Users\\zacca\\Desktop\\MAGISTRALE INFORMATICA\\PRIMO ANNO\\SECONDO SEMESTRE\\Programmazione Concorrente e Distribuita\\Assignments\\Assignment#02\\assignment-02\\src\\main\\java\\pcd\\ass02\\foopack");
        testGetPackageDependencies(".");

        testGetProjectDependencies("C:\\Users\\zacca\\Desktop\\MAGISTRALE INFORMATICA\\PRIMO ANNO\\SECONDO SEMESTRE\\Programmazione Concorrente e Distribuita\\Assignments\\Assignment#02\\assignment-02\\src\\main\\java\\pcd\\ass02\\foopack");
        testGetProjectDependencies(".");
    }

    private static void testGetClassDependencies(final String filePath) {
        DependencyAnalyzerLib deps = new DependencyAnalyzerLib();

        Future<ClassDepsReport> fut = deps.getClassDependencies(filePath);
        fut
                .onComplete((AsyncResult<ClassDepsReport> list) -> {
                    if (list.succeeded()) {
                        log("dependencies for [" + filePath + "] \n ---> " + list.result() + "\n\n");
                    } else if (list.failed()) {
                        log("failure for [" + filePath + "] \n ---> " + list.cause() + "\n\n");
                    }
                });
    }

    private static void testGetPackageDependencies(final String packagePath) {
        DependencyAnalyzerLib deps = new DependencyAnalyzerLib();

        Future<PackageDepsReport> fut = deps.getPackageDependencies(packagePath);
        fut
                .onComplete((AsyncResult<PackageDepsReport> list) -> {
                    if (list.succeeded()) {
                        log("dependencies for [" + packagePath + "] \n ---> " + list.result() + "\n\n");
                    } else if (list.failed()) {
                        log("failure for [" + packagePath + "] \n ---> " + list.cause() + "\n\n");
                    }
                });
    }

    private static void testGetProjectDependencies(final String projectPath) {
        DependencyAnalyzerLib deps = new DependencyAnalyzerLib();

        Future<ProjectDepsReport> fut = deps.getProjectDependencies(projectPath);
        fut
                .onComplete((AsyncResult<ProjectDepsReport> list) -> {
                    if (list.succeeded()) {
                        log("dependencies for [" + projectPath + "] \n ---> " + list.result() + "\n\n");
                    } else if (list.failed()) {
                        log("failure for [" + projectPath + "] \n ---> " + list.cause() + "\n\n");
                    }
                });
    }

    private static void log(String msg) {
        System.out.println("[" + System.currentTimeMillis() + "] [" + Thread.currentThread() + "]: " + msg);
    }


}

