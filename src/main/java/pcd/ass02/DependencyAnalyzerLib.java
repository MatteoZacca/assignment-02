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

    public record ClassDepsReport(List<String> dependencies) {
    }

    public record PackageDepsReport(List<ClassDepsReport> classReports) {
    }

    public record ProjectDepsReport(List<PackageDepsReport> packageReports) {
    }

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
        ;

        return fs.readDir(dirPath)
                .compose(paths -> {
                    List<String> javaFiles = paths.stream()
                            .filter(p -> p.endsWith(".java"))
                            .collect(Collectors.toList());

                    List<Future<ClassDepsReport>> futures = javaFiles.stream()
                            //.map(file -> this.getClassDependencies(file))
                            .map(this::getClassDependencies)
                            .collect(Collectors.toList()); // raccoglie i
                    // Future<ClassDepsReport> in una lista chiamata futures
                    return Future.all(futures); // restituisce un Future<CompositeFuture>
                })
                .map(composite -> new PackageDepsReport(composite.list()));
        // composite.list(): ottiene la lista dei risultati dai Future completati. Ogni
        // elemento della lista è un ClassDepsReport
    }

    public Future<ProjectDepsReport> getProjectDependencies(String rootPath) {

        return readDirRecursive(rootPath)
                // perlustro ricorsivamente tutte le subdirectory e ottengo la lista di file '.java'
                .compose(javaFiles -> {
                    // per ogni file '.java' ricorsivamente trovato chiamo getClassDependencies
                    List<Future<ClassDepsReport>> futures = javaFiles.stream()
                            .map(this::getClassDependencies)
                            .collect(Collectors.toList());
                    return Future.all(futures);
                })
                // quando tutte le classi sono state analizzate, incapsulo i report in ProjectDepsReport
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

    // ricorsivo che restituisce tutti i file '.java' a partire da dirPath
    private Future<List<String>> readDirRecursive(String dirPath) {
        FileSystem fs = vertx.fileSystem();

        return fs.readDir(dirPath)
                .compose(entries -> {
                    List<String> javaFiles = new ArrayList<>();
                    List<Future<List<String>>> subdirFutures = new ArrayList<>();

                    for (String entry : entries) {
                        if (entry.endsWith(".java")) {
                            javaFiles.add(entry);
                        } else if (isDirectory(entry)) {
                            subdirFutures.add(readDirRecursive(entry));
                        }
                    }

                    if (subdirFutures.isEmpty()) {
                        return Future.succeededFuture(javaFiles);
                    } else {
                        return Future
                                .all(subdirFutures)
                                .map(composite -> {
                                    // <List<String>> è necessaria per indicare esplicitamente
                                    // il tipo dei risultati attesi dal CompositeFuture. Senza questa
                                    // annotazione, il compilatore non può inferire automaticamnete il
                                    // tipo corretto
                                    // Codice equivalente:
                                    /*
                                    List<List<String>> listOfLists = composite.<List<String>>list();
                                    for (List<String> subList : listOfLists) {
                                    javaFiles.addAll(subList);
                                    }
                                    */
                                    composite.<List<String>>list().forEach(javaFiles::addAll);
                                    return javaFiles;
                                });
                    }
                });
    }

    private boolean isDirectory(String path) {
        return new File(path).isDirectory();
    }

}
