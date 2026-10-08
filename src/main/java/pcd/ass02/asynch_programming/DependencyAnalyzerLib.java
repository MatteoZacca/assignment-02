package pcd.ass02.asynch_programming;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DependencyAnalyzerLib {

    public record ClassDepsReport(List<String> dependencies) {}
    public record PackageDepsReport(List<ClassDepsReport> classReports) {}
    public record ProjectDepsReport(List<PackageDepsReport> packageReports) {}

    private final Vertx vertx;

    public DependencyAnalyzerLib() {
        this.vertx = Vertx.vertx();
    }

    public Future<ClassDepsReport> getClassDependencies(String path) {

        if (path == null || path.isBlank()) {
            return Future.failedFuture(new IllegalArgumentException("Percorso vuoto o nullo"));
        }

        FileSystem fs = vertx.fileSystem();

        return fs.exists(path)
                .compose(exists -> exists ?
                        vertx.executeBlocking(promise -> {
                            try {
                                ClassDepsReport report = parseClassSync(new File(path));
                                promise.complete(report);
                            } catch (Exception ex) {
                                promise.fail(ex);
                            }
                        })
                        : Future.failedFuture(new IllegalArgumentException("File not found: " + path)));
    }

    public Future<PackageDepsReport> getPackageDependencies(String dirPath) {
        FileSystem fs = vertx.fileSystem();
        ;
        return fs.readDir(dirPath)
                .compose(paths -> {
                    List<String> javaFiles = paths.stream()
                            .filter(p -> p.endsWith(".java"))
                            .toList();

                    List<Future<ClassDepsReport>> futures = javaFiles.stream()
                            //.map(file -> this.getClassDependencies(file))
                            .map(this::getClassDependencies)
                            .collect(Collectors.toList());
                    return Future.all(futures);
                })
                .map(composite -> new PackageDepsReport(composite.list()));
    }

    public Future<ProjectDepsReport> getProjectDependencies(String rootPath) {

        return findDirectoriesRecursive(rootPath)
                .compose(directories -> {
                    List<Future<PackageDepsReport>> futures = directories.stream()
                            .map(this::getPackageDependencies)
                            .collect(Collectors.toList());
                    return Future.all(futures);
                })
                .map(composite -> {
                    List<PackageDepsReport> allPackages = composite.list();

                    // Remove empty packages
                    List<PackageDepsReport> validPackages = allPackages.stream()
                            .filter(p -> !p.classReports().isEmpty())
                            .collect(Collectors.toList());

                    return new ProjectDepsReport(validPackages);
                });
    }

    // AST visit logic
    private ClassDepsReport parseClassSync(File file) throws Exception {
        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);

        // // Istanzio un nuovo JavaParser locale (Thread-Safe), invece di usare quello Statico
        com.github.javaparser.JavaParser parser = new com.github.javaparser.JavaParser(config);

        // Parsing
        CompilationUnit cu = parser.parse(file).getResult()
                .orElseThrow(() -> new Exception("Error parsing file: " + file.getName()));

        // Using a Set to avoid inserting duplicated dependencies
        Set<String> deps = new HashSet<>();

        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration n, Void arg) {
                super.visit(n, arg);
                deps.add(n.getNameAsString());
            }

            @Override
            public void visit(FieldDeclaration n, Void arg) {
                super.visit(n, arg);
                deps.add(n.getVariables().get(0).getType().asString());
            }

            @Override
            public void visit(MethodDeclaration n, Void arg) {
                super.visit(n, arg);
                for (var p : n.getParameters()) {
                    deps.add(p.getType().asString());
                }
                deps.add(n.getType().asString());
            }

            @Override
            public void visit(ObjectCreationExpr n, Void arg) {
                super.visit(n, arg);
                deps.add(n.getType().asString());
            }

            @Override
            public void visit(VariableDeclarator n, Void arg) {
                super.visit(n, arg);
                deps.add(n.getType().asString());
            }

            @Override
            public void visit(ImportDeclaration n, Void arg) {
                super.visit(n, arg);
                deps.add(n.getNameAsString());
            }
        }.visit(cu, null);

        return new ClassDepsReport(new ArrayList<>(deps));
    }


    private Future<List<String>> findDirectoriesRecursive(String dirPath) {
        FileSystem fs = vertx.fileSystem();

        return fs.readDir(dirPath).compose(entries -> {
            List<Future<List<String>>> futures = new ArrayList<>();

            for (String entry : entries) {
                Future<List<String>> dirCheckFuture = fs.props(entry).compose(props -> {
                    if (props.isDirectory()) {
                        // if it is a directory make recursion
                        return findDirectoriesRecursive(entry);
                    } else {
                        // if it is not a directory return an empty list
                        return Future.succeededFuture(new ArrayList<String>());
                    }
                });
                futures.add(dirCheckFuture);
            }

            return Future.all(futures).map(composite -> {
                List<String> directories = new ArrayList<>();
                directories.add(dirPath);

                composite.<List<String>>list().forEach(directories::addAll);
                return directories;
            });
        });
    }

    public void close() {
        if (this.vertx != null) {
            this.vertx.close();
        }
    }

}
