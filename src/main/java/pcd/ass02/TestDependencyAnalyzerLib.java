package pcd.ass02;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

import pcd.ass02.DependencyAnalyzerLib.*;

public class TestDependencyAnalyzerLib {
    public static void main(String[] args) throws Exception {
        testGetClassDependencies("C:\\Users\\zacca\\Desktop\\MAGISTRALE INFORMATICA\\PRIMO ANNO\\SECONDO SEMESTRE\\Programmazione Concorrente e Distribuita\\Assignments\\Assignment#02\\assignment-02\\src\\main\\java\\pcd\\ass02\\MyClass.java");
        testGetClassDependencies("C:\\Users\\zacca\\Desktop\\MAGISTRALE INFORMATICA\\PRIMO ANNO\\SECONDO SEMESTRE\\Programmazione Concorrente e Distribuita\\Assignments\\Assignment#02\\assignment-02\\src\\main\\java\\pcd\\ass02\\ValeYellow.java");

        testGetPackageDependencies("C:\\Users\\zacca\\Desktop\\MAGISTRALE INFORMATICA\\PRIMO ANNO\\SECONDO SEMESTRE\\Programmazione Concorrente e Distribuita\\Assignments\\Assignment#02\\assignment-02\\src\\main\\java\\pcd\\ass02\\foopack");

    }

    private static void testGetClassDependencies(final String filePath) {
        DependencyAnalyzerLib deps = new DependencyAnalyzerLib();

        Future<ClassDepsReport> fut = deps.getClassDependencies(filePath);
        fut
                .onComplete((AsyncResult<ClassDepsReport> list) -> {
                    if (list.succeeded()) {
                        System.out.println("dependencies: \n" + list.result());
                    } else if (list.failed()) {
                        System.out.println("failure: \n" + list.cause());
                    }
                });
    }

    private static void testGetPackageDependencies(final String packagePath){
        DependencyAnalyzerLib deps = new DependencyAnalyzerLib();

        Future<PackageDepsReport> fut = deps.getPackageDependencies(packagePath);
        fut
                .onComplete((AsyncResult<PackageDepsReport> list) -> {
                    if (list.succeeded()) {
                        System.out.println("dependencies: \n" + list.result());
                    } else if (list.failed()) {
                        System.out.println("failure: \n" + list.cause());
                    }
                });
    }


}

