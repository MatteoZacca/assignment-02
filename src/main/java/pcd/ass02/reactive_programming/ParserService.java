package pcd.ass02.reactive_programming;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import io.reactivex.rxjava3.core.Observable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ParserService {

    /**
     * Restituisce un Observable dei percorsi dei file .java sotto la directory root.
     */
    public static Observable<Path> listJavaFiles(Path root) {
        return Observable.create(emitter -> {
            try (Stream<Path> files = Files.walk(root)) {
                log("Sono entrato nel metodo listJavaFiles");
                files
                        .filter(f -> f.toString().endsWith(".java"))
                        .forEach(emitter::onNext);

                emitter.onComplete();
            } catch (IOException e) {
                emitter.onError(e);
            }
        });
    }

    /**
     * Analizza un singolo file Java e restituisce un Observable con la lista delle dipendenze trovate.
     */
    public static Observable<List<Dependency>> parseFile(Path file) {
        return Observable.create(emitter -> {
            try {
                CompilationUnit cu = StaticJavaParser.parse(file);
                List<Dependency> deps = extractDependencies(file.getFileName().toString(), cu);
                emitter.onNext(deps);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }

    /**
     * Estrae le dipendenze dal CompilationUnit:
     * - package declarations
     * - import statements
     * - estensioni di classi / implementazioni di interfacce
     */
    private static List<Dependency> extractDependencies(String source, CompilationUnit cu) {
        List<Dependency> deps = new ArrayList<>();

        // 1) Package declaration
        cu.getPackageDeclaration()
                .map(PackageDeclaration::getNameAsString)
                .ifPresent(pkg -> deps.add(new Dependency("package: '" + pkg + "'", "class: '"+ source + "'")));

        // 2) Import
        for (ImportDeclaration imp : cu.findAll(ImportDeclaration.class)) {
            deps.add(new Dependency("class: '" + source + "'", "import: '" + imp.getName().toString() + "'"));
        }

        // 3) Extends / Implements
        for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            for(ClassOrInterfaceType ext : cid.getExtendedTypes()) {
                deps.add(new Dependency("class: '" + source + "'", "extends: '" + ext.getName().toString() + "'"));
            }
            for (ClassOrInterfaceType impl : cid.getImplementedTypes()) {
                deps.add(new Dependency("class: '" + source + "'", "implements: '" + impl.getName().toString() + "'"));
            }
        }

        return deps;
    }

    private static void log (String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + msg);
    }
}