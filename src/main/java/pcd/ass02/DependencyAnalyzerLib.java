package pcd.ass02;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.FileSystemOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DependencyAnalyzerLib {

    public record ClassDepsReport(List<String> dependencies) {}

    public record PackageDepsReport(List<ClassDepsReport> classReports) {}

    public record ProjectDepsReport(List<PackageDepsReport> packageReports) {}

    private final Vertx vertx;

    public DependencyAnalyzerLib() {
        this.vertx = Vertx.vertx(); // crea un'istanza di Vert.x
    }

    // getClassDependencies analizza asincronamente le dipendenze di una classe Java.
    // Il parametro path è una stringa che rappresenta il percorso al file Java
    public Future<ClassDepsReport> getClassDependencies(String path) {
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
                        : Future.failedFuture(new IllegalArgumentException("File non trovato: " + path)));
    }

    public Future<PackageDepsReport> getPackageDependencies(String dirPath) {
        FileSystem fs = vertx.fileSystem();

        return fs.readDir(dirPath)
                .compose(paths -> {
                    // filtro solo i file .java
                    List<String> javaFiles = paths.stream()
                            .filter(p -> p.endsWith(".java"))
                            .collect(Collectors.toList());
                    // per ogni file .java chiamo getClassDependencies
                    List<Future<ClassDepsReport>> futures = javaFiles.stream()
                            //.map(file -> this.getClassDependencies(file))
                            .map(this::getClassDependencies)
                            .collect(Collectors.toList()); // raccoglie i
                    // Future<ClassDepsReport> in una lista chiamata futures.
                    // attendo che tutte le analisi delle classi siano completate
                    return Future.all(futures); // restituisce un Future<CompositeFuture>
                })
                .map(composite -> new PackageDepsReport(composite.list()));
        // composite.list(): ottiene la lista dei risultati dai Future completati. Ogni
        // elemento della lista è un ClassDepsReport
    }

    public Future<ProjectDepsReport> getProjectDependencies(String rootPath) {
        FileSystem fs = vertx.fileSystem();

        // Legge tutti i packages ricorsivamente
        return fs.readDir(rootPath,)//new FileSystem.ReadOptions().setRecursive(true))
                .compose(allPaths -> {
                    List<String> javaFiles = allPaths.stream()
                            .filter(p -> p.endsWith(".java"))
                            .collect(Collectors.toList());
                    List<Future<ClassDepsReport>> futures = javaFiles.stream()
                            .map(this::getClassDependencies)
                            .collect(Collectors.toList());
                    return Future.all(futures);
                })
                .map(composite -> new ProjectDepsReport(composite.list()));
    }

    /* Isolo la logica di "visita" dell'AST in un metodo sincrono che prende un File e
    restituisce un ClassDepsReport */
    private ClassDepsReport parseClassSync(File file) throws Exception {
        CompilationUnit cu = StaticJavaParser.parse(file);
        List<String> deps = new ArrayList<>();

        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ImportDeclaration n, Void arg) {
                super.visit(n, arg);
                deps.add(n.getNameAsString());
            }
            // Aggiungi altri visit() se necessario
        }.visit(cu, null);

        return new ClassDepsReport(deps);
    }
}
