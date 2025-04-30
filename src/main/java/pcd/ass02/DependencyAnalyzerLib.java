package pcd.ass02;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystem;

import java.io.File;
import java.util.List;

public class DependencyAnalyzerLib {

    public record ClassDepsReport(List<String> dependencies) {}
    public record PackageDepsReport(List<ClassDepsReport> classReports) {}
    public record ProjectDepsReport(List<PackageDepsReport> packageReports) {}

    //private final Vertx vertx;

    /*public DependencyAnalyzerLib() {
        this.vertx = Vertx.vertx(); // crea un'istanza di Vert.x
    }*/

    public Future<ClassDepsReport> getClassDependencies(String path) {
        Vertx vertx = Vertx.vertx();
        FileSystem fs = vertx.fileSystem();

        return fs.exists(path)
                .compose(exists -> {
                    if (!exists) {
                        return Future.failedFuture(new IllegalArgumentException("File non trovato: " + path));
                    }

                    return vertx.executeBlocking(promise -> {
                        try {
                            ClassDepsReport report = parseClassSync(new File(path));
                            promise.complete(report);
                        } catch (Exception ex) {
                            promise.fail(ex);
                        }
                    });
                });
    }



    public Future<PackageDepsReport> getPackageDependencies(String dirPath) {}

    public Future<ProjectDepsReport> getProjectDependencies(String rootPath) {}

}
