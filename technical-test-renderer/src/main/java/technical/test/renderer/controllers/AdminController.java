package technical.test.renderer.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import technical.test.renderer.facades.FlightFacade;
import technical.test.renderer.viewmodels.AirportViewModel;
import technical.test.renderer.viewmodels.FlightViewModel;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final FlightFacade flightFacade;

    @GetMapping("/flight/new")
    public String showCreateForm(Model model) {
        FlightViewModel flight = new FlightViewModel();
        flight.setOrigin(new AirportViewModel());
        flight.setDestination(new AirportViewModel());

        model.addAttribute("flight", flight);
        return "pages/admin-form";
    }

    @PostMapping("/flight")
    public Mono<String> createFlight(@ModelAttribute FlightViewModel flight) {
        return flightFacade.createFlight(flight)
                .thenReturn("redirect:/");
    }
}