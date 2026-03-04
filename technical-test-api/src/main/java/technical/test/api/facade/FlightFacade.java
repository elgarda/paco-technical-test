package technical.test.api.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import technical.test.api.mapper.AirportMapper;
import technical.test.api.mapper.FlightMapper;
import technical.test.api.record.FlightRecord;
import technical.test.api.representation.FlightRepresentation;
import technical.test.api.services.AirportService;
import technical.test.api.services.FlightService;

import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class FlightFacade {
    private static final String SORT_PRICE = "price";
    private static final String SORT_ORIGIN = "origin";

    private final FlightService flightService;
    private final AirportService airportService;
    private final FlightMapper flightMapper;
    private final AirportMapper airportMapper;

    /**
     * Récupère les vols enrichis avec une option de tri.
     * @param sort le critère de tri ("price" ou "origin")
     */
    public Flux<FlightRepresentation> getFlights(String sort) {
        return flightService.getAllFlights()
                .flatMap(this::enrichFlight)
                .sort(getComparator(sort));
    }

    /**
     * Création d'un vol.
     */
    public Mono<FlightRepresentation> createFlight(FlightRepresentation flightRepresentation) {
        FlightRecord record = flightMapper.convert(flightRepresentation);
        return flightService.addFlight(record)
                .flatMap(this::enrichFlight);
    }

    /**
     * Enrichit un vol avec les informations complètes des aéroports.
     */
    private Mono<FlightRepresentation> enrichFlight(FlightRecord flightRecord) {
        return airportService.findByIataCode(flightRecord.getOrigin())
                .zipWith(airportService.findByIataCode(flightRecord.getDestination()))
                .map(tuple -> {
                    FlightRepresentation flightRepresentation = this.flightMapper.convert(flightRecord);
                    flightRepresentation.setOrigin(this.airportMapper.convert(tuple.getT1()));
                    flightRepresentation.setDestination(this.airportMapper.convert(tuple.getT2()));
                    return flightRepresentation;
                });
    }

    /**
     * Le comparateur.
     */
    private Comparator<FlightRepresentation> getComparator(String sort) {
        if (sort == null) {
            return Comparator.comparing(FlightRepresentation::getDeparture);
        }

        return switch (sort.toLowerCase()) {
            case SORT_PRICE -> Comparator.comparing(FlightRepresentation::getPrice);
            case SORT_ORIGIN -> Comparator.comparing(f -> f.getOrigin().getIata());
            default -> throw new IllegalArgumentException("Invalid sort parameter: " + sort);
        };
    }
}

