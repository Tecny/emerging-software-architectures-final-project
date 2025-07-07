package com.dtaquito_backend.dtaquito_backend.sportspaces.application.internal.eventhandlers;

import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.events.SportSpacesCreatedEvent;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.model.queries.GetSportSpacesByIdQuery;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.SportSpacesCommandService;
import com.dtaquito_backend.dtaquito_backend.sportspaces.domain.services.SportSpacesQueryService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class SportSpacesCreatedEventHandler {

    private final SportSpacesCommandService sportSpacesCommandService;
    private final SportSpacesQueryService sportSpacesQueryService;

    public SportSpacesCreatedEventHandler(SportSpacesCommandService sportSpacesCommandService, SportSpacesQueryService sportSpacesQueryService) {
        this.sportSpacesCommandService = sportSpacesCommandService;
        this.sportSpacesQueryService = sportSpacesQueryService;
    }

    @EventListener(SportSpacesCreatedEvent.class)
    public void on(SportSpacesCreatedEvent event){
        System.out.println("SportSpacesCreatedEvent received for sport space ID: " + event.getId());

        sportSpacesCommandService.handleSportSpacesCreatedEvent(event);

        var getSportSpacesByIdQuery = new GetSportSpacesByIdQuery(event.getId());

        var sportSpace = sportSpacesQueryService.handle(getSportSpacesByIdQuery);

        if(sportSpace.isPresent()){
            System.out.println("Sport space with ID " + event.getId() + " has been created.");
        } else {
            System.out.println("No sport space found with ID " + event.getId());
        }
    }
}
