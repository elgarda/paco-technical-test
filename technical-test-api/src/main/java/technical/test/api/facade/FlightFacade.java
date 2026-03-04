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
    private static final int PAGE_SIZE = 6; // Taille de page imposée

    private final FlightService flightService;
    private final AirportService airportService;
    private final FlightMapper flightMapper;
    private final AirportMapper airportMapper;

    /**
     * Récupère les vols avec tri et pagination.
     */
    public Flux<FlightRepresentation> getFlights(String sort, int page) {
        return flightService.getAllFlights()
                .sort(getRecordComparator(sort))
                .skip((long) page * PAGE_SIZE)
                .take(PAGE_SIZE)
                .flatMap(this::enrichFlight);
    }

    public Mono<FlightRepresentation> createFlight(FlightRepresentation flightRepresentation) {
        FlightRecord record = flightMapper.convert(flightRepresentation);
        return flightService.addFlight(record)
                .flatMap(this::enrichFlight);
    }

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

    private Comparator<FlightRecord> getRecordComparator(String sort) {
        if (sort == null) {
            return Comparator.comparing(FlightRecord::getId);
        }

        return switch (sort.toLowerCase()) {
            case SORT_PRICE -> Comparator.comparing(FlightRecord::getPrice);
            case SORT_ORIGIN -> Comparator.comparing(FlightRecord::getOrigin);
            default -> throw new IllegalArgumentException("Invalid sort parameter: " + sort);
        };
    }
}