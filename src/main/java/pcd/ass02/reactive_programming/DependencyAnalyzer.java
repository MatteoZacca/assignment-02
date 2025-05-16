package pcd.ass02.reactive_programming;


import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.nio.file.Path;

public class DependencyAnalyzer {

    /* Sto didcendo: creo un nuovo PublishSubject pronto per ricevere ed emettere eventi.
    Un Observable è un flusso di dati immutabile, non puoi "spingere" nuovi eventi nel
    flusso dopo la creazione, mentre PublishSubject è adatto a scenari come questo poichè
    i file da analizzare non sono noti a priori e vengono aggiunti dinamicamente. */
    private final PublishSubject<Path> fileSubject = PublishSubject.create();
    private final PublishSubject<Dependency> depSubject = PublishSubject.create();

    private final GraphService graphService;
    private int filesProcessed = 0;
    private int dependenciesFound = 0;

    public DependencyAnalyzer(GraphService graphService) {
        this.graphService = graphService;
        setupPipeline();
    }


    public void processDirectory(Path root){
        log("Sono entrato nel metodo processDirectory");
        ParserService
                .listJavaFiles(root) // restituisce un Observable<Path>
                .subscribeOn(Schedulers.io())
                .subscribe(
                        file -> {
                            fileSubject.onNext(file);
                            // Debugging log
                            log("fileSubject.onNext(" + file + ")");
                        },
                        Throwable::printStackTrace,
                        () -> {
                            fileSubject.onComplete();
                            log("listJavaFiles: fileSubject.onComplete()");
                        }
                );
    }

    public int getFilesProcessed() {
        return filesProcessed;
    }

    public int getDependenciesFound() {
        return dependenciesFound;
    }

    private void setupPipeline() {
        log("Preparing pipeline... \n\n");
        fileSubject
                // sposto l'esecuzione della pipeline su un thread I/O
                .observeOn(Schedulers.io())
                // Debugging
                .doOnNext(pathToFile -> {
                    log("\nFile: " + pathToFile);
                })
                .flatMap(ParserService::parseFile)
                .doOnNext(listDeps -> filesProcessed++)
                // flatMapIterable(listDeps -> listDeps): prende ogni List<Dependency> emessa
                // e la spezza in singoli elementi
                .flatMapIterable(listDeps -> listDeps)
                .subscribe(
                        depSubject::onNext, // value -> depSubject.onNext(value);
                        Throwable::printStackTrace,
                        () -> {
                            log("Parsing completato :)");
                            depSubject.onComplete();
                        }
                );

        depSubject
                .observeOn(Schedulers.computation())
                .doOnNext(d -> {
                    dependenciesFound++;
                    // Debugging log
                    log("Found dependency in " + d);
                })
                .subscribe(
                        graphService::addDependency,
                        Throwable::printStackTrace,
                        () -> log("Elaborazione delle dipendenze completata :)")
                );
    }

    private void log (String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "]: " + msg);
    }
}

