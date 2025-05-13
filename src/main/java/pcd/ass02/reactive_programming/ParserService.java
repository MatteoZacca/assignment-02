package pcd.ass02.reactive_programming;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
                files.filter(f -> f.toString().endsWith(".java"))
                        .forEach(emitter::onNext);
                emitter.onComplete();
            } catch (IOException e) {
                emitter.onError(e);
            }
        });
    }

    /**
     * Analizza un singolo file Java e restituisce un Single con la lista delle dipendenze trovate.
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
     * - import statements
     * - estensioni di classi / implementazioni di interfacce
     */
    private static List<Dependency> extractDependencies(String source, CompilationUnit cu) {
        List<Dependency> deps = new ArrayList<>();

        // 1) Import
        for (ImportDeclaration imp : cu.findAll(ImportDeclaration.class)) {
            deps.add(new Dependency(source, imp.getName().toString()));
        }

        // 2) Extends / Implements
        for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            for(ClassOrInterfaceType ext : cid.getExtendedTypes()) {
                deps.add(new Dependency(source, "Extends: " + ext.getName().toString()));
            }
            for (ClassOrInterfaceType impl : cid.getImplementedTypes()) {
                deps.add(new Dependency(source, "Implements: " + impl.getName().toString()));
            }
        }

        return deps;
    }
}