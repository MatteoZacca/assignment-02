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

        FileSystem fs = vertx.fileSystem();

        return fs.exists(path)
                .compose(exists -> exists ?
                        vertx.executeBlocking(promise -> {
                            try {
                                if (path == null || path.isBlank()) {
                                    throw new FileNotFoundException("Percorso vuoto");
                                }
                                ClassDepsReport report = parseClassSync(new File(path));
                                promise.complete(report);
                            } catch (Exception ex) {
                                promise.fail(ex);
                            }
                        })
                        : Future.failedFuture(new IllegalArgumentException("File non trovato: " + path)));
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

        return readDirRecursive(rootPath)
                .compose(javaFiles -> {
                    List<Future<ClassDepsReport>> futures = javaFiles.stream()
                            .map(this::getClassDependencies)
                            .collect(Collectors.toList());
                    return Future.all(futures);
                })
                .map(composite -> new ProjectDepsReport(composite.list()));
    }

    // AST visit logic
    private ClassDepsReport parseClassSync(File file) throws Exception {
        ParserConfiguration config = new ParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        StaticJavaParser.setConfiguration(config);

        CompilationUnit cu = StaticJavaParser.parse(file);

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

    // it returns all '.java' files starting from dirPath
    private Future<List<String>> readDirRecursive(String dirPath) {
        FileSystem fs = vertx.fileSystem();

        return fs.readDir(dirPath)
                .compose(entries -> {
                    List<Future<List<String>>> futures = new ArrayList<>();

                    for (String entry : entries) {
                        if (entry.endsWith(".java")) {
                            futures.add(Future.succeededFuture(List.of(entry)));
                        } else {
                            Future<List<String>> dirCheckFuture = fs.props(entry).compose(props -> {
                                if (props.isDirectory()) {
                                    // Recursive call
                                    return readDirRecursive(entry);
                                } else {
                                    // Ignore other files
                                    return Future.succeededFuture(new ArrayList<String>());
                                }
                            });
                            futures.add(dirCheckFuture);
                        }
                    }

                    return Future.all(futures).map(composite -> {
                        List<String> javaFiles = new ArrayList<>();
                        composite.<List<String>>list().forEach(javaFiles::addAll);
                        return javaFiles;
                    });
                });
    }

    public void close() {
        if (this.vertx != null) {
            this.vertx.close();
        }
    }

    private boolean isDirectory(String path) {
        return new File(path).isDirectory();
    }

}
