package technical.test.api.endpoints;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import technical.test.api.facade.FlightFacade;
import technical.test.api.representation.FlightRepresentation;

@RestController
@RequestMapping("/flight")
@RequiredArgsConstructor
public class FlightEndpoint {
    private final FlightFacade flightFacade;

    /**
     * Récupère la liste des vols paginée (six par page).
     * @param sort Critère de tri (price/origin)
     * @param page Numéro de la page
     */
    @GetMapping
    public Flux<FlightRepresentation> getAllFlights(
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page) {
        return flightFacade.getFlights(sort, page);
    }

    @PostMapping
    public Mono<FlightRepresentation> createFlight(@RequestBody FlightRepresentation flightRepresentation) {
        return flightFacade.createFlight(flightRepresentation);
    }
}
