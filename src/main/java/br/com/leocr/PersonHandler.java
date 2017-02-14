package br.com.leocr;

import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PersonHandler {

    private final PersonRepository repository;

    public PersonHandler(PersonRepository repository) {
        this.repository = repository;
    }

    public Mono<ServerResponse> getPerson(ServerRequest request) {
        int personId = Integer.valueOf(request.pathVariable("id"));
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        return this.repository.getPerson(personId)
                .then(person -> {
                    Publisher<Person> personPublisher = Mono.just(person);
                    return ServerResponse.ok().body(personPublisher, Person.class);
                })
                .otherwiseIfEmpty(notFound);
    }


    public Mono<ServerResponse> createPerson(ServerRequest request) {
        Mono<Person> person = request.bodyToMono(Person.class);
        return ServerResponse.ok().build(this.repository.savePerson(person));
    }

    public Mono<ServerResponse> listPeople(ServerRequest request) {
        Flux<Person> people = this.repository.allPeople();
        return ServerResponse.ok().body(people, Person.class);
    }
}