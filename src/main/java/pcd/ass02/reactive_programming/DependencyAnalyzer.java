package pcd.ass02.reactive_programming;


import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.nio.file.Path;

public class DependencyAnalyzer {

    /* Sto didcendo: creo un nuovo PublishSubject pronto per ricevere ed emettere eventi.
    Un Observable è un flusso di dati immutabile, non puoi "spingere" nuovi eventi nel
    flusso dopo la creazione, mentre PublishSubject è adatto a scenari come questo poichè
    i file da analizzare non sono noti a priori e vengono aggiunti dinamicamente. */
    private final PublishSubject<Path> fileSubject = PublishSubject.create();
    private final PublishSubject<Dependency> depSubject = PublishSubject.create();

    private final GraphService graphService = new GraphService();
    private int filesProcessed = 0;
    private int dependenciesFound = 0;

    public DependencyAnalyzer() {
        setupPipeline();
    }

    private void setupPipeline() {
        fileSubject
                // sposto l'esecuzione della pipeline su un thread I/O
                .subscribeOn(Schedulers.io())
                // doOnNext permette di eseguire un'azione ogni volta che un elemento passa
                // attraverso il flusso reattivo, senza modificarlo o interrompere il flusso
                .doOnNext(path -> {
                    System.out.println("Analyzing: " + path);
                })
                .flatMap(path -> ParserService.parseFile(path)
                        .toObservable()
                        .doOnNext(listOfDeps -> filesProcessed++)
                        .flatMapIterable(list ->list))
                .subscribe(
                        depSubject::onNext, //value -> depSubject.onNext(value);
                        Throwable::printStackTrace,
                        () -> System.out.println("Parsing completato")
                );

        depSubject
                .observeOn(Schedulers.computation())
                .doOnNext(d -> dependenciesFound++)
                .subscribe(dep -> {
                    graphService.addDependency(dep);
                        },
                        Throwable::printStackTrace,
                        () -> System.out.println("Elaborazione delle dipendenze completata")
                );
    }

    public void processDirectory(Path root){
        ParserService.listJavaFiles(root)
                .subscribe(
                        fileSubject::onNext,
                        Throwable::printStackTrace,
                        fileSubject::onComplete
                );
    }

    public int getFilesProcessed() {
        return filesProcessed;
    }

    public int getDependenciesFound() {
        return dependenciesFound;
    }
}

