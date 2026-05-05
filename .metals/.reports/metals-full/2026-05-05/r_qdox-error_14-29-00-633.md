error id: file:///C:/Users/Usuario/Desktop/event-service/vivaeventos-event-service/src/main/java/co/edu/univalle/vivaeventoseventservice/infrastructure/web/EventController.java
file:///C:/Users/Usuario/Desktop/event-service/vivaeventos-event-service/src/main/java/co/edu/univalle/vivaeventoseventservice/infrastructure/web/EventController.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[137,1]

error in qdox parser
file content:
```java
offset: 5990
uri: file:///C:/Users/Usuario/Desktop/event-service/vivaeventos-event-service/src/main/java/co/edu/univalle/vivaeventoseventservice/infrastructure/web/EventController.java
text:
```scala
package co.edu.univalle.vivaeventoseventservice.infrastructure.web;

import co.edu.univalle.vivaeventoseventservice.application.dto.CreateEventRequest;
import co.edu.univalle.vivaeventoseventservice.application.dto.TicketTypeResponse;
import co.edu.univalle.vivaeventoseventservice.application.usecase.GetTicketTypesUseCase;
import co.edu.univalle.vivaeventoseventservice.application.usecase.ReserveStockUseCase;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.EventJpaRepository;
import co.edu.univalle.vivaeventoseventservice.application.dto.DefineTicketTypesRequest;
import co.edu.univalle.vivaeventoseventservice.application.dto.TicketTypeRequest;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeEntity;
import co.edu.univalle.vivaeventoseventservice.infrastructure.persistence.TicketTypeJpaRepository;
import co.edu.univalle.vivaeventoseventservice.domain.model.EventStatus;
import co.edu.univalle.vivaeventoseventservice.application.dto.EventSummaryResponse;
import co.edu.univalle.vivaeventoseventservice.application.dto.EventDetailResponse;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventJpaRepository eventJpaRepository;
    private final TicketTypeJpaRepository ticketTypeJpaRepository;
    private final GetTicketTypesUseCase getTicketTypesUseCase;
    private final ReserveStockUseCase reserveStockUseCase;

    public EventController(EventJpaRepository eventJpaRepository,
                           TicketTypeJpaRepository ticketTypeJpaRepository,
                           GetTicketTypesUseCase getTicketTypesUseCase,
                           ReserveStockUseCase reserveStockUseCase) {
        this.eventJpaRepository = eventJpaRepository;
        this.ticketTypeJpaRepository = ticketTypeJpaRepository;
        this.getTicketTypesUseCase = getTicketTypesUseCase;
        this.reserveStockUseCase = reserveStockUseCase;
    }

    @PostMapping
    public ResponseEntity<EventEntity> createEvent(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateEventRequest request
    ) {
        EventEntity entity = new EventEntity();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setEventDate(request.getEventDate());
        entity.setLocation(request.getLocation());
        entity.setCapacity(request.getCapacity());

        entity.setCreatedBy(userId);
        entity.setCreatedAt(Instant.now());

        EventEntity saved = eventJpaRepository.save(entity);
        return ResponseEntity.status(201).body(saved);
    }

    @PostMapping("/{eventId}/ticket-types")
    public ResponseEntity<?> defineTicketTypes(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID eventId,
            @Valid @RequestBody DefineTicketTypesRequest request
    ) {
        EventEntity event = eventJpaRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        int capacity = event.getCapacity();

        int totalExisting = ticketTypeJpaRepository.findByEvent_Id(eventId).stream()
                .mapToInt(TicketTypeEntity::getQuantityAvailable)
                .sum();

        int totalNew = request.getTicketTypes().stream()
                .mapToInt(TicketTypeRequest::getQuantityAvailable)
                .sum();

        if (totalExisting + totalNew > capacity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad total de boletas supera el aforo del evento (" + capacity + ")"
            );
        }

        List<TicketTypeEntity> entities = request.getTicketTypes().stream().map(t -> {
            TicketTypeEntity e = new TicketTypeEntity();
            e.setEvent(event);
            e.setType(t.getType());
            e.setPrice(t.getPrice());
            e.setQuantityAvailable(t.getQuantityAvailable());
            return e;
        }).toList();

        List<TicketTypeEntity> saved = ticketTypeJpaRepository.saveAll(entities);
        return ResponseEntity.status(201).body(saved);
    }

    // Listar tipos de boleta de un evento (cliente elige aquí)
    @GetMapping("/{eventId}/ticket-types")
    public ResponseEntity<List<TicketTypeResponse>> getTicketTypes(
            @PathVariable UUID eventId) {
        List<TicketTypeResponse> response = getTicketTypesUseCase
                .getByEvent(eventId)
                .stream()
                .map(TicketTypeResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    // Obtener un tipo específico (order-service consulta precio y stock)
    @GetMapping("/ticket-types/{ticketTypeId}")
    public ResponseEntity<TicketTypeResponse> getTicketType(
            @PathVariable UUID ticketTypeId) {
        return ResponseEntity.ok(
                TicketTypeResponse.from(getTicketTypesUseCase.getById(ticketTypeId)));
    }

    // Reservar stock (order-service llama esto al crear una orden)
    @PutMapping("/ticket-types/{ticketTypeId}/reserve")
    public ResponseEntity<Void> reserveStock(
            @PathVariable UUID ticketTypeId,
            @RequestParam int quantity) {
        reserveStockUseCase.execute(ticketTypeId, quantity);
        return ResponseEntity.ok().build();
    }

    @GetMapping



}@@
```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.metals.SemanticdbDefinition$.foreachWithReturnMtags(SemanticdbDefinition.scala:99)
	scala.meta.internal.metals.Indexer.indexSourceFile(Indexer.scala:560)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3(Indexer.scala:691)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3$adapted(Indexer.scala:688)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.Indexer.reindexWorkspaceSources(Indexer.scala:688)
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:940)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	java.base/java.lang.Thread.run(Thread.java:842)
```
#### Short summary: 

QDox parse error in file:///C:/Users/Usuario/Desktop/event-service/vivaeventos-event-service/src/main/java/co/edu/univalle/vivaeventoseventservice/infrastructure/web/EventController.java