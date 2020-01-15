package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/rest")
public class ShipsController {

    @Autowired
    private ShipService shipService;

    @RequestMapping(path = "/ships", method = RequestMethod.GET)
    public @ResponseBody
    List<Ship> getShips(@RequestParam(value = "name", required = false) String name,
                        @RequestParam(value = "planet", required = false) String planet,
                        @RequestParam(value = "shipType", required = false) ShipType shipType,
                        @RequestParam(value = "after", required = false) Long after,
                        @RequestParam(value = "before", required = false) Long before,
                        @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                        @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                        @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                        @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                        @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                        @RequestParam(value = "minRating", required = false) Double minRating,
                        @RequestParam(value = "maxRating", required = false) Double maxRating,
                        @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
                        @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                        @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {
        List<Ship> ships = shipService.getShips(name,
                planet,
                shipType,
                after,
                before,
                isUsed,
                minSpeed,
                maxSpeed,
                minCrewSize,
                maxCrewSize,
                minRating,
                maxRating);
        shipService.sortShipListByOrder(ships, order);

        int fromIndex = pageSize * pageNumber;
        int toIndex = Math.min(fromIndex + pageSize, ships.size());

        return ships.subList(fromIndex, toIndex);
    }

    @RequestMapping(path = "/ships/count", method = RequestMethod.GET)
    public @ResponseBody
    Integer getShipsCount(@RequestParam(value = "name", required = false) String name,
                          @RequestParam(value = "planet", required = false) String planet,
                          @RequestParam(value = "shipType", required = false) ShipType shipType,
                          @RequestParam(value = "after", required = false) Long after,
                          @RequestParam(value = "before", required = false) Long before,
                          @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                          @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                          @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                          @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                          @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                          @RequestParam(value = "minRating", required = false) Double minRating,
                          @RequestParam(value = "maxRating", required = false) Double maxRating) {
        return shipService.getShips(
                name,
                planet,
                shipType,
                after,
                before,
                isUsed,
                minSpeed,
                maxSpeed,
                minCrewSize,
                maxCrewSize,
                minRating,
                maxRating).size();
    }

    @RequestMapping(path = "/ships", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<Ship> createShip(@RequestBody Ship ship) {

        if (ship == null)
            return ResponseEntity.status(400).body(null);

        if (ship.getSpeed() != null) ship.setSpeed(shipService.round(ship.getSpeed()));
        if (!shipService.isShipValid(ship))
            return ResponseEntity.status(400).body(null);

        if (ship.getUsed() == null) ship.setUsed(false);
        shipService.computeRating(ship);

        return ResponseEntity.ok(shipService.saveShip(ship));
    }

    @RequestMapping(path = "/ships/{id}", method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity<Ship> getShipByID(@PathVariable("id") String stringId) {

        Long id = null;
        try {
            id = Long.parseLong(stringId);
        } catch (NumberFormatException ignored) {
        }

        if (id == null || id <= 0)
            return ResponseEntity.status(400).body(null);

        Optional<Ship> ship = shipService.findShipById(id);
        if (!ship.isPresent())
            return ResponseEntity.status(404).body(null);
        else
            return ResponseEntity.ok(ship.get());
    }

    @RequestMapping(path = "/ships/{id}", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<Ship> updateShipByID(@PathVariable("id") String stringId,
                                        @RequestBody Ship newShip) {
        ResponseEntity<Ship> responseEntity = getShipByID(stringId);
        if (!(responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null))
            return responseEntity;

        Ship oldShip = responseEntity.getBody();
        try {
            return ResponseEntity.ok(shipService.updateShip(oldShip, newShip));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @RequestMapping(path = "/ships/{id}", method = RequestMethod.DELETE)
    public @ResponseBody
    ResponseEntity<Ship> deleteShipByID(@PathVariable("id") String stringId) {

        ResponseEntity<Ship> responseEntity = getShipByID(stringId);
        if (!(responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null))
            return responseEntity;

        shipService.deleteShip(responseEntity.getBody());

        return new ResponseEntity<>(HttpStatus.OK);
    }
}